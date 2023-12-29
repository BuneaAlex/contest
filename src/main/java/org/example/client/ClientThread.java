package org.example.client;

import org.example.networking.request.FinishRequest;
import org.example.networking.request.GetCurrentCountryLeaderboardRequest;
import org.example.networking.request.Request;
import org.example.networking.request.SendPointsRequest;
import org.example.networking.response.CurrentCountryLeaderboardResponse;
import org.example.networking.response.FinishResponse;
import org.example.networking.response.Response;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {

    private final String country;
    private List<String> files;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientThread(String country, List<String> files) {
        this.country = country;
        this.files = files;
    }

    private void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, PORT);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

    }

    private void sendRequest(Request request) {
        try {
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response getResponse() {
        try {
            return (Response) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {

        try {

            openConnection();

            for (String file : files) {
                //System.out.println(file);
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    List<String> points = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        points.add(line);
                        if (points.size() == 20) {

                            //send
                            //System.out.println(Thread.currentThread().getId() + " Sending " + points);
                            Thread.sleep(1000);//simulate ui interface action
                            sendRequest(new SendPointsRequest(country, points.toArray(new String[0])));
                            Response response = getResponse();

                            // System.out.println(points);
                            points.clear();
                        }
                    }
                } catch ( IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //receive clasament tari
                sendRequest(new GetCurrentCountryLeaderboardRequest());
                CurrentCountryLeaderboardResponse response = (CurrentCountryLeaderboardResponse) getResponse();
                System.out.println("Country " + country + " Async result:" + response.getData());
            }
            sendRequest(new FinishRequest());
            FinishResponse responseFinal = (FinishResponse) getResponse();

            DataInputStream dis = new DataInputStream(socket.getInputStream());

            String countryLeaderboardPathFormat = "org\\example\\client\\files\\Clasament_tari_" + country + ".txt";
            String participantsLeaderboardPathFormat = "org\\example\\client\\files\\Clasament_conc_" + country + ".txt";

            receiveFile(countryLeaderboardPathFormat, dis);
            System.out.println("Received final country leaderboard");

            receiveFile(participantsLeaderboardPathFormat, dis);
            System.out.println("Received participants final leaderboard");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void receiveFile(String filePath, DataInputStream dis) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            // Read the file size
            long fileSize = dis.readLong();
            long totalRead = 0;
            byte[] buffer = new byte[4096];
            int bytesRead;
            while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            bos.flush();
        }
    }

}

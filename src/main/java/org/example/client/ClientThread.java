package org.example.client;

import org.example.networking.request.*;
import org.example.networking.response.CountryLeaderboardResponse;
import org.example.networking.response.CurrentCountryLeaderboardResponse;
import org.example.networking.response.FinalLeaderBoardResponse;
import org.example.networking.response.Response;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {

    private List<String> files;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientThread(List<String> files) {
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
                System.out.println(file);
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    int count = 0;
                    String line;
                    List<String> points = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        count++;
                        points.add(line);
                        if (count == 20) {


                            //send
                            //System.out.println(Thread.currentThread().getId() + " Sending " + points);
                            sendRequest(new SendPointsRequest(points));
                            Response response = getResponse();

                            //System.out.println(points);
                            points.clear();
                            count = 0;
                        }
                    }
                } catch ( IOException e) {
                    throw new RuntimeException(e);
                }

                //receive clasament tari
                sendRequest(new GetCurrentCountryLeaderboardRequest());
                CurrentCountryLeaderboardResponse response = (CurrentCountryLeaderboardResponse) getResponse();
            }

            //receive clasament tari
            sendRequest(new GetCountryLeaderboardRequest());

            receiveFileThroughSocket("org\\example\\client\\files\\Clasament_tari.txt");

            CountryLeaderboardResponse response = (CountryLeaderboardResponse) getResponse();
            System.out.println("Received final country leaderboard");
            //receive clasament final
            sendRequest(new GetFinalLeaderboardRequest());

            System.out.println("Getting final leaderboard");
            receiveFileThroughSocket("org\\example\\client\\files\\Clasament_conc.txt");

            FinalLeaderBoardResponse responseFinal = (FinalLeaderBoardResponse) getResponse();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFileThroughSocket(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // Create a byte array to hold the incoming file data
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the file data from the socket and save it to a file
            while ((bytesRead = input.read(buffer)) != -1) {
                System.out.println("receiving file...");
                fos.write(buffer, 0, bytesRead);
            }

            System.out.println("File received successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

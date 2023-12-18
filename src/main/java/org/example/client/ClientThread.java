package org.example.client;

import org.example.networking.request.GetCountryLeaderboardRequest;
import org.example.networking.request.GetFinalLeaderboardRequest;
import org.example.networking.request.Request;
import org.example.networking.request.SendPointsRequest;
import org.example.networking.response.CountryLeaderboardResponse;
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
                            System.out.println(Thread.currentThread().getId() + " Sending " + points);
                            sendRequest(new SendPointsRequest(points));
                            Response response = getResponse();

                            System.out.println(points);
                            points.clear();
                            count = 0;
                        }
                    }
                } catch ( IOException e) {
                    throw new RuntimeException(e);
                }

                //receive clasament tari
                sendRequest(new GetCountryLeaderboardRequest());
                CountryLeaderboardResponse response = (CountryLeaderboardResponse) getResponse();

            }

            //receive clasament tari
            sendRequest(new GetCountryLeaderboardRequest());
            CountryLeaderboardResponse response = (CountryLeaderboardResponse) getResponse();
            //receive clasament final
            sendRequest(new GetFinalLeaderboardRequest());
            FinalLeaderBoardResponse responseFinal = (FinalLeaderBoardResponse) getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

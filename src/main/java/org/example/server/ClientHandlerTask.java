package org.example.server;

import org.example.networking.request.*;
import org.example.networking.response.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandlerTask implements Runnable {
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private AtomicInteger connections;
    private boolean connected;

    private SharedQueue sharedQueue;

    public ClientHandlerTask(Socket socket, SharedQueue sharedQueue, AtomicInteger connections) {
        this.sharedQueue = sharedQueue;
        this.connections = connections;
        try {
            this.socket = socket;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        this.connected = true;
    }

    private Response handleRequest(Request request) throws InterruptedException {
        Response response=null;
        if (request instanceof SendPointsRequest){
            SendPointsRequest  req = (SendPointsRequest) request;
            //System.out.println(req.getData());
            //System.out.println("Receiving data...");
            for(String elem : req.getData())
                sharedQueue.produce(elem);
            response = new OkResponse();
        } else if (request instanceof GetCurrentCountryLeaderboardRequest) {
            System.out.println("Sending current country leaderboard");

            //Partea de future

            response = new CurrentCountryLeaderboardResponse(null);

        } else if (request instanceof GetCountryLeaderboardRequest) {
            System.out.println("Sending final country leaderboard");

            sendFileThroughSocket("org\\example\\server\\files\\Clasament_tari.txt");

            response = new CountryLeaderboardResponse(new byte[0]);
        } else if (request instanceof GetFinalLeaderboardRequest) {
            System.out.println("Sending final participants leaderboard");

            // Create a buffered input stream to read the file
            sendFileThroughSocket("org\\example\\server\\files\\Clasament_conc.txt");

            response = new FinalLeaderBoardResponse(new byte[0]);

            connections.decrementAndGet();
            connected = false;
        }
        return response;
    }

    private void sendFileThroughSocket(String filename) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
            // Create a byte array to hold the file data
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the file and send it through the socket
            while ((bytesRead = bis.read(buffer)) != -1) {
                System.out.println("sending file...");
                output.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(Response response) throws IOException{
        System.out.println("sending response "+response);
        synchronized (output) {
            output.writeObject(response);
            output.flush();
        }
    }

    @Override
    public void run() {
        while(connected){
            try {
                Object request=input.readObject();
                Object response=handleRequest((Request)request);
                if (response!=null){
                    sendResponse((Response) response);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error "+e);
        }
    }
}

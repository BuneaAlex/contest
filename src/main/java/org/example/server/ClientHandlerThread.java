package org.example.server;

import org.example.networking.request.GetCountryLeaderboardRequest;
import org.example.networking.request.GetFinalLeaderboardRequest;
import org.example.networking.request.Request;
import org.example.networking.request.SendPointsRequest;
import org.example.networking.response.CountryLeaderboardResponse;
import org.example.networking.response.FinalLeaderBoardResponse;
import org.example.networking.response.OkResponse;
import org.example.networking.response.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandlerThread extends Thread {
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean connected = true;

    public ClientHandlerThread(Socket socket) {
        try {
            this.socket = socket;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private Response handleRequest(Request request){
        Response response=null;
        if (request instanceof SendPointsRequest){
            SendPointsRequest  req = (SendPointsRequest) request;
            System.out.println(req.getData());
            response = new OkResponse();
        }
        else if (request instanceof GetCountryLeaderboardRequest) {
            response = new CountryLeaderboardResponse(new byte[0]);
        } else if (request instanceof GetFinalLeaderboardRequest) {
            response = new FinalLeaderBoardResponse(new byte[0]);
            connected = false;
        }
        return response;
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
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

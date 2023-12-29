package org.example.server;

import org.example.networking.request.FinishRequest;
import org.example.networking.request.GetCurrentCountryLeaderboardRequest;
import org.example.networking.request.Request;
import org.example.networking.request.SendPointsRequest;
import org.example.networking.response.CurrentCountryLeaderboardResponse;
import org.example.networking.response.FinishResponse;
import org.example.networking.response.OkResponse;
import org.example.networking.response.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class ClientHandlerTask implements Runnable {
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private AtomicInteger connections;
    private boolean connected;
    private SharedQueue sharedQueue;
    private final LeaderboardService leaderboardService;

    private final List<String> currentCountryLeaderboardCache;

    private AtomicLong timeWhenTheCountryLeaderboardWasSent;

    private long delta;

    public ClientHandlerTask(Socket socket, SharedQueue sharedQueue, AtomicInteger connections, LeaderboardService leaderboardService, ObjectInputStream input, ObjectOutputStream output, List<String> currentCountryLeaderboardCache, AtomicLong timeWhenTheCountryLeaderboardWasSent, long delta) {
        this.sharedQueue = sharedQueue;
        this.connections = connections;
        this.leaderboardService = leaderboardService;
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.currentCountryLeaderboardCache = currentCountryLeaderboardCache;
        this.timeWhenTheCountryLeaderboardWasSent = timeWhenTheCountryLeaderboardWasSent;
        this.delta = delta;
        this.connected = true;
    }

    private Response handleRequest(Request request) throws InterruptedException {
        Response response=null;
        if (request instanceof SendPointsRequest){
            SendPointsRequest  req = (SendPointsRequest) request;
            //System.out.println(req.getData());
            //System.out.println("Receiving data...");
            for(String elem : req.getData())
            {
                //System.out.println(elem);
                String playerFormat = elem + " " + req.getCountry();
                sharedQueue.produce(playerFormat);
            }

            response = new OkResponse();
        } else if (request instanceof GetCurrentCountryLeaderboardRequest) {


            long currentTime = System.nanoTime();
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(currentTime - timeWhenTheCountryLeaderboardWasSent.get());
            //System.out.println("elapsedTime:" + elapsedTime);
            if(currentCountryLeaderboardCache.isEmpty() || elapsedTime >= delta)
            {
                System.out.println("Sending current country leaderboard");
                CompletableFuture<List<String>> futureResult = calculateCurrentCountryLeaderboard();
                // Wait for the completion of the CompletableFuture
                List<String> result = futureResult.join();
                // Set the response based on the completed asynchronous task
                response = new CurrentCountryLeaderboardResponse(result);
                synchronized (currentCountryLeaderboardCache) {
                    currentCountryLeaderboardCache.clear();
                    currentCountryLeaderboardCache.addAll(result);
                }
                timeWhenTheCountryLeaderboardWasSent.set(System.nanoTime());
                //System.out.println(currentCountryLeaderboardCache);
                //System.out.println(result);
            }
            else
            {
                System.out.println("Sending cached country leaderboard");
                response = new CurrentCountryLeaderboardResponse(currentCountryLeaderboardCache);
                //System.out.println(currentCountryLeaderboardCache);
            }



        }
        else if (request instanceof FinishRequest) {
            System.out.println("Final response");

            response = new FinishResponse();
            connected = false;
        }
        return response;
    }



    private CompletableFuture<List<String>> calculateCurrentCountryLeaderboard() {
        // Asynchronous task
        return CompletableFuture.supplyAsync(leaderboardService::getCurrentCountryLeaderboard);
    }

    private void sendResponse(Response response) throws IOException{
        //System.out.println("sending response "+response);
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

        }

        connections.decrementAndGet();
        System.out.println("connections:" + connections.get());
    }
}

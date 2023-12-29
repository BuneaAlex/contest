package org.example.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Server {

    private final static int PORT = 12345;
    private static int p_consumers = 4;
    private static int p_producers = 4;

    private static int p_clients = 5;
    private static long delta = 100;
    private static AtomicInteger connections = new AtomicInteger(p_clients);
    private final static SharedQueue sharedQueue = new SharedQueue(100, p_consumers);
    private final static PlayerLinkedList players = new PlayerLinkedList();
    private final static Set<String> bannedIds = new HashSet<>();
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(p_producers);
    private final static ConsumerThread[] threads = new ConsumerThread[p_consumers];

    private final static LeaderboardService leaderboardService = new LeaderboardService(players);

    private static final List<String> currentCountryLeaderboardCache = new CopyOnWriteArrayList<>();

    private static final AtomicLong timeWhenTheCountryLeaderboardWasSent = new AtomicLong(0);

    private static void startConsumers() {
        for (int i = 0; i < p_consumers; i++) {
            threads[i] = new ConsumerThread(players, bannedIds, sharedQueue, connections,leaderboardService);
            threads[i].start();
        }
    }

    private static void joinConsumers() {
        for (int i = 0; i < p_consumers; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        System.out.println(args.length);
        if (args.length == 3) {
            try {
                p_consumers = Integer.parseInt(args[0]);
                p_producers = Integer.parseInt(args[1]);
                delta = Integer.parseInt(args[2]);
                System.out.println("args:" + p_consumers + " " + p_producers + " " + delta);
            } catch (NumberFormatException e) {
                System.err.println("Invalid value for argument p. Using the default value.");
            }
        }

        long startTime = System.nanoTime();

        startConsumers();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            List<Socket> clientSockets = new ArrayList<>();


            for(int i=0;i<p_clients;i++)
            {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                output.flush();
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                // Create a new thread to handle the client
                ClientHandlerTask clientHandler = new ClientHandlerTask(clientSocket, sharedQueue, connections, leaderboardService,input,output, currentCountryLeaderboardCache, timeWhenTheCountryLeaderboardWasSent, delta);
                threadPool.submit(clientHandler);
            }
            threadPool.shutdown();
            while(!threadPool.isTerminated())
            {

            }
            joinConsumers();

            for(int i=0;i<clientSockets.size();i++)
            {
                Socket clientSocket = clientSockets.get(i);
                System.out.println("Sending FINAL leaderboard in files");

                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                sendFile("org\\example\\server\\files\\Clasament_tari.txt", dos);
                sendFile("org\\example\\server\\files\\Clasament_conc.txt", dos);

                clientSocket.close();

            }

            long endTime = System.nanoTime();
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            System.out.println("Elapsed time: " + elapsedTime + " milliseconds");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(String filePath, DataOutputStream dos) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();

        // Send the size of the file
        dos.writeLong(fileSize);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        }
    }


}

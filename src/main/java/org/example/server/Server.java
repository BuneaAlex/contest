package org.example.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private final static int PORT = 12345;
    private final static int p_consumers = 4;
    private final static int p_producers = 5;
    private static AtomicInteger connections = new AtomicInteger(5);
    private final static SharedQueue sharedQueue = new SharedQueue(100, p_consumers);
    private final static PlayerLinkedList players = new PlayerLinkedList();
    private final static Set<String> bannedIds = new HashSet<>();
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(p_producers);
    private final static AtomicBoolean online = new AtomicBoolean(true);
    private final static ConsumerThread[] threads = new ConsumerThread[p_consumers];

    private final static LeaderboardService leaderboardService = new LeaderboardService(players);

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
        startConsumers();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            List<Socket> clientSockets = new ArrayList<>();
            List<ObjectOutputStream> outputStreams = new ArrayList<>();
            List<ObjectInputStream> inputStreams = new ArrayList<>();

            //facut for? cel putin initial
            //while (connections.getCount() > 0)
            for(int i=0;i<5;i++)
            {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                output.flush();
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                outputStreams.add(output);
                inputStreams.add(input);

                // Create a new thread to handle the client
                ClientHandlerTask clientHandler = new ClientHandlerTask(clientSocket, sharedQueue, connections, leaderboardService,input,output);
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
                ObjectOutputStream output = outputStreams.get(i);
                ObjectInputStream input = inputStreams.get(i);
                System.out.println("Sending FINAL leaderboard in files");

                //sendFileThroughSocket("org\\example\\server\\files\\Clasament_tari.txt",output);

                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                sendFile("org\\example\\server\\files\\Clasament_tari.txt", dos);

                //output.flush();

                //sendFileThroughSocket("org\\example\\server\\files\\Clasament_conc.txt",output);
                sendFile("org\\example\\server\\files\\Clasament_conc.txt", dos);

                clientSocket.close();

            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileThroughSocket(String filename, ObjectOutputStream output) throws IOException {

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename))) {
            // Create a byte array to hold the file data
            byte[] buffer = new byte[1024];
            int bytesRead;
            // Read the file and send it through the socket
            while ((bytesRead = bis.read(buffer)) != -1) {
                System.out.println("sending file...");
                output.write(buffer, 0, bytesRead);
            }
            output.flush();

            System.out.println("File sent successfully.");
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

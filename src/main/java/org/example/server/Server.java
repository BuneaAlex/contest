package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private final static int PORT = 12345;
    private final static int p_consumers = 4;
    private final static int p_producers = 4;
    private static AtomicInteger connections = new AtomicInteger(5);
    private final static SharedQueue sharedQueue = new SharedQueue(100, p_consumers);
    private final static PlayerLinkedList players = new PlayerLinkedList();
    private final static Set<String> bannedIds = new HashSet<>();
    private final static ExecutorService threadPool = Executors.newFixedThreadPool(p_producers);
    private final static AtomicBoolean online = new AtomicBoolean(true);
    private final static ConsumerThread[] threads = new ConsumerThread[p_consumers];

    private static void startConsumers() {
        for (int i = 0; i < p_consumers; i++) {
            threads[i] = new ConsumerThread(players, bannedIds, sharedQueue, connections);
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

            while (connections.get() > 0) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread to handle the client
                ClientHandlerTask clientHandler = new ClientHandlerTask(clientSocket, sharedQueue, connections);
                threadPool.submit(clientHandler);
            }
            threadPool.shutdown();
            joinConsumers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

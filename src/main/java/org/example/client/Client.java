package org.example.client;

import java.util.ArrayList;
import java.util.List;

public class Client {

    private static final int numOfCountries = 5;
    private static final int numOfProblems = 10;
    private static final ClientThread[] clientThreads = new ClientThread[numOfCountries];


    private static void prepareClients() {
        for(int i=1;i<=numOfCountries;i++)
        {
            List<String> fileNames = new ArrayList<>();
            String country = "C" + i;
            for(int pb=1;pb<=numOfProblems;pb++)
            {
                String filename = "org\\example\\client\\files\\" + "Rezultate" + country + "_" + "P" + pb + ".txt";
                fileNames.add(filename);
            }
            clientThreads[i-1] = new ClientThread(fileNames);
        }
    }

    private static void startClients() {
        for (var t : clientThreads) {
            t.start();
        }
    }

    private static void joinClients() {
        for (ClientThread t : clientThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        prepareClients();
        startClients();
        joinClients();
    }
}

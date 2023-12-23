package org.example.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardService {

    private final PlayerLinkedList players;
    public LeaderboardService(PlayerLinkedList players) {
        this.players = players;
    }

    public List<String> getCurrentCountryLeaderboard() {

        Map<String, Integer> countryLeaderboard = new HashMap<>();

        //copy list so that it does not modify during iteration
        List<Player> playersCopy = players.copyToList();

        //System.out.println(playersCopy);

        for (Player player : playersCopy) {
            //System.out.println(player);
            String country = player.getCountry();
            int score = player.getScore();

            // Check if the country is present in the leaderboard
            if (countryLeaderboard.containsKey(country)) {
                // Update the score by adding the additional score
                int currentScore = countryLeaderboard.get(country);
                int newScore = currentScore + score;
                countryLeaderboard.put(country, newScore);
            } else
                countryLeaderboard.put(country, score);
        }

        // Sort the entries by values in descending order
        List<String> sortedLeaderboard = countryLeaderboard.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> entry.getKey() + " " + entry.getValue()) // Combine key and value with space
                .collect(Collectors.toList());

        return sortedLeaderboard;
    }

    public void writePlayerLeaderboardToFile() {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("org\\example\\server\\files\\Clasament_conc.txt"));

            List<Player> playerList = new ArrayList<>();
            for (Player p : players) playerList.add(p);
            playerList.sort(new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return o2.getScore() - o1.getScore();
                }
            });
            for (Player p : playerList) {
                writer.write(p.getId() + " " + p.getScore() + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCountryLeaderboardToFile()
    {
        List<String> countryLeaderboard = getCurrentCountryLeaderboard();
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("org\\example\\server\\files\\Clasament_tari.txt"));

            for(String country : countryLeaderboard)
            {
                //System.out.println(country);
                writer.write(country);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

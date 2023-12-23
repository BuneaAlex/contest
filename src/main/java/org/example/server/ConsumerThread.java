package org.example.server;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

public class ConsumerThread extends Thread{
    private final PlayerLinkedList players;
    private final Set<String> bannedIds;
    private final SharedQueue playerQueue;
    private final AtomicInteger connections;
    private final LeaderboardService leaderboardService;

    public ConsumerThread(PlayerLinkedList players, Set<String> bannedIds, SharedQueue playerQueue, AtomicInteger connections,LeaderboardService leaderboardService) {
        this.players = players;
        this.bannedIds = bannedIds;
        this.playerQueue = playerQueue;
        this.connections = connections;
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void run() {
        try {
            while (connections.get() > 0 || !playerQueue.isQueueEmpty()) {

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //System.out.println("conn:" + connections.get() + " is empty? " + playerQueue.isQueueEmpty() );

                if(!playerQueue.isQueueEmpty())
                {
                    String data = playerQueue.consume();
                    System.out.println(data);
                    //System.out.println(Thread.currentThread().getId() + " consume: " + data);
                    String[] dataList = data.split(" ");
                    String participant = dataList[0];
                    int points = parseInt(dataList[1]);
                    String country = dataList[2];
                    Player player = new Player(participant, points, country);
                    synchronized (bannedIds) {
                        if (player != null) {
                            if (!bannedIds.contains(player.getId())) {
                                if (player.getScore() == -1) {
                                    bannedIds.add(player.getId());
                                    players.remove(player.getId());
                                } else {
                                    players.insert(player);
                                }
                            }
                        }
                    }
                }

            }
            playerQueue.decrementWriter();
            if(playerQueue.getWritersCounter() == 0)
            {
                leaderboardService.writePlayerLeaderboardToFile();
                leaderboardService.writeCountryLeaderboardToFile();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}

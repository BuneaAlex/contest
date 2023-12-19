package org.example.server;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

public class ConsumerThread extends Thread{
    private final PlayerLinkedList players;
    private final Set<String> bannedIds;
    private final SharedQueue playerQueue;
    private final AtomicInteger online;

    public ConsumerThread(PlayerLinkedList players, Set<String> bannedIds, SharedQueue playerQueue, AtomicInteger online) {
        this.players = players;
        this.bannedIds = bannedIds;
        this.playerQueue = playerQueue;
        this.online = online;
    }

    @Override
    public void run() {
        try {
            while (online.get() > 0 || !playerQueue.isQueueEmpty()) {

                String data = playerQueue.consume();
                System.out.println(Thread.currentThread().getId() + " consume: " + data);
                String[] dataList = data.split(" ");
                String participant = dataList[0];
                int points = parseInt(dataList[1]);
                Player player = new Player(participant, points);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

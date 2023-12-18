package org.example.server;

public class Player {
    private String id;
    private int score;

    Player(String id) {
        this.id = id;
        this.score = 0;
    }

    public Player(String id, int score) {
        this.id = id;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && this.id.equals(((Player)obj).id);
    }

    @Override
    public String toString() {
        return id + " " + score;
    }
}

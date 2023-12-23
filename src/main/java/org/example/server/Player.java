package org.example.server;

public class Player {
    private String id;
    private int score;
    private String country;

    public String getCountry() {
        return country;
    }

    Player(String id) {
        this.id = id;
        this.score = 0;
    }

    public Player(String id, int score, String country) {
        this.id = id;
        this.score = score;
        this.country = country;
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

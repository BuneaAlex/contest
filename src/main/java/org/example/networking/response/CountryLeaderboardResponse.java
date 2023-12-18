package org.example.networking.response;

public class CountryLeaderboardResponse implements Response{
    private byte[] file;

    public CountryLeaderboardResponse(byte[] file) {
        this.file = file;
    }

    public byte[] getFile() {
        return file;
    }
}

package org.example.networking.response;

public class FinalLeaderBoardResponse implements Response {
    private byte[] file;

    public FinalLeaderBoardResponse(byte[] file) {
        this.file = file;
    }

    public byte[] getFile() {
        return file;
    }
}

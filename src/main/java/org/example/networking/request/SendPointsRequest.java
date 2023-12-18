package org.example.networking.request;

import java.util.List;

public class SendPointsRequest implements Request {

    private List<String> data;

    public SendPointsRequest(List<String> data) {
        this.data = data;
    }

    public List<String> getData() {
        return data;
    }
}

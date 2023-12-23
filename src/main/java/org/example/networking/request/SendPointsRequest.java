package org.example.networking.request;

import java.util.List;

public class SendPointsRequest implements Request {

    private String country;
    private List<String> data;

    public SendPointsRequest(String country, List<String> data) {
        this.country = country;
        this.data = data;
    }

    public List<String> getData() {
        return data;
    }

    public String getCountry() {
        return country;
    }
}

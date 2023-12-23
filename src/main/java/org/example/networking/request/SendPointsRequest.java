package org.example.networking.request;

public class SendPointsRequest implements Request {

    private String country;
    private String[] data;

    public SendPointsRequest(String country, String[] data) {
        this.country = country;
        this.data = data;
    }

    public String[] getData() {
        return data;
    }

    public String getCountry() {
        return country;
    }
}

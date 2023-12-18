package org.example.networking.response;

import java.util.List;

public class CurrentCountryLeaderboardResponse implements Response{

    private List<String> data;

    public CurrentCountryLeaderboardResponse(List<String> data) {
        this.data = data;
    }

    public List<String> getData() {
        return data;
    }


}

package com.rssreader.model;

import java.util.List;

public class FeedlyResponse {

    private List<FeedlyResult> results;

    public  FeedlyResponse(){}

    public List<FeedlyResult> getResults() {
        return results;
    }

    public void setResults(List<FeedlyResult> results) {
        this.results = results;
    }
}

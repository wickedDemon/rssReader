package com.rssreader.service;

import com.rssreader.model.FeedlyResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface FeedlyService {

    String FEEDLY_ENDPOINT = "http://feedly.com";

    @GET("/v3/search/feeds")
    void searchFeeds(@QueryMap Map<String, String> options, Callback<FeedlyResponse> cb);
}

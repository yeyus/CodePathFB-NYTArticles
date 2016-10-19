package com.ea7jmf.nytarticles.apis;

import com.ea7jmf.nytarticles.models.SearchResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jesusft on 10/18/16.
 */

public interface NYTArticleSearchApiEndpoint {

    @GET("/svc/search/v2/articlesearch.json")
    rx.Observable<SearchResponse> articleSearch(@Query("begin_date") String beginDate, @Query("sort") String sort, @Query("fq") String filterQuery, @Query("api-key") String apiKey);

}
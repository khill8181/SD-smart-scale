package com.example.Smartscale;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface PlaceholderAPI {

    @Headers({
            "x-app-id: ca60f771",
            "x-app-key: 02b0513e9c67fb9cd31f3f85efa5c7f5"
    })
    //if this were proper, it would return a list of "food" objects
    @GET("v2/search/instant")
    Call<ResponseBody> getPosts(@Query("query") String query,
                                @Query("detailed") boolean isDetailed,
                                @Query("branded") boolean isBranded);
}

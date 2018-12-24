package com.razy.location.network;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface TempInterface {
    @GET
    Call<com.razy.location.model.WeatherMain> getCatList(@Url String url);

}

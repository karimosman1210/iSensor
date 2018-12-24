package com.razy.location.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitDataCliantInstance {
    private static Retrofit retrofit;
    private static final String BASE_URL="http://api.openweathermap.org/data/2.5/";

    public static Retrofit baseUrl() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

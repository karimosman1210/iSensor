package com.razy.location.network;

import com.razy.location.model.WeatherMain;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface DataServices {
    @POST("senddata")
    @FormUrlEncoded
    Call<ResponseBody> setPosts( @Field("DeviceId") String devId,
                                @Field("Lat") String lat, @Field("Long") String lng,
                                @Field("Battery") String battery, @Field("Temperature") String temp,
                                @Field("Humidity") String humiolty, @Field("Connectivity_Status") String connectStatuse,
                                @Field("Accelerator") String accelerator, @Field("Country") String county
            , @Field("IMEI") String emei, @Field("TimeZone") String timeZone);
}

package com.razy.location.network;
import com.razy.location.CountryModel;

import java.util.List;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DataCountry {
    @GET("json")
    Call<CountryModel> CountryData();
}

package com.razy.location.network;

import android.util.Log;

import com.razy.location.CountryModel;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CountryPresnter {
    GetDataCountry getDataCountry;

    public CountryPresnter(GetDataCountry getDataCountry) {
        this.getDataCountry = getDataCountry;
    }

    public void getCountryOFData(){
        DataCountry dataCountry=RetrofCountryitInstance.baseUrl().create(DataCountry.class);
        dataCountry.CountryData().enqueue(new Callback<CountryModel>() {
            @Override
            public void onResponse(Call<CountryModel> call, Response<CountryModel> response) {
                if (response.isSuccessful()){
                    getDataCountry.getDataOfCountry(response.body().getCountry());
                    getDataCountry.cityName(response.body().getCity());
                }else {
                    Log.d("dasd","ntodaodn");
                }
            }

            @Override
            public void onFailure(Call<CountryModel> call, Throwable t) {

            }
        });

    }
}

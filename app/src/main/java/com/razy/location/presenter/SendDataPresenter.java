package com.razy.location.presenter;

import android.util.Log;

import com.razy.location.network.DataServices;
import com.razy.location.network.RetrofitCliantInstance;
import com.razy.location.network.ShowResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendDataPresenter {
    ShowResult showResult;
    static String lastTemp = "0";

    public SendDataPresenter(ShowResult showResult) {
        this.showResult = showResult;
    }

    public void sendData(String devId, String latitude, String longitude, String battary, String temp,
                         String humuiolity, String statuseConnection, String speed, String country, String imei,
                         String timeZone) {
//        Log.d("tests", "devid" + devId);
//        Log.d("tests", "lat" + String.valueOf(latitude));
//        Log.d("tests", "long" + String.valueOf(longitude));
//        Log.d("tests", "battary" + battary);
//        Log.d("tests", "temp" + temp);
//        Log.d("tests", "humm" + humuiolity);
//        Log.d("tests", "connect" + statuseConnection);
//        Log.d("tests", "speed" + String.valueOf(speed));
//        Log.d("tests", "coounty" + country);
//        Log.d("tests", "imi" + imei);
//        Log.d("tests", "timezone" + timeZone);
        if (devId != null && latitude != null && longitude != null && battary != null && temp != null && humuiolity != null &&
                statuseConnection != null && speed != null && country != null && imei != null && timeZone != null) {
            if (!speed.equals("0.0") || !lastTemp.equals(temp)) {

                DataServices dataServices = RetrofitCliantInstance.getRetrofitInstance().create(DataServices.class);
                dataServices.setPosts(devId, latitude, longitude, battary, temp, humuiolity, statuseConnection, speed,
                        country, imei, timeZone).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            showResult.resultMassage("done");
                            Log.d("hittttt", "is sucess");
                        } else {
                            showResult.resultMassage("else");
                            Log.d("hittttt", "is not sucess");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        showResult.resultMassage("onFailure");
                    }
                });
                lastTemp = temp;
            }

        }
    }
}

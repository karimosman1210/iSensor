package com.razy.location.network;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class InternetCheck extends AsyncTask<Void, Void, Boolean> {


    private Activity activity;
    private InternetCheckListener listener;

    public InternetCheck(Activity x, InternetCheckListener y) {

        activity = x;
        listener = y;

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        return hasInternetAccess();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() && activeNetworkInfo.isAvailable();
    }

    private boolean hasInternetAccess() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(2000);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                e.printStackTrace();

            }
        } else {
            Log.d("TAG", "No network available!");
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        listener.onComplete(aBoolean);
    }

    public interface InternetCheckListener {
        void onComplete(boolean connected);
    }

}
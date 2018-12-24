package com.razy.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.razy.location.model.WeatherMain;
import com.razy.location.network.CountryPresnter;
import com.razy.location.network.GetDataCountry;
import com.razy.location.network.InternetCheck;
import com.razy.location.network.RetrofitDataCliantInstance;
import com.razy.location.network.ShowResult;
import com.razy.location.network.TempInterface;
import com.razy.location.presenter.SendDataPresenter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, InternetCheck.InternetCheckListener, ShowResult, GetDataCountry {
    private static final int MY_PERMISION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RESOULUTION_REQUEST = 7192;

    @BindView(R.id.companyProgress)
    SpinKitView companyProgress;
    @BindView(R.id.framePrgress)
    FrameLayout framePrgress;
    @BindView(R.id.androidId)
    TextView androidId;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.stateSignalTv)
    TextView stateSignalTv;
    @BindView(R.id.timeZoneTv)
    TextView timeZoneTv;
    @BindView(R.id.countyName)
    TextView countyName;
    @BindView(R.id.latTv)
    TextView latTv;
    @BindView(R.id.longTv)
    TextView longTv;
    @BindView(R.id.battryLevel)
    TextView battryLevel;
    @BindView(R.id.speenTv)
    TextView speenTv;
    @BindView(R.id.humidityTv)
    TextView humidityTv;
    @BindView(R.id.tempTv)
    TextView tempTv;
    @BindView(R.id.weatherIcon)
    ImageView weatherIcon;
    @BindView(R.id.retry_btn)
    Button retryBtn;
    @BindView(R.id.cityNameTv)
    TextView cityNameTV;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiCliant;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 300; // 3 SEC
    private static int FATEST_iNTERVAL = 300;
    private static int DISPLACEMENT = 10;
    private static String humuiolity;
    private static String temp;
    private static String imei;
    private static String statuseConnection;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;
    static String devId;
    static String timeZone;
    static String country = null;
    private InternetCheck internetCheck;
    static double latitude;
    static double longitude;
    static double speed;
    SendDataPresenter sendDataPresenter;
    static String baturyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        baturyLevel = getBatteryPercentage(this);
        checkConnectivity();
        CountryPresnter countryPresnter = new CountryPresnter(this);
        countryPresnter.getCountryOFData();
    }

    private void lifeApp() {
        checkOpenGps();
        timeZone();
        if (timeZone.equals("+02:00")) {
            Log.d("lang", "cairo");
            tempget("cairo");
        } else {
            tempget("dubai");
            Log.d("lang", "dubai");
        }
        sendDataPresenter = new SendDataPresenter(this);


        devId = deviceId();
        imeiPhone();
        Log.i("dasfasf", timeZone);
        checkConnectionSignal();
        setUpLocation();
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {

                hitApi();


            }
        };
        timer.schedule(hourlyTask, 0l, 1000 * 1 * 60);   // 1000*10*60 every 3 minut

    }

    private void hitApi() {
        sendDataPresenter.sendData(devId, String.valueOf(latitude), String.valueOf(longitude)
                , baturyLevel, temp, humuiolity, statuseConnection, String.valueOf(speed), country,
                imei, timeZone);

    }

    private void checkConnectivity() {
        internetCheck = new InternetCheck(this, this);
        internetCheck.execute();
    }

    private void checkOpenGps() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        } else {
            displayPromptForEnablingGPS(this);
        }
    }

    private void tempget(String city) {
        TempInterface tempInterface = RetrofitDataCliantInstance.baseUrl().create(TempInterface.class);
        tempInterface.getCatList("weather?q=" + city + "&appid=85d4a9d5c32d1e76f82ded957247f3b8&units=Imperial").enqueue(new Callback<WeatherMain>() {
            @Override
            public void onResponse(Call<WeatherMain> call, Response<WeatherMain> response) {
                if (response.isSuccessful()) {
                    double centi = (response.body().getMain().getTemp() - 32) / 1.8000;
                    if (centi <= 40 && centi > 30) {
                        weatherIcon.setImageResource(R.drawable.skys);
                    } else if (centi <= 30 && centi > 20) {
                        weatherIcon.setImageResource(R.drawable.icon_fewclouds);
                    } else if (centi <= 20 && centi > 10) {
                        weatherIcon.setImageResource(R.drawable.icon_scatteredclouds);
                    } else {
                        weatherIcon.setImageResource(R.drawable.icon_mist);
                    }
                    DecimalFormat f = new DecimalFormat("####");

                    temp = String.valueOf(f.format(centi));
                    tempTv.setText(temp + "  °C");
                    humuiolity = String.valueOf(response.body().getMain().getHumidity());
                    humidityTv.setText(humuiolity + " %");


                } else {

                }
            }

            @Override
            public void onFailure(Call<WeatherMain> call, Throwable t) {

            }
        });
    }

    private void checkConnectionSignal() {
        if (isSimSupport(this)) {
            mPhoneStatelistener = new MyPhoneStateListener();
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            statuseConnection = "No SimCard";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiCliant();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;

        }

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISION_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_STATE}, MY_PERMISION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiCliant();
                createLocationRequest();
                displayLocation();
                imeiPhone();
            }

        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_iNTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiCliant() {
        mGoogleApiCliant = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiCliant.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOULUTION_REQUEST);
            else {
                Toast.makeText(this, "this device is not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    // app Done
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiCliant);
        // (1) Battery
        battryLevel.setText(getBatteryPercentage(MainActivity.this) + " % ");
        if (mLastLocation != null) {
            // (2)lat
            latitude = mLastLocation.getLatitude();
            latTv.setText(latitude + " ");
            // (3)long
            longitude = mLastLocation.getLongitude();
            longTv.setText(longitude + " ");
            //(4) speed in km/h
            speed = (int) (mLastLocation.getSpeed() * 18 / 5);
            speenTv.setText(speed + "  km/h ");

            // (6)Date
            date.setText(getDateTime());
            framePrgress.setVisibility(View.GONE);

            Log.d("EDMTDEV", String.format("your location was changed : %f / %f", latitude, longitude));
        } else {
            Log.d("EDMTDEV", String.format("cant get your lication"));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();

    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiCliant, mLocationRequest, this);

        } catch (Exception e) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiCliant.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    //Battry
    public static String getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;
        int bat = (int) (batteryPct * 100);
        return String.valueOf(bat);
    }

    //imeiPhone
    public void imeiPhone() {
        if (pemissionCamera() == true) {
            imei = "MissingPermission";
        } else if (pemissionCamera() == false) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            imei = telephonyManager.getDeviceId();

        }

    }

    // Date
    private String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd   HH:mm");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }

    // Device_id
    public String deviceId() {
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        androidId.setText(" " + android_id);
        return android_id;
    }

    //Time Zone
    public void timeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();

        DateFormat date = new SimpleDateFormat("ZZZZZ", Locale.getDefault());
        String localTime = date.format(currentLocalTime);

        System.out.println(localTime + "  TimeZone   ");
        timeZone = localTime;
        timeZoneTv.setText(localTime);


    }

    // check have simSupport
    public static boolean isSimSupport(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);

    }

    private boolean pemissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_PHONE_STATE}, 1);
                return true;
            } else {
                Log.e("DB", "PERMISSION GRANTED");
                return false;
            }
        }
        return false;
    }

    @OnClick(R.id.retry_btn)
    public void onViewClicked() {
        checkConnectivity();
        retryBtn.setVisibility(View.GONE);
        companyProgress.setVisibility(View.VISIBLE);


    }

    @Override
    public void onComplete(boolean connected) {
        if (connected) {
            lifeApp();
        } else {
            retryBtn.setVisibility(View.VISIBLE);
            companyProgress.setVisibility(View.GONE);
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void resultMassage(String res) {
        Log.d("resultresponse", res);
    }

    @Override
    public void getDataOfCountry(String countryNameGet) {
        country = countryNameGet;
        countyName.setText(country);

    }

    @Override
    public void cityName(String cityName) {
        cityNameTV.setText(cityName);
    }

    //signal statuse
    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm
            int x = Math.abs(mSignalStrength);
            Log.i("kapskdpoas", String.valueOf(mSignalStrength));
            if (x > 30) {
                Log.d("kapskdpoas", "Signal GSM : Good");
                statuseConnection = "Good";
            } else if (x > 20 && mSignalStrength < 30) {
                Log.d("kapskdpoas", "Signal GSM : Avarage");
                statuseConnection = "Avarage";

            } else if (x < 20 && x > 3) {
                Log.d("kapskdpoas", "Signal GSM : Weak");
                statuseConnection = "Weak";

            } else if (x < 3) {
                Log.d("kapskdpoas", "Signal GSM : Very weak");
                statuseConnection = "Very weak";

            }

        }
    }

    public static void displayPromptForEnablingGPS(final Activity activity) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

}

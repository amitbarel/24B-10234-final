package dev.amitb.a24b_10234_finalproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 10001;

    private FusedLocationProviderClient fusedLocationClient;

    private MaterialTextView location_msg;
    private MaterialTextView pollution_window;
    private MaterialButton start_BTN, stop_BTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        if (!checkPermissions()) {
            requestPermissions();
        }

        start_BTN.setOnClickListener(v -> startService());
        stop_BTN.setOnClickListener(v -> stopService());
    }

    private void findViews() {
        location_msg = findViewById(R.id.location_msg);
        pollution_window = findViewById(R.id.pollution_window);
        start_BTN = findViewById(R.id.start_BTN);
        stop_BTN = findViewById(R.id.stop_BTN);
    }

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(AirQualityService.BROADCAST_LOCATION_KEY);
            try {
                int color;
                String txt;
                MyLoc myLoc = new Gson().fromJson(json, MyLoc.class);
                location_msg.setText("Location: " + myLoc.getLat() + " " + myLoc.getLon());
                int index = myLoc.getAqi();
                if (index <= 50) {
                    txt = "Good!";
                    color = R.color.pol_good;
                } else if (index <= 100) {
                    txt = "Fair";
                    color = R.color.pol_medium;
                } else if (index <= 200) {
                    txt = "Poor";
                    color = R.color.pol_bad;
                } else {
                    txt = "Very Poor";
                    color = R.color.pol_very_bad;
                }
                pollution_window.setBackgroundColor(ContextCompat.getColor(MainActivity.this, color));
                pollution_window.setText(txt);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    };

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void startService() {
        sendActionToService(AirQualityService.START_FOREGROUND_SERVICE);
    }

    private void stopService() {
        sendActionToService(AirQualityService.STOP_FOREGROUND_SERVICE);
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, AirQualityService.class);
        intent.setAction(action);

        startForegroundService(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iFilter = new IntentFilter(AirQualityService.BROADCAST_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, iFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

}
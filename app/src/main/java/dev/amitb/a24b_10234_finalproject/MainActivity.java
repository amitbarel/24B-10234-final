package dev.amitb.a24b_10234_finalproject;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 10001;

    private MaterialTextView lat_msg, lon_msg, aqi_msg;
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
        lat_msg = findViewById(R.id.lat_msg);
        lon_msg = findViewById(R.id.lon_msg);
        aqi_msg = findViewById(R.id.aqi_msg);
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
                int index = myLoc.getAqi();
                lat_msg.setText("Latitude: " + myLoc.getLat());
                lon_msg.setText("Longitude: " + myLoc.getLon());
                aqi_msg.setText("AQ Index: " + myLoc.getAqi());
                if (index <= 50) {
                    txt = "Good!";
                    color = R.color.pol_good;
                } else if (index <= 100) {
                    txt = "Moderate";
                    color = R.color.pol_medium;
                } else if (index <= 200) {
                    txt = "Unhealthy";
                    color = R.color.pol_bad;
                } else {
                    txt = "Very Unhealthy or Hazardous";
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
        boolean locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean notificationPermission = true;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationPermission = notificationManager.areNotificationsEnabled();
        }

        return locationPermission && notificationPermission;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void startService() {
        if (checkPermissions())
            sendActionToService(AirQualityService.START_FOREGROUND_SERVICE);
        else
            requestPermissions();
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
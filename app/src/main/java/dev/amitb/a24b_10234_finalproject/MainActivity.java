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

    // Request code for location permissions
    private static final int REQUEST_LOCATION_PERMISSION = 10001;

    // UI elements
    private MaterialTextView lat_msg, lon_msg, aqi_msg;
    private MaterialTextView pollution_window;
    private MaterialButton start_BTN, stop_BTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        findViews();

        // Check and request permissions
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Set click listeners for buttons
        start_BTN.setOnClickListener(v -> startService());
        stop_BTN.setOnClickListener(v -> stopService());
    }

    // Bind UI components
    private void findViews() {
        lat_msg = findViewById(R.id.lat_msg);
        lon_msg = findViewById(R.id.lon_msg);
        aqi_msg = findViewById(R.id.aqi_msg);
        pollution_window = findViewById(R.id.pollution_window);
        start_BTN = findViewById(R.id.start_BTN);
        stop_BTN = findViewById(R.id.stop_BTN);
    }

    // Broadcast receiver for location updates
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(AirQualityService.BROADCAST_LOCATION_KEY);
            try {
                // Parse the received JSON to MyLoc object
                MyLoc myLoc = new Gson().fromJson(json, MyLoc.class);
                int index = myLoc.getAqi();
                int color;
                String txt;

                // Update UI elements with location and AQI data
                lat_msg.setText("Latitude: " + myLoc.getLat());
                lon_msg.setText("Longitude: " + myLoc.getLon());
                aqi_msg.setText("AQ Index: " + myLoc.getAqi());

                // Determine the AQI status and set appropriate background color and text
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

    // Check for necessary permissions
    private boolean checkPermissions() {
        boolean locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean notificationPermission = true;

        // Check notification permission for Android 13 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationPermission = notificationManager.areNotificationsEnabled();
        }

        return locationPermission && notificationPermission;
    }

    // Request necessary permissions
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

    // Start the foreground service
    private void startService() {
        if (checkPermissions())
            sendActionToService(AirQualityService.START_FOREGROUND_SERVICE);
        else
            requestPermissions();
    }

    // Stop the foreground service
    private void stopService() {
        sendActionToService(AirQualityService.STOP_FOREGROUND_SERVICE);
    }

    // Send an action intent to the foreground service
    private void sendActionToService(String action) {
        Intent intent = new Intent(this, AirQualityService.class);
        intent.setAction(action);

        startForegroundService(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Register the BroadcastReceiver to listen for location updates
        IntentFilter iFilter = new IntentFilter(AirQualityService.BROADCAST_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, iFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the BroadcastReceiver to stop receiving updates
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

}
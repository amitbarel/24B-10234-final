package dev.amitb.a24b_10234_finalproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirQualityService extends Service {

    // Actions -> Foreground service
    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    // Broadcast intents
    public static final String BROADCAST_LOCATION = "BROADCAST_LOCATION";
    public static final String BROADCAST_LOCATION_KEY = "BROADCAST_LOCATION_KEY";

    // Notification constants
    public static final int NOTIFICATION_ID = 168;
    private int lastShownNotificationId = -1;
    public static final String CHANNEL_ID = "dev.amitb.a24b_10234_finalproject.CHANNEL_ID_FOREGROUND";
    public static final String MAIN_ACTION = "dev.amitb.a24b_10234_finalproject.AirQualityService.action.main";

    // API details
    public static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String API_KEY = "e206f6a4b43cfb927c81f09fd9bc3dce";

    public NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunning = false;

    private FusedLocationProviderClient fusedLocationClient;
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    private int locationAQI = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle service start and stop actions
        if (intent == null || intent.getAction() == null) {
            stopForeground(true);
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (action.equals(START_FOREGROUND_SERVICE)) {
            if (isServiceRunning) {
                return START_STICKY;
            }
            isServiceRunning = true;
            notifyToUserForForegroundService();
            startSample();
        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            stopSample();
            stopForeground(true);
            stopSelf();
            isServiceRunning = false;
        }
        return START_STICKY;
    }

    // Location listener callback to process location updates
    private LocationListener locationCallback = location -> {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // Make API call to get air quality data
        AirQualityServiceCalls calls = RetrofitClient.getClient(BASE_URL).create(AirQualityServiceCalls.class);
        Call<AirQualityResponse> call = calls.getAirQuality(lat, lon, API_KEY);

        call.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                AirQualityResponse airQualityResponse = response.body();
                if (airQualityResponse.getList() != null) {
                    AirQualityResponse.Data data = airQualityResponse.getList().get(0);
                    if (data != null) {
                        locationAQI = data.getMain().getAqi();
                        updateNotification(locationAQI);
                        MyLoc myLoc = new MyLoc().setLat(lat).setLon(lon).setAqi(locationAQI);
                        String json = new Gson().toJson(myLoc);
                        Intent intent = new Intent(BROADCAST_LOCATION);
                        intent.putExtra(BROADCAST_LOCATION_KEY, json);
                        LocalBroadcastManager.getInstance(AirQualityService.this).sendBroadcast(intent);
                    } else {
                        Log.e("AirQualityService", "Null or empty data returned");
                    }
                } else {
                    Log.e("AirQualityService", "Empty data returned");
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                Log.e("AirQualityService", "Error: " + t.getMessage());
            }
        });
    };

    // Start location sampling
    private void startSample() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(5000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)
                    .setMinUpdateDistanceMeters(1.0f)
                    .build();

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    // Stop location sampling
    private void stopSample() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();

        if (fusedLocationClient != null) {
            Task<Void> task = fusedLocationClient.removeLocationUpdates(locationCallback);
            task.addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Log.d("AQI sensor stop", "stop LocationCallback removed.");
                    stopSelf();
                } else {
                    Log.d("AQI sensor stop", "stop Failed to remove Location Callback.");
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Create and show the foreground notification
    private void notifyToUserForForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW);

        notificationBuilder
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_air_quality)
                .setContentTitle("App in progress")
                .setContentText("AQI checking")
        ;

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    // Helper method to create notification builder
    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        prepareChannel(context, channelId, importance);
        builder = new NotificationCompat.Builder(context, channelId);
        return builder;
    }

    // Prepare notification channel for Android O and above
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Air Quality app location channel";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if (nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(notifications_channel_description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    // Update notification based on air quality index
    private void updateNotification(int index) {
        Icon image = null;
        if (index <= 50) {
            image = Icon.createWithResource(this, R.drawable.ic_smile);
        } else if (index <= 100) {
            image = Icon.createWithResource(this, R.drawable.ic_meh);
        } else if (index <= 200) {
            image = Icon.createWithResource(this, R.drawable.ic_frown);
        } else {
            image = Icon.createWithResource(this, R.drawable.ic_mad);
        }
        notificationBuilder.setLargeIcon(image);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}

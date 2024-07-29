package dev.amitb.a24b_10234_finalproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirQualityServiceCalls {
    @GET("air_pollution")
    Call<AirQualityResponse> getAirQuality(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );
}

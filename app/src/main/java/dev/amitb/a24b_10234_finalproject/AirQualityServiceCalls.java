package dev.amitb.a24b_10234_finalproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirQualityServiceCalls {
    /**
     * Retrieves the air quality index (AQI) for a given location.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @param apiKey    The API key for accessing the air quality service.
     * @return A Call object that can be used to make the request asynchronously.
     */
    @GET("air_pollution")
    Call<AirQualityResponse> getAirQuality(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );
}

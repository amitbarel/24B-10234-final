package dev.amitb.a24b_10234_finalproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Singleton instance of Retrofit
    private static Retrofit retrofit = null;

    /**
     * Returns a singleton Retrofit client instance.
     * Initializes the Retrofit client with the given base URL if it has not been created yet.
     *
     * @param baseUrl The base URL for the API endpoints.
     * @return The Retrofit client instance.
     */
    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

package dev.amitb.a24b_10234_finalproject;

import java.util.List;

public class AirQualityResponse {

    // List of air quality data objects
    private List<Data> list;

    public List<Data> getList() {
        return list;
    }

    public static class Data {
        // Main object containing AQI information
        private Main main;

        public Main getMain() {
            return main;
        }
    }

    public static class Main {
        // Air Quality Index value
        private int aqi;

        public int getAqi() {
            return aqi;
        }
    }
}


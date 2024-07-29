package dev.amitb.a24b_10234_finalproject;

import java.util.List;

public class AirQualityResponse {
    private List<Data> list;

    public List<Data> getList() {
        return list;
    }

    public static class Data {
        private Main main;

        public Main getMain() {
            return main;
        }
    }

    public static class Main {
        private int aqi;

        public int getAqi() {
            return aqi;
        }
    }
}


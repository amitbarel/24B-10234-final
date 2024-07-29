package dev.amitb.a24b_10234_finalproject;

public class MyLoc {

    private double lat, lon;
    private int aqi;

    public MyLoc(){}

    public double getLat() {
        return lat;
    }

    public MyLoc setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public double getLon() {
        return lon;
    }

    public MyLoc setLon(double lon) {
        this.lon = lon;
        return this;
    }

    public int getAqi() {
        return aqi;
    }

    public MyLoc setAqi(int aqi) {
        this.aqi = aqi;
        return this;
    }
}

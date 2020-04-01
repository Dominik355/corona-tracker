package com.example.coronatracker.models;

import java.util.Date;

public class LocationStats {

    private String state;
    private String country;
    private int latestCases;
    private int deltaCases;
    private int deaths;
    private int recovered;

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getRecovered() {
        return recovered;
    }

    public void setRecovered(int recovered) {
        this.recovered = recovered;
    }

    public int getDeltaCases() {
        return deltaCases;
    }

    public void setDeltaCases(int deltaCases) {
        this.deltaCases = deltaCases;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLatestCases() {
        return latestCases;
    }

    public void setLatestCases(int latestCases) {
        this.latestCases = latestCases;
    }

    @Override
    public String toString() {
        return "LocationStats{" +
                "state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", latestCases=" + latestCases +
                ", deltaCases=" + deltaCases +
                ", deaths=" + deaths +
                ", recovered=" + recovered +
                '}';
    }
}

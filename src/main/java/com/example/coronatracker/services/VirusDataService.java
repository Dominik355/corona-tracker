package com.example.coronatracker.services;

import com.example.coronatracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.stream.Location;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class VirusDataService {

    private static String[] europeStates = new String[]{
            "Albania","Andorra","Armenia","Azerbaijan","Belarus","Belgium"
            ,"Bosnia and Herzegovina","Bulgaria","Croatia","Cyprus","Denmark"
            ,"Estonia","Finland","France","Georgia","Germany","Greece","Iceland"
            ,"Ireland","Italy","Kosovo","Latvia","Liechtenstein","Lithuania","Luxembourg"
            ,"Nort Macedonia","Malta","Moldova","Monaco","Montenegro","Netherlands","Norway"
            ,"Portugal","Romania","Russia","San Marino","Serbia","Slovenia","Spain"
            ,"Sweden","Switzerland","Turkey","United Kingdom"};

    private static String[] mostImportant = new String[]{"Slovakia","Czechia","Austria","Hungary","Poland","Ukraine"};

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
    private static String DEATHS_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
    private static String RECOVERS_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";


    private String lastDate;
    private List<LocationStats> statsEurope = new ArrayList<>();
    private List<LocationStats> statsRest = new ArrayList<>();
    private List<LocationStats> statsImportant = new ArrayList<>();


    @PostConstruct
    @Scheduled(cron = "0 0 * * * ?")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<String> europe = Arrays.asList(europeStates);
        List<String> europeImporant = Arrays.asList(mostImportant);
        List<LocationStats> newStatsEuropeImportant = new ArrayList<>();
        List<LocationStats> newStatsEurope = new ArrayList<>();
        List<LocationStats> newStatsRest= new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpRequest requestDeaths = HttpRequest.newBuilder()
                .uri(URI.create(DEATHS_URL))
                .build();

        HttpResponse<String> responseDeaths = client.send(requestDeaths, HttpResponse.BodyHandlers.ofString());

        HttpRequest requestRecoveries = HttpRequest.newBuilder()
                .uri(URI.create(RECOVERS_URL))
                .build();

        HttpResponse<String> responseRecoveries = client.send(requestRecoveries, HttpResponse.BodyHandlers.ofString());

        Iterable<CSVRecord> recordsData = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new StringReader(response.body()));
        Iterable<CSVRecord> recordsDeaths = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new StringReader(responseDeaths.body()));
        Iterable<CSVRecord> recordsRecoveries = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new StringReader(responseRecoveries.body()));

        setLastDate(responseRecoveries.body());
        List<LocationStats> overall = new ArrayList<>();
        for (CSVRecord record : recordsData) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setLatestCases(latestCases);
            locationStat.setDeltaCases(latestCases - prevDayCases);
            overall.add(locationStat);
        }

        for( CSVRecord record : recordsDeaths) {
            String stateName = record.get("Province/State");
            String countryName = record.get("Country/Region");
            for(LocationStats stat : overall) {
                if(stat.getCountry().equals(countryName) && stat.getState().equals(stateName)) {
                    stat.setDeaths(Integer.parseInt(record.get(record.size() - 1)));
                }
            }
        }

        for( CSVRecord record : recordsRecoveries) {
            String stateName = record.get("Province/State");
            String countryName = record.get("Country/Region");
            for(LocationStats stat : overall) {
                if(stat.getCountry().equals(countryName) && stat.getState().equals(stateName)) {
                    stat.setRecovered(Integer.parseInt(record.get(record.size() - 1)));
                }
            }
        }

        for(LocationStats locationStat : overall) {
            if (europe.contains(locationStat.getCountry())) {
                newStatsEurope.add(locationStat);
            } else if (europeImporant.contains(locationStat.getCountry())) {
                newStatsEuropeImportant.add(locationStat);
            } else {
                newStatsRest.add(locationStat);
            }
        }

        Collections.sort(newStatsEuropeImportant, (o1, o2) -> o1.getCountry().compareTo(o2.getCountry()));
        Collections.sort(newStatsEurope, (o1, o2) -> o1.getCountry().compareTo(o2.getCountry()));
        Collections.sort(newStatsRest, (o1, o2) -> o1.getCountry().compareTo(o2.getCountry()));
        this.statsImportant = newStatsEuropeImportant;
        this.statsEurope = newStatsEurope;
        this.statsRest = newStatsRest;
    }

    public List<LocationStats> getStatsEurope() {
        return statsEurope;
    }

    public List<LocationStats> getStatsRest() {
        return statsRest;
    }

    public List<LocationStats> getStatsEuropeImportant() {
        return statsImportant;
    }

    public String getLastDate() {
        return this.lastDate;
    }

    private void setLastDate(String text) {
        int index = text.indexOf("\n");
        String subText = text.substring(0, index);
        String originalDate = subText.substring(subText.lastIndexOf(",")+1, subText.length());
        String[] params = originalDate.split("/");
        this.lastDate = params[1]+"."+params[0]+".20"+params[2];
    }

}

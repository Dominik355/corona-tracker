package com.example.coronatracker.controllers;

import com.example.coronatracker.models.LocationStats;
import com.example.coronatracker.services.VirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private VirusDataService virusDataService;

    @GetMapping("/")
    public String home(Model model)  {
        List<LocationStats> mostImportant = virusDataService.getStatsEuropeImportant();
        List<LocationStats> allStatsEurope = virusDataService.getStatsEurope();
        List<LocationStats> allStatsRest = virusDataService.getStatsRest();
        List<LocationStats> allStats = new ArrayList<>(allStatsEurope);
        allStats.addAll(allStatsRest);
        allStats.addAll(mostImportant);
        int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDeltaCases()).sum();
        int totalDeaths = allStats.stream().mapToInt(stat -> stat.getDeaths()).sum();
        int totalRecoveries = allStats.stream().mapToInt(stat -> stat.getRecovered()).sum();

        model.addAttribute("mostImportantStats", mostImportant);
        model.addAttribute("europeStats", allStatsEurope);
        model.addAttribute("restStats", allStatsRest);
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("totalNewCases", totalNewCases);
        model.addAttribute("totalRecoveries", totalRecoveries);
        model.addAttribute("totalDeaths", totalDeaths);
        model.addAttribute("lastDate", virusDataService.getLastDate());
        return "home";
    }

}

package com.example.facebook.adsscraper.controller;

import com.example.facebook.adsscraper.model.Ad;
import com.example.facebook.adsscraper.service.FacebookAdsScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FacebookAdsScraperController {

    @Autowired
    private FacebookAdsScrapingService facebookAdsScrapingService;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/scrape-ads")
    public ResponseEntity<Map<String, Object>> scrapeAds(
            @RequestParam(required = false) String phrase,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "1") int pageNumber,  // Default page number to 1 if not provided
            @RequestParam(defaultValue = "10") int pageSize) { // Default page size to 10 if not provided

        // Prepare the response map
        Map<String, Object> response = new HashMap<>();

        // Validate the phrase parameter
        if (phrase == null || phrase.trim().isEmpty()) {
            response.put("error", "Phrase is mandatory.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Validate the country parameter
        if (country == null || country.trim().isEmpty()) {
            response.put("error", "Country is mandatory.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Validate the pageNumber parameter
        if (pageNumber < 1) {
            response.put("error", "Page number must be greater than or equal to 1.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Validate the pageSize parameter
        if (pageSize < 1) {
            response.put("error", "Page size must be greater than or equal to 1.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            // Call the service to scrape ads
            List<Ad> adsList = facebookAdsScrapingService.scrapeAds(phrase, country, pageNumber, pageSize);

            // Check if there are more ads to load
            boolean hasMore = adsList.size() == pageSize;

            // Populate the response map with the ads data and pagination info
            response.put("ads", adsList);
            response.put("pageNumber", pageNumber);
            response.put("hasMore", hasMore);

            // Return the response entity with the ads data
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle any unexpected errors
            response.put("error", "Failed to scrape ads: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

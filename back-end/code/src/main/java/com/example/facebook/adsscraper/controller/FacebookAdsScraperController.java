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

@RestController // This tells Spring Boot that this class is a controller that can handle HTTP requests.
public class FacebookAdsScraperController {

    @Autowired // This is used to connect the service that scrapes the ads to this controller.
    private FacebookAdsScrapingService facebookAdsScrapingService;

    @CrossOrigin(origins = "http://localhost:3000") // This allows the frontend on localhost:3000 to talk to this backend.
    @GetMapping("/scrape-ads") // This tells the app to respond to a GET request at /scrape-ads.
    public ResponseEntity<Map<String, Object>> scrapeAds(
            @RequestParam(required = false) String phrase, // This is the search phrase parameter (can be empty).
            @RequestParam(required = false) String country, // This is the country parameter (can be empty).
            @RequestParam(defaultValue = "1") int pageNumber,  // This sets the page number to 1 if not provided.
            @RequestParam(defaultValue = "10") int pageSize) { // This sets the number of ads per page to 10 if not provided.

        Map<String, Object> response = new HashMap<>(); // This is the map where we store the response to send back.

        // Check if the search phrase is missing
        if (phrase == null || phrase.trim().isEmpty()) {
            response.put("error", "Phrase is mandatory."); // Add error message to response
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Send back error with a 400 status code.
        }

        // Check if the country is missing
        if (country == null || country.trim().isEmpty()) {
            response.put("error", "Country is mandatory."); // Add error message to response
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Send back error with a 400 status code.
        }

        // Check if pageNumber is less than 1
        if (pageNumber < 1) {
            response.put("error", "Page number must be greater than or equal to 1."); // Add error message to response
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Send back error with a 400 status code.
        }

        // Check if pageSize is less than 1
        if (pageSize < 1) {
            response.put("error", "Page size must be greater than or equal to 1."); // Add error message to response
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Send back error with a 400 status code.
        }

        try {
            // Call the service to scrape ads based on the given parameters
            List<Ad> adsList = facebookAdsScrapingService.scrapeAds(phrase, country, pageNumber, pageSize);

            // Check if there are more ads to load
            boolean hasMore = adsList.size() == pageSize;

            // Put the ads data and pagination info into the response map
            response.put("ads", adsList);
            response.put("pageNumber", pageNumber);
            response.put("hasMore", hasMore);

            // Return the response entity with the ads data and status 200 (OK)
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // If something goes wrong, send an error message with status 500 (Internal Server Error)
            response.put("error", "Failed to scrape ads: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

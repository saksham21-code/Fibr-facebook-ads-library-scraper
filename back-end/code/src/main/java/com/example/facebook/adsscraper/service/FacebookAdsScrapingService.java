package com.example.facebook.adsscraper.service;

import com.example.facebook.adsscraper.model.Ad;
import com.example.facebook.adsscraper.utils.WebElementUtils;
import com.example.facebook.adsscraper.utils.XPathUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class FacebookAdsScrapingService {

    // Injecting property values from application.properties
    @Value("${facebook.ads.library.url}")
    private String facebookAdsLibraryUrl;

    @Value("${xpath.country.dropdown}")
    private String xpathCountryDropdown;

    @Value("${xpath.country.search.input}")
    private String xpathCountrySearchInput;

    @Value("${xpath.country.option.prefix}")
    private String xpathCountryOptionPrefix;

    @Value("${xpath.ad.category.dropdown}")
    private String xpathAdCategoryDropdown;

    @Value("${xpath.all.ads.option}")
    private String xpathAllAdsOption;

    @Value("${xpath.search.input}")
    private String xpathSearchInput;

    @Value("${xpath.ad.container}")
    private String xpathAdContainer;

    @Value("${xpath.library.id}")
    private String xpathLibraryId;

    @Value("${xpath.account.name}")
    private String xpathAccountName;

    @Value("${xpath.description}")
    private String xpathDescription;

    @Value("${xpath.media.image}")
    private String xpathMediaImage;

    @Value("${xpath.media.video}")
    private String xpathMediaVideo;

    // Map stores the processed ads
    private Map<String, Set<String>> processedAdsMap = new HashMap<>();

    /*
     * Scrapes ads from Facebook Ads Library based on a search phrase and country.
     * @return A list of ads scraped from the Facebook Ads Library.
     */
    public List<Ad> scrapeAds(String searchPhrase, String country, int pageNumber, int pageSize) {
        WebDriver driver = null; // WebDriver object for automation
        List<Ad> adsList = new ArrayList<>(); // List to store scraped ads

        // Create a key based on phrase and country to track the processing
        String key = searchPhrase.toLowerCase() + "-" + country.toLowerCase();
        Set<String> processedLibraryIds = processedAdsMap.getOrDefault(key, new HashSet<>());

        try {
            /* 
             * Step 1: Setup the WebDriver and configure browser options.
             * Navigate to the Facebook Ads Library.
             */
            WebDriverManager.firefoxdriver().setup(); 
            FirefoxOptions options = new FirefoxOptions(); 
            driver = new FirefoxDriver(options); 
            driver.get(facebookAdsLibraryUrl); // Opens the Facebook Ads Library page
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50)); // Waits for 50 seconds.

            /* 
             * Step 2: Select the country, by typing it, select all ads, enter phrase, hit enter.
             */
            selectCountryAndSearchAds(driver, wait, country, searchPhrase);

            /* 
             * Step 3: Wait for the ads to load, handle pagination by scrolling.
             * Get the initial scroll height.
             */
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            // Step 4: Calculate the number of ads to skip based on the page number, continue scrolling till ads are loaded.
            // If no more ads are loaded, break the loop.
            // Updates the number of loaded ads.
            int adsToSkip = (pageNumber - 1) * pageSize;
            int totalAdsLoaded = 0;

            while (adsList.size() < pageSize) { 
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // Scrolls to the bottom of the page.
                Thread.sleep(2000); // Waits for 2 seconds to load.
                long newHeight = (long) js.executeScript("return document.body.scrollHeight"); // Gets the new scroll height.
                if (newHeight == lastHeight) { 
                    break; // Break if no new ads are loaded.
                }
                lastHeight = newHeight; 

                List<WebElement> ads = driver.findElements(By.xpath(xpathAdContainer)); // Finds all ad elements on the page.
                if (ads.size() == totalAdsLoaded) { // If no new ads, continue scrolling.
                    continue;
                }
                totalAdsLoaded = ads.size();

                // Step 5: Loop through each ad container
                // Skip ads based on the page number.
                // Stop only if the required number is loaded.
                for (WebElement adElement : ads) { 
                    if (adsToSkip > 0) { 
                        adsToSkip--;
                        continue;
                    }
                    if (adsList.size() >= pageSize) break; 

                    /* 
                     * Step 6: Extract ad details like Library ID, account name, description, title, and media.
                     * We are collecting the unique identifier i.e Library ID first.   
                     * If the ad is already processed, skip it; if not, add Library ID to the processed set.
                     */
                    try {
                        String libraryId = WebElementUtils.extractText(adElement, xpathLibraryId);
                        if (libraryId.isEmpty() || processedLibraryIds.contains(libraryId)) {
                            continue;
                        }
                        processedLibraryIds.add(libraryId); 

                        // Extract account name
                        String accountName = WebElementUtils.extractText(adElement, xpathAccountName);

                        // Extract description and title
                        String description = WebElementUtils.extractText(adElement, xpathDescription);
                        String title = WebElementUtils.extractTitleFromDescription(description);

                        // Extract media URL (image or video)
                        String mediaUrl = WebElementUtils.extractMediaUrl(adElement, xpathMediaImage, xpathMediaVideo);

                        // Extract social media links (Facebook and Instagram profile)
                        Map<String, String> socialMediaLinks = WebElementUtils.extractSocialMediaLinks(adElement); 
                        String fbLink = socialMediaLinks.get("facebook"); 
                        String igLink = socialMediaLinks.get("instagram"); 

                        // Store extracted information in the Ad model class
                        Ad ad = new Ad(title, description, mediaUrl, accountName, libraryId, fbLink, igLink);
                        adsList.add(ad);

                    } catch (Exception e) { 
                        log.error("Error processing ad: {}", e.getMessage(), e); 
                        continue; 
                    }
                }
            }

            // Step 7: Update the map with processed Library IDs.
            processedAdsMap.put(key, processedLibraryIds);

        } catch (Exception e) {
            log.error("An error occurred during scraping: {}", e.getMessage(), e); 
        } finally {
            if (driver != null) {
                driver.quit(); // Quitting session.
            }
        }

        // Return the list of ads.
        return adsList;
    }

    // Helper method for step 2: Select the country, by typing it, select all ads, enter phrase, hit enter.
    private void selectCountryAndSearchAds(WebDriver driver, WebDriverWait wait, String country, String searchPhrase) {
        try {
            WebElement countryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathCountryDropdown))); // Waits for the country dropdown to be clickable.
            countryDropdown.click();
            WebElement searchCountryInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathCountrySearchInput))); // Waits for the search input to be clickable.
            searchCountryInput.sendKeys(country); 
            searchCountryInput.sendKeys(Keys.ENTER); 
            WebElement countryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(XPathUtils.getCountryOptionXPath(country)))); // Waits for the country option to appear.
            countryOption.click(); 
            WebElement adCategoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathAdCategoryDropdown))); // Waits for the ad category dropdown.
            adCategoryDropdown.click(); 
            WebElement allAdsOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathAllAdsOption))); // Waits for the 'All ads' option.
            allAdsOption.click(); 
            WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathSearchInput))); // Waits for the search input.
            searchInput.sendKeys(searchPhrase); 
            searchInput.sendKeys(Keys.ENTER); 
        } catch (Exception e) {
            log.error("Error selecting country and searching ads: {}", e.getMessage(), e);
        }
    }
}

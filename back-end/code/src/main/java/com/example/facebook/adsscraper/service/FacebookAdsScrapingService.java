package com.example.facebook.adsscraper.service;

import com.example.facebook.adsscraper.model.Ad;
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
    @Value("${webdriver.browser.path}")
    private String webDriverPath;

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

    // map stores the processed ads
    private Map<String, Set<String>> processedAdsMap = new HashMap<>();

    /*
     * Scrapes ads from Facebook Ads Library based on a search phrase and country.
     * @return A list of ads scraped from the Facebook Ads Library.
     */
    public List<Ad> scrapeAds(String searchPhrase, String country, int pageNumber, int pageSize) {
        WebDriver driver = null; // WebDriver object { automation } 
        List<Ad> adsList = new ArrayList<>(); // List of ads.

        // create a key on phrase and country to track the processing
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
            driver.get(facebookAdsLibraryUrl); // Opens page.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50)); // Waits to 50 seconds.

            /* 
             * Step 2: Select the country, by typing it, select all ads, enter phrase, hit enter.
             */
            selectCountryAndSearchAds(driver, wait, country, searchPhrase);

            /* 
             * Step 3: Wait for the ads to load, handle pagination by scrolling.
             *    get the initial scroll height.
             */
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            // Step 4: Calculate the number of ads to skip based on the page number, continue scrolling till ads are loaded.
            // If no more ads are loaded, break the loop.
            // Updates number of loaded ads.
            int adsToSkip = (pageNumber - 1) * pageSize;
            int totalAdsLoaded = 0;

            while (adsList.size() < pageSize) { 
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // Scrolls to the bottom of the page.
                Thread.sleep(2000); // Waits for 2 seconds to load.
                long newHeight = (long) js.executeScript("return document.body.scrollHeight"); // Gets the new scroll height.
                if (newHeight == lastHeight) { 
                    break;     // handles hasMore attribute making sure you do not continue further.
                }
                lastHeight = newHeight; 

                List<WebElement> ads = driver.findElements(By.xpath(xpathAdContainer)); // Finds all ad elements on the page.
                if (ads.size() == totalAdsLoaded) { // If no new ads, continue scrolling.
                    continue;
                }
                totalAdsLoaded = ads.size();

                // Step 5: Loop through each ad container
                // Skip ads based on the page number.
                // Stop only if the required no. is loaded.
                for (WebElement adElement : ads) { 
                    if (adsToSkip > 0) { 
                        adsToSkip--;
                        continue;
                    }
                    if (adsList.size() >= pageSize) break; 

                    /* 
                     * Step 6: Extract ad details like Library ID, account name, description, title, and media.
                     *    we are collecting the unique identifier i.e Library ID first.   
                     *   If the ad is already processed, skip it IF NOT add Library ID to the processed set.
                     */
                    try {
                        String libraryId = extractLibraryId(adElement);
                        if (libraryId.isEmpty() || processedLibraryIds.contains(libraryId)) {
                            continue;
                        }
                        processedLibraryIds.add(libraryId); 

                        // name extraction
                        String accountName = extractAccountName(adElement);

                        // description and title extraction
                        String description = extractDescription(adElement);
                        String title = extractTitleFromDescription(description);

                        // media URL (image or video) extraction
                        String mediaUrl = extractMediaUrl(adElement);

                        // social media links (Facebook and Instagram profile) extraction
                        Map<String, String> socialMediaLinks = extractSocialMediaLinks(adElement); 
                        String fbLink = socialMediaLinks.get("facebook"); 
                        String igLink = socialMediaLinks.get("instagram"); 

                        // till now extracted all information to be stored in the Ad model class
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
            WebElement countryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathCountryOptionPrefix + country + "']"))); // Waits for the country option to appear.
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

    // Helper method to extract Library ID.
    private String extractLibraryId(WebElement adElement) {
        try {
            return adElement.findElement(By.xpath(xpathLibraryId)).getText(); 
        } catch (NoSuchElementException e) { 
            return ""; 
        }
    }

    // Helper method to extract Account Name.
    private String extractAccountName(WebElement adElement) {
        try {
            return adElement.findElement(By.xpath(xpathAccountName)).getText(); 
        } catch (NoSuchElementException e) { 
            log.warn("Account name not found for ad."); 
            return "Unknown"; 
        }
    }

    // Helper method to extract Description.
    private String extractDescription(WebElement adElement) {
        try {
            WebElement descriptionElement = adElement.findElement(By.xpath(xpathDescription)); 
            String description = descriptionElement.getAttribute("innerHTML").replace("<br>", "\n"); 
            return description.replaceAll("\\<.*?\\>", ""); 
        } catch (NoSuchElementException e) { 
            log.warn("Description not found for ad."); 
            return ""; 
        }
    }

    // Helper method to extract Title from Description.
    private String extractTitleFromDescription(String description) {
        String[] descriptionLines = description.split("\n"); 
        return descriptionLines.length > 0 ? descriptionLines[0] : ""; 
    }

    // Helper method to extract Media URL (Image or Video).
    private String extractMediaUrl(WebElement adElement) {
        try {
            return adElement.findElement(By.xpath(xpathMediaImage)).getAttribute("src"); 
        } catch (NoSuchElementException imgEx) { 
            try {
                return adElement.findElement(By.xpath(xpathMediaVideo)).getAttribute("src");
            } catch (NoSuchElementException vidEx) { 
                log.warn("No media (image or video) found for ad."); 
                return ""; 
            }
        }
    }

    // Helper method to extract the social links (Facebook and Instagram).
    private Map<String, String> extractSocialMediaLinks(WebElement adElement) {
        List<WebElement> links = adElement.findElements(By.tagName("a")); 
        String facebookLink = null; 
        String instagramLink = null; 
        for (WebElement link : links) { 
            String href = link.getAttribute("href");
            if (href.contains("facebook.com") && facebookLink == null) { 
                if (isProfileLink(href)) { 
                    facebookLink = href; 
                }
            } else if (href.contains("instagram.com") && instagramLink == null) { 
                if (isProfileLink(href)) { 
                    instagramLink = href; 
                }
            }
        }

        Map<String, String> socialMediaLinks = new HashMap<>(); 
        socialMediaLinks.put("facebook", facebookLink); 
        socialMediaLinks.put("instagram", instagramLink); 

        return socialMediaLinks; 
    }

    // If the link doesn't contain "posts", "photos", or "videos", it's considered a profile link.
    private boolean isProfileLink(String href) {
        return !href.contains("/posts/") && !href.contains("/photos/") && !href.contains("/videos/");
    }
}

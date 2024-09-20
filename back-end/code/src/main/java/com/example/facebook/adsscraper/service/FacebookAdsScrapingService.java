package com.example.facebook.adsscraper.service;

import com.example.facebook.adsscraper.model.Ad;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class FacebookAdsScrapingService {

   //  map stores the processed ads
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
        driver.get("https://www.facebook.com/ads/library/"); // Opens  page.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50)); // Waits to 50 seconds.

        /* 
         * Step 2: Select the country , by typing it , select all ads , enter phrase , hit enter.
         */
        WebElement countryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='js_7']"))); // Waits for the country dropdown to be clickable.
        countryDropdown.click();
        WebElement searchCountryInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Search for country']"))); // Waits for the search input to be clickable.
        searchCountryInput.sendKeys(country); 
        searchCountryInput.sendKeys(Keys.ENTER); 
        WebElement countryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='" + country + "']"))); // Waits for the country option to appear.
        countryOption.click(); 
        WebElement adCategoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Ad category']"))); // Waits for the ad category dropdown.
        adCategoryDropdown.click(); 
        WebElement allAdsOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='All ads']"))); // Waits for the 'All ads' option.
        allAdsOption.click(); 
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Search by keyword or advertiser']"))); // Waits for the search input.
        searchInput.sendKeys(searchPhrase); 
        searchInput.sendKeys(Keys.ENTER); 

        /* 
         * Step 3: Wait for the ads to load, handle pagination by scrolling.
         *    get the initial scroll height.
         */
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'xh8yej3')]")));
        // JavaScript Executor for scrolling 
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long lastHeight = (long) js.executeScript("return document.body.scrollHeight"); .

        
        // Step 4 : Calculate the number of ads to skip based on the page number, conntinue scrolling till ads are loaded.
        // If no more ads are loaded, break the loop.
        // Updates number of loaded ads.
        int adsToSkip = (pageNumber - 1) * pageSize;
        int totalAdsLoaded = 0;

        while (adsList.size() < pageSize) { 
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // Scrolls to the bottom of the page.
            Thread.sleep(2000); // Waits for 2 seconds to load.
            long newHeight = (long) js.executeScript("return document.body.scrollHeight"); // Gets the new scroll height.
            if (newHeight == lastHeight) { 
                break;
            }
            lastHeight = newHeight; 

            List<WebElement> ads = driver.findElements(By.xpath("//div[contains(@class, 'xh8yej3')]")); // Finds all ad elements on the page.
            if (ads.size() == totalAdsLoaded) { // If no new ads, continue scrolling.
                continue;
            }
            totalAdsLoaded = ads.size();

            //Step 5:  Loop through each ad container
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
                    String libraryId = ""; .
                    try {
                        libraryId = adElement.findElement(By.xpath(".//span[contains(text(), 'Library ID')]")).getText(); 
                    } catch (org.openqa.selenium.NoSuchElementException e) { 
                        continue;
                    }
                    if (processedLibraryIds.contains(libraryId)) {
                        continue;
                    }
                    processedLibraryIds.add(libraryId); 

                    // name extraction
                    String accountName = "Unknown"; 
                    try {
                        accountName = adElement.findElement(By.xpath(".//a[@target='_blank']")).getText(); 
                    } catch (org.openqa.selenium.NoSuchElementException e) { 
                        log.warn("Account name not found for ad: {}", libraryId);
                    }

                    // description and title extraction
                    String description = ""; 
                    try {
                        WebElement descriptionElement = adElement.findElement(By.xpath(".//div[@style='white-space: pre-wrap;']//span")); 
                        description = descriptionElement.getAttribute("innerHTML").replace("<br>", "\n"); 
                        description = description.replaceAll("\\<.*?\\>", ""); 
                    } catch (org.openqa.selenium.NoSuchElementException e) { 
                        log.warn("Description not found for ad: {}", libraryId);
                    }
                    String[] descriptionLines = description.split("\n"); 
                    String title = descriptionLines.length > 0 ? descriptionLines[0] : ""; 

                    // media URL (image or video) extraction
                    String mediaUrl = "";.
                    try {
                        mediaUrl = adElement.findElement(By.xpath(".//img[@class='x1ll5gia x19kjcj4 xh8yej3']")).getAttribute("src"); 
                    } catch (org.openqa.selenium.NoSuchElementException imgEx) { 
                        try {
                            mediaUrl = adElement.findElement(By.xpath(".//video")).getAttribute("src");
                        } catch (org.openqa.selenium.NoSuchElementException vidEx) { 
                            log.warn("No media (image or video) found for ad: {}", libraryId);
                        }
                    }

                    // social media links (Facebook and Instagram profile) extraction
                    Map<String, String> socialMediaLinks = extractSocialMediaLinks(adElement); 
                    String fbLink = socialMediaLinks.get("facebook"); 
                    String igLink = socialMediaLinks.get("instagram"); 


                    //till now extracted all information to be stored in the Ad model class
                    Ad ad = new Ad(title, description, mediaUrl, accountName, libraryId, fbLink, igLink);
                    adsList.add(ad);

                } catch (Exception e) { 
                    log.error("Error processing ad: {}", e.getMessage(), e); 
                    continue; 
                }
            }
        }

        // Step 7 : Update the map with processed Library IDs.
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

    // helper method to extract the social links ( Facebook and Instagram )
    // firstly stores all the anchor tags ,Get the href attribute of the link.
    // Check if it's a Facebook link or Instagram link
    // Check if it's a profile link.
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
//// If the link doesn't contain "posts", "photos", or "videos", it's considered a profile link.
   private boolean isProfileLink(String href) {
       return !href.contains("/posts/") && !href.contains("/photos/") && !href.contains("/videos/");
}

        
}

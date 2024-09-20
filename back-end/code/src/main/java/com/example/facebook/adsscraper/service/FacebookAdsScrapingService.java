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

    private Map<String, Set<String>> processedAdsMap = new HashMap<>();

    public List<Ad> scrapeAds(String searchPhrase, String country, int pageNumber, int pageSize) {
        WebDriver driver = null;
        List<Ad> adsList = new ArrayList<>();

        String key = searchPhrase.toLowerCase() + "-" + country.toLowerCase();
        Set<String> processedLibraryIds = processedAdsMap.getOrDefault(key, new HashSet<>());

        try {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            driver = new FirefoxDriver(options);

            driver.get("https://www.facebook.com/ads/library/");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

            // Select Country
            WebElement countryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='js_7']")));
            countryDropdown.click();

            WebElement searchCountryInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Search for country']")));
            searchCountryInput.sendKeys(country);
            searchCountryInput.sendKeys(Keys.ENTER);

            WebElement countryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='" + country + "']")));
            countryOption.click();

            // Select Ad Category
            WebElement adCategoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[text()='Ad category']")));
            adCategoryDropdown.click();

            WebElement allAdsOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='All ads']")));
            allAdsOption.click();

            // Enter Search Phrase
            WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Search by keyword or advertiser']")));
            searchInput.sendKeys(searchPhrase);
            searchInput.sendKeys(Keys.ENTER);

            // Wait for Ads to Load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'xh8yej3')]")));

            // Handle scrolling to load more ads
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            int adsToSkip = (pageNumber - 1) * pageSize;
            int totalAdsLoaded = 0;

            while (adsList.size() < pageSize) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000);

                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;

                List<WebElement> ads = driver.findElements(By.xpath("//div[contains(@class, 'xh8yej3')]"));

                if (ads.size() == totalAdsLoaded) {
                    continue;
                }

                totalAdsLoaded = ads.size();

                for (WebElement adElement : ads) {
                    if (adsToSkip > 0) {
                        adsToSkip--;
                        continue;
                    }

                    if (adsList.size() >= pageSize) break;

                    try {
                        // Extract Library ID
                        String libraryId = "";
                        try {
                            libraryId = adElement.findElement(By.xpath(".//span[contains(text(), 'Library ID')]")).getText();
                        } catch (org.openqa.selenium.NoSuchElementException e) {
                            continue;
                        }

                        // Skip if duplicate Library ID
                        if (processedLibraryIds.contains(libraryId)) {
                            continue;
                        }

                        processedLibraryIds.add(libraryId);

                        // Extract account name
                        String accountName = "Unknown";
                        try {
                            accountName = adElement.findElement(By.xpath(".//a[@target='_blank']")).getText();
                        } catch (org.openqa.selenium.NoSuchElementException e) {
                            log.warn("Account name not found for ad: {}", libraryId);
                        }

                        // Extract ad description, title, and other elements
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

                        // Improved media extraction (try both image and video)
                        String mediaUrl = "";
                        try {
                            mediaUrl = adElement.findElement(By.xpath(".//img[@class='x1ll5gia x19kjcj4 xh8yej3']")).getAttribute("src");
                        } catch (org.openqa.selenium.NoSuchElementException imgEx) {
                            try {
                                mediaUrl = adElement.findElement(By.xpath(".//video")).getAttribute("src");
                            } catch (org.openqa.selenium.NoSuchElementException vidEx) {
                                log.warn("No media (image or video) found for ad: {}", libraryId);
                            }
                        }

                        // Extract social media links (Facebook and Instagram profile links)
                        Map<String, String> socialMediaLinks = extractSocialMediaLinks(adElement);
                        String fbLink = socialMediaLinks.get("facebook");
                        String igLink = socialMediaLinks.get("instagram");

                        // Create Ad object
                        Ad ad = new Ad(title, description, mediaUrl, accountName, libraryId, fbLink, igLink);

                        // Add to list
                        adsList.add(ad);

                    } catch (Exception e) {
                        log.error("Error processing ad: {}", e.getMessage(), e);
                        continue;
                    }
                }
            }

            processedAdsMap.put(key, processedLibraryIds);

        } catch (Exception e) {
            log.error("An error occurred during scraping: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return adsList;
    }

    // Extract social media links for Facebook and Instagram
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

    // Check if the href link is a profile link (not a post link)
    private boolean isProfileLink(String href) {
        return !href.contains("/posts/") && !href.contains("/photos/") && !href.contains("/videos/");
    }
}

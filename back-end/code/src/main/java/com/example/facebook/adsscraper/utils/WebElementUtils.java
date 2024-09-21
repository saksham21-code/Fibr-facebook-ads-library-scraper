package com.example.facebook.adsscraper.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebElementUtils {
    private static final Logger log = LoggerFactory.getLogger(WebElementUtils.class);

    // Extracts text from a WebElement using the provided XPath
    public static String extractText(WebElement element, String xpath) {
        try {
            return element.findElement(By.xpath(xpath)).getText();
        } catch (NoSuchElementException e) {
            log.warn("Element not found with XPath: {}", xpath);
            return "";
        }
    }

    // Extracts attribute value from a WebElement using the provided XPath and attribute name
    public static String extractAttribute(WebElement element, String xpath, String attribute) {
        try {
            return element.findElement(By.xpath(xpath)).getAttribute(attribute);
        } catch (NoSuchElementException e) {
            log.warn("Element not found with XPath: {}", xpath);
            return "";
        }
    }

    // Extracts social media links (Facebook and Instagram) from an ad element
    public static Map<String, String> extractSocialMediaLinks(WebElement adElement) {
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

    // Helper method to check if a link is a profile link
    private static boolean isProfileLink(String href) {
        return !href.contains("/posts/") && !href.contains("/photos/") && !href.contains("/videos/");
    }

    // Extracts the first line as title from description
    public static String extractTitleFromDescription(String description) {
        String[] descriptionLines = description.split("\n");
        return descriptionLines.length > 0 ? descriptionLines[0] : "";
    }

    // Extracts the media URL (Image or Video) from an ad element
    public static String extractMediaUrl(WebElement adElement, String xpathMediaImage, String xpathMediaVideo) {
        try {
            return extractAttribute(adElement, xpathMediaImage, "src");
        } catch (NoSuchElementException imgEx) {
            try {
                return extractAttribute(adElement, xpathMediaVideo, "src");
            } catch (NoSuchElementException vidEx) {
                log.warn("No media (image or video) found for ad.");
                return "";
            }
        }
    }
}


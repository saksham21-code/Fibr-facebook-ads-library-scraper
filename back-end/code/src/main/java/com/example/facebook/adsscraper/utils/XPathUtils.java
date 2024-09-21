package com.example.facebook.adsscraper.utils;

public class XPathUtils {

    // dynamically generate the country option XPath based on country name
    public static String getCountryOptionXPath(String country) {
        return "//div[text()='" + country + "']";
    }

}


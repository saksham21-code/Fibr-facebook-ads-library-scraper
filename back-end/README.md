
# Facebook Ads Scraper Backend API

This repository contains the backend API for scraping Facebook ads using Selenium and providing the scraped data through a RESTful API. The API allows you to search and paginate through Facebook ads based on a search phrase and country.

## Table of Contents

1. [Introduction](#introduction)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Setup and Installation](#setup-and-installation)
5. [API Endpoints](#api-endpoints)
   - [GET /scrape-ads](#get-scrape-ads)
6. [Data Model](#data-model)
7. [Scraping Logic](#scraping-logic)
8. [Error Handling](#error-handling)
9. [Logging](#logging)
10. [Future Enhancements](#future-enhancements)

## Introduction

The Facebook Ads Scraper Backend API is designed to scrape ads from the Facebook Ads Library. It allows users to search for ads based on a specific phrase and country. The results are paginated, and the API provides functionality to retrieve ads from multiple pages.

## Technology Stack

- **Java 17**: Primary language for the backend.
- **Spring Boot**: Framework for building the RESTful API.
- **Selenium WebDriver**: For scraping the Facebook Ads Library.
- **Maven**: Dependency management and build tool.
- **Lombok**: To reduce boilerplate code.
- **Log4j**: Logging framework.

## Project Structure

```
facebook-adsscraper-backend/
│
├── src/main/java/com/example/facebook/adsscraper/
│   ├── controller/
│   │   └── FacebookAdsScraperController.java     # API Controller for scraping ads
│   │
│   ├── model/
│   │   └── Ad.java                               # Model representing a Facebook ad
│   │
│   ├── service/
│   │   └── FacebookAdsScrapingService.java       # Service containing scraping logic
│   │
│   └── Application.java                          # Main entry point of the Spring Boot application
│
└── pom.xml                                       # Maven configuration file
```

## Setup and Installation

1. **Install dependencies:**
   Make sure you have Maven and Java 11 installed. Then run:
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`.

3. **Configure WebDriver:**
   - The service uses Firefox WebDriver to scrape the ads.
   - Ensure that the [GeckoDriver](https://github.com/mozilla/geckodriver/releases) is installed and added to your system path.

## API Endpoints

### GET /scrape-ads

**Description:**
Scrapes Facebook ads based on a search phrase and country, with pagination support.

**Endpoint:**
```
GET /scrape-ads
```

**Query Parameters:**
- `phrase` (string, required): The search phrase for ads.
- `country` (string, required): The country to search ads in.
- `pageNumber` (integer, optional): The page number to retrieve (default: 1).
- `pageSize` (integer, optional): The number of ads to retrieve per page (default: 10).

**Response:**
- **200 OK**: Returns a JSON object with ads data and pagination info.
- **400 Bad Request**: If the phrase or country is missing or invalid.
- **500 Internal Server Error**: If scraping fails or any other error occurs.

**Example Request:**
```
GET http://localhost:8080/scrape-ads?phrase=smartphone&country=India&pageNumber=1&pageSize=10
```

**Example Response:**
```json
{
  "ads": [
    {
      "title": "Amazing Smartphone",
      "description": "Buy now with a discount!",
      "media": "https://example.com/image.jpg",
      "accountName": "Tech Store",
      "libraryID": "1234567890",
      "fbLink": "https://facebook.com/techstore",
      "igLink": "https://instagram.com/techstore"
    }
  ],
  "pageNumber": 1,
  "hasMore": true
}
```

## Data Model

### Ad.java

Represents a Facebook advertisement scraped from the library.

- `String title`: Title of the ad.
- `String description`: Description of the ad.
- `String media`: Media link (image or video).
- `String accountName`: Name of the Facebook account.
- `String libraryID`: Unique identifier for the ad in the library.
- `String fbLink`: Facebook link for the ad or account.
- `String igLink`: Instagram link for the ad or account.

## Scraping Logic

The scraping logic is implemented in `FacebookAdsScrapingService.java` using Selenium WebDriver:

1. **Setup WebDriver**: Initializes Firefox WebDriver.
2. **Navigate to Facebook Ads Library**: Loads the Facebook Ads Library page.
3. **Select Country**: Chooses the country from the dropdown.
4. **Select Ad Category**: Chooses 'All Ads' as the ad category.
5. **Search by Phrase**: Enters the search phrase and initiates the search.
6. **Scroll and Load Ads**: Scrolls the page to load more ads.
7. **Extract Ad Information**: Retrieves ad details such as title, description, media, and social links.
8. **Pagination Handling**: Skips ads based on page number and retrieves the required page.

## Error Handling

- **Validation Errors**: If mandatory parameters (phrase, country) are missing or invalid, the API returns a `400 Bad Request` with an error message.
- **Scraping Errors**: If scraping fails, the API returns a `500 Internal Server Error` with an appropriate error message.
- **Empty Results**: If no ads are found, the response will contain an empty `ads` array with `hasMore` set to `false`.

## Logging

- Logging is handled using the `@Slf4j` annotation from Lombok.
- Logs are generated for important events like starting the scraping process, errors encountered, and key milestones.

## Future Enhancements

- **Additional Filters**: Add support for filtering ads based on date, impressions, and spend.
- **Headless Browser Support**: Implement headless browser scraping to improve performance and resource usage.

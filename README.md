
# Facebook Ads Scraper

## 1. Introduction
This project is a web application designed to scrape ads from the Facebook Ads Library based on user input criteria such as search phrases and country names. It consists of a backend API built using Spring Boot and a frontend application developed with ReactJS. The application provides real-time ad retrieval with pagination, dark/light themes, and error handling.

## 2. Tech Stack
- **Backend:** Java, Spring Boot, Selenium WebDriver, Maven, Lombok, Log4j
- **Frontend:** ReactJS, Tailwind CSS, JavaScript (ES6)

## 3. API Responses
Here are some sample API responses from the Facebook Ads Scraper API:

### API Endpoint
![API URL](images/api_url.png)

### API Response 
![API Response 1](images/api_response_1.png)

### API Response (with hasMore and pageNumber attribute)
![API Response 2](images/api_response_2.png)

## 4. Frontend Screenshots
The following images showcase the frontend UI in both light and dark themes.

### Search Page - Light Theme
![Search Page Light Theme](images/search_page_light_theme.png)

### Search Page - Dark Theme
![Search Page Dark Theme](images/search_page_dark_theme.png)

### Result Page - Light Theme
![Result Page Light Theme](images/result_page_light_theme.png)

### Result Page - Dark Theme
![Result Page Dark Theme](images/result_page_dark_theme.png)

## 5. Error Handling
When there are no ads found for a given search, the application provides a user-friendly error message:

### No Ads Found
![Error Handling](images/error_handling.png)

## 6. Application Flow
1. **User Input:** Users enter a search phrase and select a country on the search page.
2. **API Request:** The application sends a request to the backend API with the search criteria.
3. **Data Retrieval:** The backend scrapes the Facebook Ads Library and retrieves ads based on the input.
4. **Result Display:** The frontend displays the ads in a paginated format, allowing users to switch between pages.
5. **Error Handling:** If no ads are found, an error message is displayed.

## 7. Caching Mechanism
- The application uses a simple caching mechanism to store previously fetched ads in the frontend state. This prevents redundant API calls when users navigate back and forth between pages.

## 8. Demo
To see a demonstration of the project in action, check out the following videos:

- [API Testing Demo](demo/API_testing.mp4)
- [Frontend Testing Demo](demo/front_end_testing.mp4)

## 9. Contact
For any queries or issues, please contact:

- **Name:** Saksham Tiwari
- **Email:** saksham@example.com
- **GitHub:** [saksham21-code](https://github.com/saksham21-code)

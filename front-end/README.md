
# Facebook Ads Scraper

This project is a **ReactJS** application that allows users to search and analyze Facebook ads in real-time based on a search phrase and country. It includes two main components: the **SearchPage** and the **ResultsPage**. The **SearchPage** allows users to enter their search criteria, while the **ResultsPage** displays the results in a paginated manner. The application supports both light and dark themes.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [File Structure](#file-structure)
- [Setup Instructions](#setup-instructions)
- [Application Flow](#application-flow)
- [Detailed Component Description](#detailed-component-description)
  - [SearchPage](#searchpage)
  - [ResultsPage](#resultspage)
- [Error Handling](#error-handling)
- [Caching Mechanism](#caching-mechanism)
- [Dark and Light Theme](#dark-and-light-theme)
- [Pagination](#pagination)

## Features

- **Real-Time Facebook Ads Scraping:** Fetches Facebook ads in real-time based on the search phrase and country.
- **Search Functionality:** Allows users to search for ads based on a keyword and country.
- **Theme Toggle:** Supports both light and dark themes with a toggle button.
- **Pagination:** Enables users to navigate through the pages of ads using Next and Previous buttons.
- **Caching Mechanism:** Caches previously fetched ads to avoid redundant API calls.
- **Error Handling:** Displays user-friendly messages when no ads are found or when an error occurs during the API call.

## Tech Stack

- **ReactJS:** Frontend framework used to build the user interface.
- **Tailwind CSS:** Utility-first CSS framework used for styling the components.
- **JavaScript (ES6):** Programming language used to build the functionality.

## Setup Instructions

1. Start the development server.
   ```bash
   npm start
   ```
2. Open your browser and go to `http://localhost:3000` to view the application.

## Application Flow

1. The user navigates to the **SearchPage** where they can enter a search phrase and select a country from a dropdown menu.
2. On clicking the "Search" button, the user is redirected to the **ResultsPage** with the search criteria passed via `state`.
3. The **ResultsPage** fetches ads from the backend API based on the search criteria and displays them in a grid format.
4. Users can navigate between pages using the "Next" and "Previous" buttons. The app caches the results of each page to avoid redundant API calls.
5. If no ads are found or if an error occurs, a user-friendly message is displayed.

## Detailed Component Description

### SearchPage

- **Purpose:** Allows users to enter a search phrase and select a country to search for Facebook ads.
- **Features:**
  - A text input for the search phrase.
  - A searchable dropdown to select the country.
  - A search button that navigates to the ResultsPage with the search criteria.
  - Theme toggle button to switch between light and dark themes.
  

### ResultsPage

- **Purpose:** Displays the results of the Facebook ads search in a paginated manner.
- **Features:**
  - Fetches ads from the backend API based on the search criteria.
  - Displays ads in a grid format with details such as account name, library ID, title, and media.
  - Supports pagination with "Next" and "Previous" buttons.
  - Caches results for each page to reduce redundant API calls.
  - Theme toggle button to switch between light and dark themes.
  - Displays user-friendly messages if no ads are found or if an error occurs.

## Error Handling

- If the search inputs are incomplete, an error message is displayed on the **SearchPage**.
- If no ads are found or if an error occurs during the API call on the **ResultsPage**, a user-friendly message is displayed to inform the user.

## Caching Mechanism

- The app uses a caching mechanism to store the ads fetched for each page in the `adsCache` state. This prevents redundant API calls when the user navigates back to a previously visited page.

## Dark and Light Theme

- The application supports both light and dark themes.
- Users can toggle between the themes using the "Light Theme / Dark Theme" button in the header of both the **SearchPage** and **ResultsPage**.

## Pagination

- The **ResultsPage** supports pagination with "Next" and "Previous" buttons.
- The "Next" button is enabled only if there are more ads to fetch.
- The "Previous" button is enabled only if the current page is greater than 1.



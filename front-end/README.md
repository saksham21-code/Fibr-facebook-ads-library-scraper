
# Facebook Ads Scraper

This project is a **ReactJS** application that allows users to search and analyze Facebook ads in real-time based on a search phrase and country. The app includes two main components: the **SearchPage** and the **ResultsPage**. The **SearchPage** allows users to enter their search criteria, while the **ResultsPage** displays the results in a paginated manner. The application supports both light and dark themes.

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
- [How to Use](#how-to-use)
- [Conclusion](#conclusion)

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

## File Structure

```bash
├── src
│   ├── components
│   │   ├── SearchPage.js
│   │   ├── ResultsPage.js
│   ├── assets
│   │   └── countries.js
│   ├── App.js
│   ├── index.js
│   └── styles.css
├── public
│   ├── index.html
│   └── favicon.ico
└── README.md

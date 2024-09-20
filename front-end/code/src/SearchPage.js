import React, { useState, useEffect } from 'react';
import { countries } from './countries'; // Import the list of countries
import { useNavigate } from 'react-router-dom'; // Import navigation hook

function SearchPage() {
  // These are the states that will hold the values for the search phrase, selected country, and theme
  const [phrase, setPhrase] = useState('');
  const [country, setCountry] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [isDropdownOpen, setDropdownOpen] = useState(false); // To show or hide the country dropdown
  const [theme, setTheme] = useState('dark'); // The theme of the page, dark or light
  const [error, setError] = useState(''); // For showing error messages

  const navigate = useNavigate(); // This hook lets us go to other pages

  // This function changes the theme between light and dark
  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    document.documentElement.classList.toggle('dark'); // Applies dark mode to the whole page
  };

  // This useEffect makes sure the correct theme is applied when the page loads
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  // This function checks if the user has entered both a phrase and a country before searching
  const handleSearch = () => {
    if (!phrase.trim() || !country.trim()) {
      setError('Please enter both a search phrase and a country.');
      return;
    }

    // If both are entered, it goes to the results page and sends the phrase and country
    navigate('/results', { state: { phrase: phrase.trim(), country: country.trim(), resetPage: true } });
  };

  // This filters the list of countries based on what the user types
  const filteredCountries = countries.filter((country) =>
    country.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // This function is called when a country is selected from the dropdown
  const handleCountrySelect = (selectedCountry) => {
    setCountry(selectedCountry);
    setSearchTerm(selectedCountry); // Sets the searchTerm to the selected country
    setDropdownOpen(false); // Closes the dropdown
    setError(''); // Clears any error messages
  };

  return (
    <div className={`flex flex-col min-h-screen ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
      {/* This is the header section at the top */}
      <header className="w-full p-4 bg-blue-600 shadow-md dark:bg-gray-800">
        <div className="flex items-center justify-between max-w-6xl mx-auto">
          <div className="flex-grow text-center">
            <h1 className="text-3xl font-bold text-white dark:text-white"> {/* The main title of the page */}
              Facebook Ads Scraper
            </h1>
            <p className="mt-1 text-lg text-white dark:text-gray-300"> {/* A short description below the title */}
              Search and analyze Facebook ads in real time
            </p>
          </div>
          {/* Button to change the theme between light and dark */}
          <button
            onClick={toggleTheme}
            className="px-3 py-2 text-white bg-gray-600 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            {theme === 'dark' ? 'Light theme' : 'Dark theme'}
          </button>
        </div>
      </header>

      {/* Main content section with search form */}
      <main className="flex items-center justify-center flex-grow my-8">
        <div className="w-full max-w-2xl p-8 mx-auto bg-white rounded-lg shadow-lg dark:bg-gray-800">
          <h2 className="mb-6 text-2xl font-bold text-center text-gray-700 dark:text-gray-200">
            Search for Facebook Ads
          </h2>
          {/* This shows an error message if there is one */}
          {error && (
            <div className="p-2 mb-4 text-red-600 bg-red-100 border border-red-600 rounded">
              {error}
            </div>
          )}
          <div className="flex space-x-4">
            {/* Dropdown to select a country */}
            <div className="relative w-1/4">
              <div
                className="relative"
                onClick={() => setDropdownOpen(!isDropdownOpen)} // Toggle dropdown visibility
              >
                <input
                  type="text"
                  placeholder="Country"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)} // Update searchTerm with user input
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-200"
                  onClick={() => setDropdownOpen(true)} // Show dropdown when input is clicked
                />
                <div
                  className={`absolute w-full mt-1 bg-white rounded-lg shadow-lg dark:bg-gray-700 max-h-60 overflow-y-auto z-10 ${
                    isDropdownOpen ? '' : 'hidden' // Show or hide the dropdown
                  }`}
                >
                  {/* Show this message if no countries are found */}
                  {filteredCountries.length === 0 ? (
                    <div className="p-2 text-center dark:text-white">
                      No countries found
                    </div>
                  ) : (
                    // List of filtered countries
                    filteredCountries.map((country, index) => (
                      <div
                        key={index}
                        className="p-2 cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 dark:text-white"
                        onClick={() => handleCountrySelect(country)} // Select country on click
                      >
                        {country}
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>

            {/* Input for the search phrase */}
            <input
              type="text"
              placeholder="Search Phrase"
              value={phrase}
              onChange={(e) => {
                setPhrase(e.target.value);
                setError(''); // Clear any errors when input changes
              }}
              className="flex-grow w-1/2 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-200"
            />

            {/* Button to start the search */}
            <button
              onClick={handleSearch}
              className="w-1/4 p-3 text-white bg-blue-500 rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={!phrase.trim() || !country.trim()} // Disable button if inputs are empty
            >
              Search
            </button>
          </div>
        </div>
      </main>

      {/* Footer section at the bottom of the page */}
      <footer className="w-full p-4 mt-auto text-center text-white bg-gray-800 dark:bg-gray-900">
        <p>Developed by Saksham Tiwari</p>
      </footer>
    </div>
  );
}

export default SearchPage;

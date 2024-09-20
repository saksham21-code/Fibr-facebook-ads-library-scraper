import React, { useState, useEffect } from 'react';
import { countries } from './countries'; // Import the countries list
import { useNavigate } from 'react-router-dom'; // Import useNavigate for navigation

function SearchPage() {
  const [phrase, setPhrase] = useState('');
  const [country, setCountry] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [isDropdownOpen, setDropdownOpen] = useState(false);
  const [theme, setTheme] = useState('dark');
  const [error, setError] = useState(''); // State for error handling

  const navigate = useNavigate(); // Initialize navigate

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    document.documentElement.classList.toggle('dark');
  };

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const handleSearch = () => {
    if (!phrase.trim() || !country.trim()) {
      setError('Please enter both a search phrase and a country.');
      return;
    }

    // Navigate to the results page with search parameters and reset the page to 1
    navigate('/results', { state: { phrase: phrase.trim(), country: country.trim(), resetPage: true } });
  };

  // Filter countries based on search term
  const filteredCountries = countries.filter((country) =>
    country.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleCountrySelect = (selectedCountry) => {
    setCountry(selectedCountry);
    setSearchTerm(selectedCountry);
    setDropdownOpen(false);
    setError(''); // Clear error if a country is selected
  };

  return (
    <div className={`flex flex-col min-h-screen ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
      {/* Header Section */}
      <header className="w-full p-4 bg-blue-600 shadow-md dark:bg-gray-800">
        <div className="flex items-center justify-between max-w-6xl mx-auto">
          <div className="flex-grow text-center">
            <h1 className="text-3xl font-bold text-white dark:text-white"> {/* Text color changed to white */}
              Facebook Ads Scraper
            </h1>
            <p className="mt-1 text-lg text-white dark:text-gray-300"> {/* Text color changed to white */}
              Search and analyze Facebook ads in real time
            </p>
          </div>
          <button
            onClick={toggleTheme}
            className="px-3 py-2 text-white bg-gray-600 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            {theme === 'dark' ? 'Light theme' : 'Dark theme'}
          </button>
        </div>
      </header>

      {/* Main Content Section */}
      <main className="flex items-center justify-center flex-grow my-8">
        <div className="w-full max-w-2xl p-8 mx-auto bg-white rounded-lg shadow-lg dark:bg-gray-800">
          <h2 className="mb-6 text-2xl font-bold text-center text-gray-700 dark:text-gray-200">
            Search for Facebook Ads
          </h2>
          {error && (
            <div className="p-2 mb-4 text-red-600 bg-red-100 border border-red-600 rounded">
              {error}
            </div>
          )}
          <div className="flex space-x-4">
            {/* Custom Searchable Dropdown */}
            <div className="relative w-1/4">
              <div
                className="relative"
                onClick={() => setDropdownOpen(!isDropdownOpen)}
              >
                <input
                  type="text"
                  placeholder="Country"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-200"
                  onClick={() => setDropdownOpen(true)}
                />
                <div
                  className={`absolute w-full mt-1 bg-white rounded-lg shadow-lg dark:bg-gray-700 max-h-60 overflow-y-auto z-10 ${
                    isDropdownOpen ? '' : 'hidden'
                  }`}
                >
                  {filteredCountries.length === 0 ? (
                    <div className="p-2 text-center dark:text-white">
                      No countries found
                    </div>
                  ) : (
                    filteredCountries.map((country, index) => (
                      <div
                        key={index}
                        className="p-2 cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 dark:text-white"
                        onClick={() => handleCountrySelect(country)}
                      >
                        {country}
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>

            {/* Search Phrase Input */}
            <input
              type="text"
              placeholder="Search Phrase"
              value={phrase}
              onChange={(e) => {
                setPhrase(e.target.value);
                setError(''); // Clear error if the phrase is changed
              }}
              className="flex-grow w-1/2 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-200"
            />

            {/* Search Button */}
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

      {/* Footer Section */}
      <footer className="w-full p-4 mt-auto text-center text-white bg-gray-800 dark:bg-gray-900">
        <p>Developed by Saksham Tiwari</p>
      </footer>
    </div>
  );
}

export default SearchPage;

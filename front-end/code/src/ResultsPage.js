import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

function ResultsPage() {
  const navigate = useNavigate(); // Hook for navigation between pages
  const location = useLocation(); // Hook to get the data passed to this page
  const [adsCache, setAdsCache] = useState({}); // Cache to store ads data by page number
  const [ads, setAds] = useState([]); // State to hold the current ads data
  const [currentPage, setCurrentPage] = useState(1); // State to keep track of the current page number
  const [isLoading, setIsLoading] = useState(false); // State to show spinner while fetching data
  const [hasMore, setHasMore] = useState(true); // State to check more pages to load
  const [error, setError] = useState(''); // State to hold error messages
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light'); // State to hold the current theme 

  // Extract the search phrase and country
  const { phrase = '', country = '' } = location.state || {};

  useEffect(() => {
    // Apply the correct theme
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem('theme', theme); // Save the theme preference
  }, [theme]);

  useEffect(() => {
    // Redirect to the search page if no phrase or country 
    if (!phrase || !country) {
      setError('Search parameters are missing. Redirecting to search page...');
      setTimeout(() => navigate('/'), 2000); // Redirect after 2 seconds
    } else if (adsCache[currentPage]) {
      // If the ads for the current page are already in the cache
      setAds(adsCache[currentPage].ads);
      setHasMore(adsCache[currentPage].hasMore);
    } else {
      // If not, fetch the ads from the API
      fetchAds(currentPage);
    }
  }, [currentPage, phrase, country]);

  // fetches the ads from the API
  const fetchAds = async (page) => {
    setIsLoading(true); //  loading spinner
    setError(''); // Clear previous error
    try {
      // Fetch the ads from the server
      const response = await fetch(
        `http://localhost:8080/scrape-ads?phrase=${encodeURIComponent(phrase)}&country=${encodeURIComponent(country)}&pageNumber=${page}&pageSize=10`
      );
      const data = await response.json();
      if (data && Array.isArray(data.ads)) {
        // If the server responds 
        if (data.ads.length === 0) {
          setError('SORRY!! It seems your search phrase has no Ads in the ad library.'); // Show error if no ads 
        } else {
          setError(''); // Clear error if ads found
        }
        // Update the state with the fetched ads and save them in the cache
        setAds(data.ads || []);
        setHasMore(data.hasMore); // Check if there are more ads to load
        setAdsCache((prevCache) => ({
          ...prevCache,
          [page]: { ads: data.ads || [], hasMore: data.hasMore },
        })); // Add the ads to the cache
      } else {
        setError('Unexpected response format from the server.'); // Show error if response is not as expected
        setAds([]); // Clear the ads state
      }
      setIsLoading(false); // Hide the loading spinner
    } catch (error) {
      setError('Error fetching ads. Please try again later.'); // Show error if the request fails
      setIsLoading(false); // Hide the loading spinner
    }
  };

  // handles clicking the "Next" button
  const handleNextPage = () => {
    if (hasMore) {
      setCurrentPage((prev) => prev + 1); // Go to the next page if more ads available
    }
  };

  // handles clicking the "Previous" button
  const handlePreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage((prev) => prev - 1); // Go to the previous page if not the first one
    }
  };

  // toggles the theme 
  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
  };

  return (
    <div className={`flex flex-col min-h-screen ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
      {/* Header Section */}
      <header className="w-full p-4 bg-blue-600 shadow-md dark:bg-gray-800 dark:text-white">
        <div className="flex items-center justify-between max-w-6xl mx-auto">
          {/* go back to the search page */}
          <button
            onClick={() => navigate('/')}
            className="px-3 py-2 text-white bg-gray-600 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            Back
          </button>
          <div className="flex-grow text-center">
            <h1 className="text-3xl font-bold">Ads Results</h1>
          </div>
          {/* change the theme */}
          <button
            onClick={toggleTheme}
            className="px-3 py-2 ml-auto text-white bg-gray-600 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            {theme === 'dark' ? 'Light Theme' : 'Dark Theme'}
          </button>
        </div>
      </header>

      {/* Main Content Section */}
      <main className="flex-grow p-8">
        {/* error message if an error */}
        {error && (
          <div className="p-6 mb-6 text-red-700 bg-red-200 border border-red-300 rounded-lg shadow-lg dark:bg-red-900 dark:text-red-200">
            <h3 className="mb-2 text-xl font-semibold">No Ads Found</h3>
            <p>{error}</p>
          </div>
        )}
        {isLoading ? (
          // loading spinner while fetching ads
          <div className="flex items-center justify-center min-h-[50vh]">
            <div className="w-16 h-16 border-t-4 border-blue-500 rounded-full animate-spin"></div>
          </div>
        ) : (
          // Show the ads if there are any
          Array.isArray(ads) && ads.length > 0 ? (
            <div className="grid gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-5">
              {/* Display each ad */}
              {ads.map((ad, index) => (
                <div
                  key={index}
                  className="flex flex-col justify-between p-6 bg-white rounded-lg shadow-md dark:bg-gray-800"
                >
                  <div>
                    {/* Display the account name, library ID, and ad title */}
                    <h3 className="mb-2 text-xl font-bold text-gray-900 dark:text-gray-100">
                      {ad.accountName}
                    </h3>
                    <p className="mb-1 text-gray-700 dark:text-gray-300">
                      {ad.libraryID}
                    </p>
                    <p className="mb-4 text-gray-700 dark:text-gray-300">
                      {ad.title}
                    </p>
                    {/* Display media (image or video) if available */}
                    {ad.media ? (
                      <div className="mb-4">
                        {ad.media.endsWith('.mp4') || ad.media.includes('video') ? (
                          <video
                            src={ad.media}
                            controls
                            className="w-full h-auto"
                            onError={(e) => (e.target.style.display = 'none')}
                          >
                            Your browser does not support the video tag.
                          </video>
                        ) : (
                          <img
                            src={ad.media}
                            alt={ad.title}
                            className="w-full h-auto"
                            onError={(e) => (e.target.style.display = 'none')}
                          />
                        )}
                      </div>
                    ) : (
                      <div className="p-4 text-center text-white bg-gray-400 rounded">
                        Sorry 🙂, this ad had multiple images for different products, visit the FB ads page to know more.
                      </div>
                    )}
                  </div>
                  <div>
                    {/* Divider line */}
                    <hr className="my-2 border-t border-gray-300 dark:border-gray-600" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">Social Links:</p>
                    <div className="flex items-center mt-2 space-x-4">
                      {/* Links to social  */}
                      {ad.fbLink && (
                        <a href={ad.fbLink} target="_blank" rel="noopener noreferrer">
                          <img src="https://cdn-icons-png.flaticon.com/512/174/174848.png" alt="Facebook" className="w-6 h-6" />
                        </a>
                      )}
                      {ad.igLink && (
                        <a href={ad.igLink} target="_blank" rel="noopener noreferrer">
                          <img src="https://cdn-icons-png.flaticon.com/512/174/174855.png" alt="Instagram" className="w-6 h-6" />
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            // if no ads are found and there's no error
            !isLoading && error === '' && (
              <div className="p-6 mb-6 text-gray-700 bg-gray-100 border border-gray-300 rounded-lg shadow-lg dark:bg-gray-800 dark:text-gray-200">
                <h3 className="mb-2 text-xl font-semibold">No Ads Found</h3>
                <p>SORRY!! It seems your search phrase has no Ads in the ad library.</p>
              </div>
            )
          )
        )}
      </main>

      {/* Pagination Section */}
      <footer className="flex items-center justify-between w-full p-6 mt-auto text-center text-white bg-gray-800 dark:bg-gray-900">
        {/* Button to go to the previous page */}
        <button
          onClick={handlePreviousPage}
          disabled={currentPage === 1}
          className={`px-4 py-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-500 ${
            currentPage === 1 ? 'bg-gray-300 dark:bg-gray-700 text-gray-400 cursor-not-allowed' : 'bg-gray-200 dark:bg-gray-600 text-gray-800 hover:bg-gray-300 dark:hover:bg-gray-500'
          }`}
          style={{ marginLeft: '25%' }}
        >
          Previous
        </button>
        {/* Button to go to the next page */}
        <button
          onClick={handleNextPage}
          disabled={!hasMore}
          className={`px-4 py-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-500 ${
            !hasMore ? 'bg-gray-300 dark:bg-gray-700 text-gray-400 cursor-not-allowed' : 'bg-gray-200 dark:bg-gray-600 text-gray-800 hover:bg-gray-300 dark:hover:bg-gray-500'
          }`}
          style={{ marginRight: '25%' }}
        >
          Next
        </button>
      </footer>
    </div>
  );
}

export default ResultsPage;

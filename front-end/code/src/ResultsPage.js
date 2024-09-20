import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

function ResultsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [adsCache, setAdsCache] = useState({}); // Cache for storing ads by page number
  const [ads, setAds] = useState([]); // Ensure initial state is an array
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true); // State to track if more ads are available
  const [error, setError] = useState(''); // State for error messages
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');

  // Extract phrase and country from state passed via navigate or use empty strings as fallback
  const { phrase = '', country = '' } = location.state || {};

  useEffect(() => {
    // Apply theme on component mount
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem('theme', theme);
  }, [theme]);

  useEffect(() => {
    // Redirect to search page if phrase or country is missing
    if (!phrase || !country) {
      setError('Search parameters are missing. Redirecting to search page...');
      setTimeout(() => navigate('/'), 2000); // Redirect after 2 seconds
    } else if (adsCache[currentPage]) {
      // If the page is cached, use the cached ads
      setAds(adsCache[currentPage].ads);
      setHasMore(adsCache[currentPage].hasMore);
    } else {
      // If not cached, fetch ads from API
      fetchAds(currentPage);
    }
  }, [currentPage, phrase, country]);

  const fetchAds = async (page) => {
    setIsLoading(true);
    setError(''); // Reset error state before fetching
    try {
      const response = await fetch(
        `http://localhost:8080/scrape-ads?phrase=${encodeURIComponent(phrase)}&country=${encodeURIComponent(country)}&pageNumber=${page}&pageSize=10`
      );
      const data = await response.json();
      if (data && Array.isArray(data.ads)) {
        if (data.ads.length === 0) {
          setError('SORRY!! It seems your search phrase has no Ads in the ad library.');
        } else {
          setError(''); // Clear the error if ads are found
        }
        // Check if ads is an array before setting the state
        setAds(data.ads || []);
        setHasMore(data.hasMore); // Update hasMore based on the response
        setAdsCache((prevCache) => ({
          ...prevCache,
          [page]: { ads: data.ads || [], hasMore: data.hasMore },
        })); // Cache the ads for the current page
      } else {
        setError('Unexpected response format from the server.');
        setAds([]); // Fallback to empty array if response format is unexpected
      }
      setIsLoading(false);
    } catch (error) {
      setError('Error fetching ads. Please try again later.');
      setIsLoading(false);
    }
  };

  const handleNextPage = () => {
    if (hasMore) {
      setCurrentPage((prev) => prev + 1);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage((prev) => prev - 1);
    }
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
  };

  return (
    <div className={`flex flex-col min-h-screen ${theme === 'dark' ? 'bg-gray-900' : 'bg-gray-100'}`}>
      {/* Header Section */}
      <header className="w-full p-4 bg-blue-600 shadow-md dark:bg-gray-800 dark:text-white">
        <div className="flex items-center justify-between max-w-6xl mx-auto">
          <button
            onClick={() => navigate('/')}
            className="px-3 py-2 text-white bg-gray-600 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            Back
          </button>
          <div className="flex-grow text-center">
            <h1 className="text-3xl font-bold">Ads Results</h1>
          </div>
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
        {error && (
          <div className="p-6 mb-6 text-red-700 bg-red-200 border border-red-300 rounded-lg shadow-lg dark:bg-red-900 dark:text-red-200">
            <h3 className="mb-2 text-xl font-semibold">No Ads Found</h3>
            <p>{error}</p>
          </div>
        )}
        {isLoading ? (
          <div className="flex items-center justify-center min-h-[50vh]">
            <div className="w-16 h-16 border-t-4 border-blue-500 rounded-full animate-spin"></div>
          </div>
        ) : (
          Array.isArray(ads) && ads.length > 0 ? ( // Check if ads is an array and not empty
            <div className="grid gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-5">
              {ads.map((ad, index) => (
                <div
                  key={index}
                  className="flex flex-col justify-between p-6 bg-white rounded-lg shadow-md dark:bg-gray-800"
                >
                  <div>
                    <h3 className="mb-2 text-xl font-bold text-gray-900 dark:text-gray-100">
                      {ad.accountName}
                    </h3>
                    <p className="mb-1 text-gray-700 dark:text-gray-300">
                      {ad.libraryID}
                    </p>
                    <p className="mb-4 text-gray-700 dark:text-gray-300">
                      {ad.title}
                    </p>
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
                    <hr className="my-2 border-t border-gray-300 dark:border-gray-600" />
                    <p className="text-sm text-gray-500 dark:text-gray-400">Social Links:</p>
                    <div className="flex items-center mt-2 space-x-4">
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

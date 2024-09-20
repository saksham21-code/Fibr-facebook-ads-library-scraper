import React, { useState } from 'react';

function Search({ onSearch }) {
  const [phrase, setPhrase] = useState('');
  const [country, setCountry] = useState('');

  const handleSearch = () => {
    if (phrase && country) {
      onSearch(phrase, country);
    } else {
      alert('Please enter a search phrase and select a country.');
    }
  };

  return (
    <div className="bg-white p-6 rounded shadow-md max-w-md w-full">
      <h2 className="text-2xl font-bold mb-4 text-center">Search for Facebook Ads</h2>
      <input
        type="text"
        placeholder="Search Phrase"
        value={phrase}
        onChange={(e) => setPhrase(e.target.value)}
        className="border p-2 w-full mb-4"
      />
      <select
        value={country}
        onChange={(e) => setCountry(e.target.value)}
        className="border p-2 w-full mb-4"
      >
        <option value="">Select Country</option>
        <option value="India">India</option>
        <option value="USA">USA</option>
        {/* Add more countries as needed */}
      </select>
      <button
        onClick={handleSearch}
        className="bg-blue-500 text-white p-2 rounded w-full"
      >
        Search
      </button>
    </div>
  );
}

export default Search;

import React from 'react';
import { Routes, Route } from 'react-router-dom';
import SearchPage from './SearchPage';
import ResultsPage from './ResultsPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<SearchPage />} />
      <Route path="/results" element={<ResultsPage />} />
    </Routes>
  );
}

export default App;

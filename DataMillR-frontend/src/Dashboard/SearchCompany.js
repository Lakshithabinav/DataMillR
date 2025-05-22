import React, { useState, useEffect } from 'react';
import axios from 'axios';

const SearchCompany = () => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [debounceTimer, setDebounceTimer] = useState(null);
   const [isSelecting, setIsSelecting] = useState(false);

  useEffect(() => {
    if (isSelecting) return; 
    if (debounceTimer) clearTimeout(debounceTimer);
    const timer = setTimeout(() => {
      if (query.trim() !== '') {
        axios
          .get(`http://localhost:8082/admin/search-company-name?companyName=${encodeURIComponent(query)}`)
          .then((res) => {
            setSuggestions(res.data);
            setShowDropdown(true);
          })
          .catch(() => {
            setSuggestions([]);
            setShowDropdown(false);
          });
      } else {
        setSuggestions([]);
        setShowDropdown(false);
      }
    }, 300);

    setDebounceTimer(timer);

    return () => clearTimeout(timer);
  }, [query]);

  const handleSelect = (companyName) => {
    setIsSelecting(true);
    setQuery(companyName);
    setSuggestions([]);
    setShowDropdown(false);
  };

  return (
    <div className="relative max-w-md mx-auto mt-10">
      <input
        type="text"
        placeholder="Search company name..."
        className="w-full p-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={() => setShowDropdown(suggestions.length > 0)}
      />

      {showDropdown && suggestions.length > 0 && (
        <ul className="absolute left-0 right-0 bg-white border border-gray-300 shadow-md rounded-md mt-1 max-h-60 overflow-y-auto z-10">
          {suggestions.map((item, idx) => (
            <li
              key={idx}
              className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
              onClick={() => handleSelect(item.companyName)}
            >
              {item.companyName}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default SearchCompany;

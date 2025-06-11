import React, { useState, useEffect } from "react";
import axios from "axios";
import qs from "qs";
import './Dashboard.css';

function Dashboard() {
  const [deviceName, setDeviceName] = useState("");
  const [deviceId, setDeviceId] = useState("");
  const [companyName, setCompanyName] = useState("");
  const [responseMsg, setResponseMsg] = useState("");
  const [isRegistered, setIsRegistered] = useState(false);
  const [showPopup, setShowPopup] = useState(false);
  const [popupData, setPopupData] = useState(null);
  const [debounceTimer, setDebounceTimer] = useState(null);
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [isSelecting, setIsSelecting] = useState(false);
  const [userKey, setUserKey] = useState(0);
  const [searchResultCount, setSearchResultCount] = useState(0);

  useEffect(() => {
    const storedData = sessionStorage.getItem("deviceData");
    if (storedData) {
      const parsed = JSON.parse(storedData);
      setDeviceName(parsed.deviceName);
      setDeviceId(parsed.deviceId);
      setCompanyName(parsed.companyName);
      setIsRegistered(true);
      setResponseMsg("Device already registered.");
    }
  }, []);

  useEffect(() => {
    if (isSelecting) return;
    if (debounceTimer) clearTimeout(debounceTimer);

    const timer = setTimeout(() => {
      if (companyName.trim() !== '') {
        axios.get(`http://localhost:8082/admin/search-company-name?companyName=${encodeURIComponent(companyName)}`)
          .then((res) => {
            setSearchResultCount(res.data.length);
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
  }, [companyName]);

  const handleRegister = async (e) => {
    e.preventDefault();

    const payload = {
      deviceName,
      deviceId,
      companyName,
    };

    try {
      const response = await axios.post(
        "http://localhost:8082/admin/create-table",
        qs.stringify(payload),
        {
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
        }
      );

      const responseData = response?.data;
      if (!responseData) throw new Error("Invalid response from server");

      const fullData = { ...responseData, deviceName, deviceId, companyName };
      sessionStorage.setItem("deviceData", JSON.stringify(fullData));

      setResponseMsg(responseData.message || "Device registered successfully.");
      setIsRegistered(true);
    } catch (error) {
      const errMsg = error?.response?.data?.message || error?.response?.data || error.message || "An error occurred.";
      console.error("Registration error:", errMsg);
      setResponseMsg(`Error: ${errMsg}`);
    }
  };

  const handleSelect = (companyName, userKeyVal) => {
    setUserKey(userKeyVal);
    setIsSelecting(true);
    setCompanyName(companyName);
    setSuggestions([]);
    setShowDropdown(false);
  };

  const handleMapping = async () => {
    const isNewUser = searchResultCount === 0 || !isSelecting;

    const payload = {
      deviceId: parseInt(deviceId),
      deviceName,
      companyName,
      userKey,
      newUser: isNewUser,
    };

    try {
      const res = await axios.post("http://localhost:8082/admin/register-device", payload);
      const data = res.data;

      // ✅ Store new user credentials in sessionStorage
      if (data.newUser && data.userId && data.password) {
        sessionStorage.setItem("newUserId", data.userId);
        sessionStorage.setItem("newUserPassword", data.password);
        sessionStorage.setItem("isNewUser", "true");
      }

      setPopupData({
        deviceName,
        deviceId,
        companyName,
        userId: data.userId,
        password: data.password,
        isNewUser: data.newUser,
      });

      // Clear form inputs and message
      setDeviceName("");
      setDeviceId("");
      setCompanyName("");
      setResponseMsg(null);
      sessionStorage.removeItem("deviceData");

      setShowPopup(true);
    } catch (error) {
      setPopupData({ error: error?.response?.data?.message || error.message });
      setShowPopup(true);
    }
  };

  const closePopup = () => {
    setShowPopup(false);
    setPopupData(null);
  };

  return (
    <div style={{ maxWidth: 500, margin: "0 auto", padding: 20 }}>
      <h2>Device Information Form</h2>
      <form>
        <div>
          <label>Device ID:</label>
          <input
            type="text"
            value={deviceId}
            onChange={(e) => setDeviceId(e.target.value)}
            required
          />
        </div>

        <div style={{ marginTop: 10 }}>
          <button type="submit" onClick={handleRegister}>Register</button>
        </div>

        <div>
          <label>Device Name:</label>
          <input
            type="text"
            value={deviceName}
            onChange={(e) => setDeviceName(e.target.value)}
            required
          />
        </div>

        <div>
          <label>Company Name:</label>
          <input
            type="text"
            value={companyName}
            onChange={(e) => {
              setCompanyName(e.target.value);
              setIsSelecting(false);
            }}
            required
          />
        </div>

        {showDropdown && suggestions.length > 0 && (
          <ul className="absolute left-0 right-0 bg-white border border-gray-300 shadow-md rounded-md mt-1 max-h-60 overflow-y-auto z-10">
            {suggestions.map((item, idx) => (
              <li
                key={idx}
                className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                onClick={() => handleSelect(item.companyName, item.userKey)}
              >
                {item.companyName}
              </li>
            ))}
          </ul>
        )}

        <button
          type="button"
          style={{ marginTop: 10 }}
          onClick={handleMapping}
        >
          Mapping
        </button>

        {responseMsg && <p style={{ marginTop: 10 }}>{responseMsg}</p>}
      </form>

      {showPopup && popupData && (
        <div className="popup-overlay">
          <div className="popup-content">
            <h3>Mapping Response</h3>
            {popupData.error ? (
              <p style={{ color: "red" }}>{popupData.error}</p>
            ) : (
              <div>
                <p><strong>Device Name:</strong> {popupData.deviceName}</p>
                <p><strong>Device ID:</strong> {popupData.deviceId}</p>
                <p><strong>Company Name:</strong> {popupData.companyName}</p>
                {popupData.isNewUser && (
                  <>
                    <p><strong>User ID:</strong> {popupData.userId}</p>
                    <p><strong>Password:</strong> {popupData.password}</p>
                  </>
                )}
              </div>
            )}
            <button onClick={closePopup}>X</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;

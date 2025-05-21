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
  const[isNewUser,setISnewUser] = useState(false);
  const [showPopup, setShowPopup] = useState(false);
  const [popupData, setPopupData] = useState(null);
  const [debounceTimer, setDebounceTimer] = useState(null);
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [isSelecting, setIsSelecting] = useState(false);
  const[userKey,setUserKey] = useState(0);

  var searchresult=0;
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
        axios
          .get(`http://localhost:8082/admin/search-company-name?companyName=${encodeURIComponent(companyName)}`)
          .then((res) => {
            searchresult =res.data.length;
            console.log(res.data);
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
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      // Save isNewCompany flag along with data
      const responseData = {
        ...response.data,
        deviceName,
        deviceId,
        companyName,
        isNewCompany: response.data.isNewCompany, // expect backend to include this
      };

      sessionStorage.setItem("deviceData", JSON.stringify(responseData));
      setResponseMsg(response.data.message || "Device registered successfully.");
      setIsRegistered(true);

      setDeviceName("");
      
      setCompanyName("");
    } catch (error) {
      console.log(error.response.data);
      setResponseMsg(`Error: ${error.response.data|| error.message}`);
    }
  };
  const handleSelect = (companyName,userkeyy) => {
    console.log(userkeyy);
    setUserKey(userkeyy);
    setIsSelecting(true);
    setCompanyName(companyName);
    setSuggestions([]);
    setShowDropdown(false);
  };
  const handleMapping = async (e) => {
    e.preventDefault();

    const storedData = sessionStorage.getItem("deviceData");
    if (!storedData) {
      setResponseMsg("No registered device to map.");
      return;
    }

    const parsed = JSON.parse(storedData);
    console.log(isSelecting);

    if(searchresult ===0||!isSelecting){
      setISnewUser(true);
    }
    console.log(userKey);

    const mappingPayload = {
      deviceId: parsed.deviceId,
      deviceName: deviceName,
      userKey: userKey, // make sure this is available
      companyName: companyName,
      password: parsed.password,
      isNewUser: isNewUser, // match backend expectations
    };

    try {
      const res = await axios.post(
        "http://localhost:8082/admin/register-device",
        mappingPayload
      );

      const responseData = res.data;

      const popupInfo = {
        deviceName: parsed.deviceName,
        deviceId: parsed.deviceId,
        companyName: parsed.companyName,
        userId: responseData.userId ?? parsed.userId,
        password: responseData.password ?? parsed.password,
        isNewCompany: parsed.isNewCompany,
      };

      setPopupData(popupInfo);
      setShowPopup(true);
    } catch (error) {
      setPopupData({ error: error.response?.data?.message || error.message });
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
          <button type="submit" onClick={handleRegister}>
            Register
          </button>
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
            onChange={(e) => {setCompanyName(e.target.value);setIsSelecting(false)}}
            required
          />
        </div>
        {showDropdown && suggestions.length > 0 && (
          <ul className="absolute left-0 right-0 bg-white border border-gray-300 shadow-md rounded-md mt-1 max-h-60 overflow-y-auto z-10">
            {suggestions.map((item, idx) => (
              <li
                key={idx}
                className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                onClick={() => handleSelect(item.companyName,item.userKey)}
              >
                {item.companyName}
              </li>
            ))}
          </ul>
        )}

        

          {isRegistered && (
            <button
              type="button"
              style={{ marginLeft: 10 }}
              onClick={handleMapping}
            >
              Mapping
            </button>
          )}
        </div>

        {responseMsg && <p style={{ marginTop: 10 }}>{responseMsg}</p>}
      </form>

      {/* Mapping Popup */}
      {showPopup && popupData && (
        <div className="popup-overlay">
          <div className="popup-content">
            <h3>Mapping Response</h3>
            <div style={{
              textAlign: 'left',
              backgroundColor: '#f9f9f9',
              padding: '10px',
              borderRadius: '5px'
            }}>
              {popupData.error ? (
                <p style={{ color: "red" }}>{popupData.error}</p>
              ) : (
                <>
                  <p><strong>Device Name:</strong> {popupData.deviceName}</p>
                  <p><strong>Device ID:</strong> {popupData.deviceId}</p>
                  <p><strong>Company Name:</strong> {popupData.companyName}</p>
                  <p><strong>User ID:</strong> {popupData.userId}</p>
                  <p><strong>Password:</strong> {popupData.password}</p>
                  {popupData.isNewCompany && (
                    <>
                      <hr />
                      <p><strong>User ID:</strong> {popupData.userId}</p>
                      <p><strong>Password:</strong> {popupData.password}</p>
                    </>
                  )}
                </>
              )}
            </div>
            <button className="close-arrow" onClick={closePopup}>→</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;


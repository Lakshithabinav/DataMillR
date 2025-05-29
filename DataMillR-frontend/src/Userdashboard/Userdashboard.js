import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Userdashboard.css';

const UserDashboard = () => {
  const [userData, setUserData] = useState(null);
  const [selectedDeviceId, setSelectedDeviceId] = useState('');
  const [deviceData, setDeviceData] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [notLoggedIn, setNotLoggedIn] = useState(false);

  useEffect(() => {
    const stored = sessionStorage.getItem('userData');
    if (!stored) {
      setNotLoggedIn(true);
      return;
    }

    try {
      const parsed = JSON.parse(stored);
      setUserData(parsed);
    } catch (err) {
      console.error('Failed to parse user data:', err);
      setNotLoggedIn(true);
    }
  }, []);

  const fetchDeviceData = (deviceId) => {
    axios
      .post('http://localhost:8082/user/data', { deviceId })
      .then((res) => {
        setDeviceData(res.data);
        setLastUpdated(new Date());
      })
      .catch((err) => {
        console.error('Error fetching device data:', err);
        setDeviceData([]);
      });
  };

  useEffect(() => {
    if (!selectedDeviceId) return;

    fetchDeviceData(selectedDeviceId);

    const interval = setInterval(() => {
      fetchDeviceData(selectedDeviceId);
    }, 60000);

    return () => clearInterval(interval);
  }, [selectedDeviceId]);

  if (notLoggedIn) return <p>User not logged in. Please go back to login page.</p>;
  if (!userData) return <p>Loading dashboard...</p>;

  const { devices = [], companyName = '' } = userData;

  // Logout handler without router
  const handleLogout = () => {
    sessionStorage.clear();
    window.location.href = '/Userlogin/Userlogin.js'; // Change '/login' to your actual login page path if needed
  };

  return (
    <div id="tomoveright">
      <div id="move">
        {/* Header with Logout button */}
        <div className="taskbar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <center style={{ flex: 1 , fontSize: 35}}>
            {companyName.charAt(0).toUpperCase() + companyName.slice(1)}
          </center>
          <button
            onClick={handleLogout}
            style={{
              backgroundColor: '#5D5C61',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              padding: '8px 15px',
              cursor: 'pointer',
              fontWeight: 'bold',
              marginRight: '10px',
              flexShrink: 0,
            }}
          >
            Logout
          </button>
        </div>

        {/* Device Selector */}
        <div className="device-selector">
          <p>Select Device:</p>
          <div className="device-boxes-scroll">
            <div className="device-boxes">
              {devices.map((device) => (
                <div
                  key={device.deviceId}
                  onClick={() => setSelectedDeviceId(device.deviceId)}
                  className={`device-box ${selectedDeviceId === device.deviceId ? 'active' : ''}`}
                >
                  {device.deviceName}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Data Display */}
        <div className="data-section">
          {selectedDeviceId && deviceData.length > 0 ? (
            deviceData.map((row, index) => (
              <div key={index} className="data-card">
                <div className="data-header">
                  Last Updated: {lastUpdated ? lastUpdated.toLocaleTimeString() : '...'}
                </div>

                <div className="data-row">
                  <div className="data-box">
                    <strong>Timestamp:</strong>
                    <br />
                    {new Date(row.timestamp).toLocaleString()}
                  </div>
                  <div className="data-box">
                    <strong>Batch Name:</strong>
                    <br />
                    {row.batchName}
                  </div>
                </div>

                <div className="data-row">
                  <div className="data-box">
                    <strong>Set Weight:</strong>
                    <br />
                    {row.setWeight}
                  </div>
                  <div className="data-box">
                    <strong>Actual Weight:</strong>
                    <br />
                    {row.actualWeight}
                  </div>
                  <div className="data-box">
                    <strong>Total Weight:</strong>
                    <br />
                    {row.totalWeight}
                  </div>
                </div>
              </div>
            ))
          ) : selectedDeviceId ? (
            <p>No data available for this device.</p>
          ) : null}
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;

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

  const { devices = [] } = userData;

  const handleLogout = () => {
    sessionStorage.clear();
    window.location.href = '/';
  };

  const handleReport = () => {
    window.history.pushState({}, '', '/report');
    window.location.reload(); 
  };

return (
  <div id="move">
    <div
      className="taskbar"
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 20px',
        background: '#5D5C61',
        borderRadius: '0',
        boxShadow: 'none',
      }}
    >
      <div style={{ flex: 1 }}>
        <button
          onClick={handleReport}
          style={{
            backgroundColor: 'white',
            color: '#5D5C61',
            border: 'none',
            borderRadius: '5px',
            padding: '8px 15px',
            cursor: 'pointer',
            fontWeight: 'bold',
          }}
        >
          Report
        </button>
      </div>
      <div style={{ flex: 1, textAlign: 'center' }}>
        <span style={{ fontSize: 35, fontWeight: 'bold' }}>
          Current Status
        </span>
      </div>
      <div style={{ flex: 1, textAlign: 'right' }}>
        <button
          onClick={handleLogout}
          style={{
            backgroundColor: 'white',
            color: '#5D5C61',
            border: 'none',
            borderRadius: '5px',
            padding: '8px 15px',
            cursor: 'pointer',
            fontWeight: 'bold',
          }}
        >
          Logout
        </button>
      </div>
    </div>

    <div className="device-selector" style={{ padding: '20px' }}>
      <p style={{ fontWeight: 'bold' }}>Select Device:</p>
      <div className="device-boxes-scroll" style={{ overflowX: 'auto' }}>
        <div
          className="device-boxes"
          style={{
            display: 'flex',
            gap: '10px',
            flexWrap: 'wrap',
          }}
        >
          {devices.map((device) => (
            <div
              key={device.deviceId}
              onClick={() => setSelectedDeviceId(device.deviceId)}
              className={`device-box ${selectedDeviceId === device.deviceId ? 'active' : ''}`}
              style={{
                padding: '10px 15px',
                border: '1px solid #ccc',
                borderRadius: '5px',
                backgroundColor: selectedDeviceId === device.deviceId ? '#5D5C61' : '#f0f0f0',
                color: selectedDeviceId === device.deviceId ? 'white' : '#333',
                cursor: 'pointer',
                fontWeight: 'bold',
              }}
            >
              {device.deviceName}
            </div>
          ))}
        </div>
      </div>
    </div>

    <div className="data-section" style={{ padding: '20px' }}>
      {selectedDeviceId && deviceData.length > 0 ? (
        deviceData.map((row, index) => (
          <div
            key={index}
            className="data-card"
            style={{
              border: '1px solid #ddd',
              borderRadius: '10px',
              padding: '20px',
              marginBottom: '20px',
              backgroundColor: '#fff',
              boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
              position: 'relative',
            }}
          >
            <div
              className="data-header"
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '10px',
              }}
            >
              <div>
                <strong>Machine Status:</strong>{' '}
                <span
                  style={{
                    color: row.machineRunning ? 'green' : 'red',
                    fontWeight: 'bold',
                    marginLeft: '5px',
                  }}
                >
                  {row.machineRunning ? 'Running' : 'Stopped'}
                </span>
              </div>
              <div style={{ position: 'absolute', top: 20, right: 20 }}>
                <strong>Last Updated:</strong>{' '}
                {lastUpdated ? lastUpdated.toLocaleTimeString() : '...'}
              </div>
            </div>

            <div
              style={{
                fontSize: '18px',
                fontWeight: 'bold',
                marginBottom: '10px',
              }}
            >
              Batch Name: {row.batchName}
            </div>

            <div
              className="data-row"
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                gap: '20px',
                flexWrap: 'wrap',
              }}
            >
              <div className="data-box" style={{ minWidth: '200px' }}>
                <strong>Timestamp:</strong>
                <br />
                {new Date(row.timestamp).toLocaleString()}
              </div>
              <div className="data-box" style={{ minWidth: '150px' }}>
                <strong>Set Weight:</strong>
                <br />
                {row.setWeight}
              </div>
              <div className="data-box" style={{ minWidth: '150px' }}>
                <strong>Actual Weight:</strong>
                <br />
                {row.actualWeight}
              </div>
              <div className="data-box" style={{ minWidth: '150px' }}>
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
);
};

export default UserDashboard; 
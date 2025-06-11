import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from "recharts";
import "./Userdashboard.css";

const UserDashboard = () => {
  const [userData, setUserData] = useState(null);
  const [selectedDeviceId, setSelectedDeviceId] = useState("");
  const [deviceData, setDeviceData] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [notLoggedIn, setNotLoggedIn] = useState(false);

  useEffect(() => {
    const stored = sessionStorage.getItem("userData");
    if (!stored) {
      setNotLoggedIn(true);
      return;
    }
    try {
      const parsed = JSON.parse(stored);

      // If new user, use old credentials and store in parsed.userId and parsed.password
      if (parsed.oldUserId && parsed.oldPassword) {
        parsed.userId = parsed.oldUserId;
        parsed.password = parsed.oldPassword;
      }

      const uniqueDevices = Array.isArray(parsed.devices)
        ? parsed.devices.filter(
            (device, index, self) =>
              index === self.findIndex((d) => d.deviceId === device.deviceId)
          )
        : [];

      setUserData({ ...parsed, devices: uniqueDevices });
      console.log("Logged-in Company Name:", parsed.companyName); // ✅ Debug check
    } catch (err) {
      console.error("Failed to parse user data:", err);
      setNotLoggedIn(true);
    }
  }, []);

  const fetchDeviceData = (deviceId) => {
    axios
      .post("http://localhost:8082/user/data", { deviceId })
      .then((res) => {
        let data = res.data.map((item) => ({
          ...item,
          timestamp: new Date(item.timestamp).getTime(),
          referenceWeight: item.totalWeight || 0,
        }));

        const now = new Date();
        const startOfDay = new Date(now);
        startOfDay.setHours(0, 0, 0, 0);

        if (data.length > 0 && data[0].timestamp > startOfDay.getTime()) {
          data.unshift({
            timestamp: startOfDay.getTime(),
            totalWeight: 0,
            setWeight: 0,
            actualWeight: 0,
            machineRunning: false,
            batchName: data[0].batchName,
          });
        }

        data.sort((a, b) => a.timestamp - b.timestamp);
        setDeviceData(data);
        setLastUpdated(new Date());
      })
      .catch((err) => {
        console.error("Error fetching device data:", err);
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

  const getNormalizedData = (data) => {
    if (!data || data.length === 0) return [];
    const sorted = [...data].sort((a, b) => a.timestamp - b.timestamp);
    const startOfDay = new Date(sorted[0].timestamp);
    startOfDay.setHours(0, 0, 0, 0);
    return [
      {
        ...sorted[0],
        timestamp: startOfDay.getTime(),
        totalWeight: 0,
        setWeight: 0,
        actualWeight: 0,
        machineRunning: false,
        batchName: sorted[0]?.batchName || "",
      },
      ...sorted,
    ];
  };

  const getXTicks = () => {
    const ticks = [];
    const base = new Date();
    base.setHours(0, 0, 0, 0);
    for (let h = 0; h <= 24; h += 2) {
      const tick = new Date(base);
      tick.setHours(h);
      ticks.push(tick.getTime());
    }
    return ticks;
  };

  const formatTimeTick = (time) =>
    new Date(time).toLocaleTimeString("en-IN", {
      hour: "numeric",
      hour12: true,
    });

  if (notLoggedIn) return <p>User not logged in. Please go back to login page.</p>;
  if (!userData) return <p>Loading dashboard...</p>;

  const { devices = [] } = userData;
  const selectedDevice = devices.find((d) => d.deviceId === selectedDeviceId);

  return (
    <div id="move" style={{ padding: "20px" }}>
      <div className="device-selector">
        <p><strong>Select:</strong></p>
        <select
          value={selectedDeviceId}
          onChange={(e) => setSelectedDeviceId(e.target.value)}
        >
          <option value="">-- Select --</option>
          {devices.map((device) => (
            <option key={device.deviceId} value={device.deviceId}>
              {device.deviceName}
            </option>
          ))}
        </select>
      </div>

      <div className="data-section">
        {selectedDeviceId && deviceData.length > 0 ? (
          <div className="data-card">
            <div className="data-header">
              <div>
                <strong>Machine Status:</strong>{" "}
                <span
                  style={{
                    color: deviceData[deviceData.length - 1].machineRunning
                      ? "green"
                      : "red",
                    fontWeight: "bold",
                    marginLeft: "5px",
                  }}
                >
                  {deviceData[deviceData.length - 1].machineRunning
                    ? "Running"
                    : "Stopped"}
                </span>
              </div>
              <div>
                <strong>Last Updated:</strong>{" "}
                {lastUpdated ? lastUpdated.toLocaleTimeString() : "..."}
              </div>
            </div>

            <div className="batch-title">
              Batch Name: {deviceData[deviceData.length - 1].batchName}
            </div>
            <div className="batch-title" style={{ marginTop: "4px" }}>
              Company Name: {userData?.companyName || "N/A"}
            </div>

            <div className="data-row">
              <div className="data-box">
                <strong>Timestamp:</strong>
                <br />
                {new Date(
                  deviceData[deviceData.length - 1].timestamp
                ).toLocaleString()}
              </div>
              <div className="data-box">
                <strong>Set Weight:</strong>
                <br />
                {deviceData[deviceData.length - 1].setWeight}
              </div>
              <div className="data-box">
                <strong>Actual Weight:</strong>
                <br />
                {deviceData[deviceData.length - 1].actualWeight}
              </div>
              <div className="data-box">
                <strong>Total Weight:</strong>
                <br />
                {deviceData[deviceData.length - 1].totalWeight}
              </div>
            </div>

            <div className="data-chart-scroll">
              <div className="data-chart-container">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={getNormalizedData(deviceData)}
                    margin={{ top: 20, right: 60, left: 60, bottom: 50 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="timestamp"
                      type="number"
                      scale="time"
                      domain={[
                        new Date().setHours(0, 0, 0, 0),
                        new Date().setHours(24, 0, 0, 0),
                      ]}
                      ticks={getXTicks()}
                      tickFormatter={formatTimeTick}
                      tick={{ fontSize: 12, textAnchor: "middle" }}
                      tickMargin={15}
                      label={{
                        value: "Time",
                        position: "insideBottom",
                        dy: 30,
                        fontSize: 14,
                      }}
                    />
                    <YAxis />
                    <Tooltip
                      labelFormatter={(label) => new Date(label).toLocaleString()}
                    />
                    <ReferenceLine y={0} stroke="grey" strokeDasharray="5 5" />
                    <Line
                      type="monotone"
                      dataKey="totalWeight"
                      stroke="red"
                      strokeWidth={3}
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                      isAnimationActive={true}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        ) : selectedDeviceId ? (
          <p>No data available for this device.</p>
        ) : (
          <p>Please select a device to view data.</p>
        )}
      </div>
    </div>
  );
};

export default UserDashboard;

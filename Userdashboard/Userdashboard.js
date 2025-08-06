import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  LineChart,
  Line,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
  BarChart,
  Bar,
} from "recharts";
import "./Userdashboard.css";
import config from "../config";

const UserDashboard = () => {
  const [userData, setUserData] = useState(null);
  const [selectedDeviceId, setSelectedDeviceId] = useState("");
  const [deviceData, setDeviceData] = useState([]);
  const [summaryData, setSummaryData] = useState([]);
  const [viewType, setViewType] = useState("daily");
  const [lastUpdated, setLastUpdated] = useState(null);
  const [notLoggedIn, setNotLoggedIn] = useState(false);
  const [grandTotal, setGrandTotal] = useState(0);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());


  useEffect(() => {
    const stored = sessionStorage.getItem("userData");
    if (!stored) {
      setNotLoggedIn(true);
      return;
    }
    try {
      const parsed = JSON.parse(stored);

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
    } catch (err) {
      console.error("Failed to parse user data:", err);
      setNotLoggedIn(true);
    }
  }, []);
 const fetchDeviceData = (deviceId) => {
    axios
      .post(`${config.BASE_URL}/user/data`, { deviceId })
      .then((res) => {
        const rawData = res.data;

        if (!rawData || rawData.length === 0) {
          setDeviceData([]);
          return;
        }

        const machineType = rawData[0].machinetype;
        let data = [];

        const now = new Date();
        const startOfDay = new Date(now);
        startOfDay.setHours(0, 0, 0, 0);
        const startTimestamp = startOfDay.getTime();

        if (machineType === "Flow Veyor") {
          data = rawData.map((item) => ({
            ...item,
            machineType,
            timestamp: new Date(item.timestamp).getTime(),
            actualWeight: item.presentWeight || 0,
            totalWeight: item.totalWeight || 0,
            machineStatus: item.status === 1 ? "Running" : "Stopped",
          }));

          if (data.length > 0 && data[0].timestamp > startTimestamp) {
            data.unshift({
              timestamp: startTimestamp,
              totalWeight: 0,
              setWeight: 0,
              actualWeight: 0,
              machineStatus: "Stopped",
              batchName: data[0].batchName || "",
              machineType,
            });
          }

          data.sort((a, b) => a.timestamp - b.timestamp);
        } else if (machineType === "Packing Machine") {
          data = rawData.map((item) => ({
            ...item,
            machineType,
            timestamp: new Date(item.timestamp).getTime(),
            actualWeight: item.actualWeight || 0,
            setWeight: item.bagWeightSet || 0,
            totalWeight: item.accumulatedWeight || 0,
            machineStatus:
              item.status === 1 || item.machineStatus === "RUNNING"
                ? "Running"
                : "Stopped",
          }));

          if (data.length > 0 && data[0].timestamp > startTimestamp) {
            data.unshift({
              timestamp: startTimestamp,
              actualWeight: 0,
              setWeight: 0,
              totalWeight: 0,
              machineStatus: "Stopped",
              batchName: data[0].batchName || "",
              machineType,
            });
          }

          data.sort((a, b) => a.timestamp - b.timestamp);
        } else {
          console.warn("Unknown machinetype:", machineType);
          data = [];
        }

        setDeviceData(data);
        setLastUpdated(new Date());
      })
      .catch((err) => {
        console.error("Error fetching device data:", err);
        setDeviceData([]);
      });
  };

const fetchMonthlyData = async () => {
  if (!selectedDeviceId) return;

  const requestBody = {
    deviceId: Number(selectedDeviceId),
    month: Number(selectedMonth),
    year: Number(selectedYear),
  };

  console.log("Sending monthly request:", requestBody);

  try {
    const response = await axios.post(`${config.BASE_URL}/user/data`, requestBody);

    console.log("Monthly response received:", response.data);

    if (response.data && response.data.type === "monthly") {
      const summary = response.data.summary || [];
      const formatted = summary.map((item) => ({
        label: item.label,
        totalWeight: item.totalWeight,
      }));
      setSummaryData(formatted);
      setGrandTotal(response.data.grandTotal || 0);
      setViewType("monthly");
    } else {
      console.warn("Unexpected monthly data format", response.data);
      setSummaryData([]);
    }
  } catch (error) {
    console.error("Error fetching monthly data:", error);
    console.log("Error response:", error.response?.data);
    setSummaryData([]);
  }
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
  const lastData = deviceData[deviceData.length - 1] || {};

return (
  <div id="move1">
    <div className="device-selector1">
      <p><strong>Select:</strong></p>
      <select
        value={selectedDeviceId}
        onChange={(e) => setSelectedDeviceId(e.target.value)}
      >
        <option value="">-- Select --</option>
        {devices.map((device, index) => (
          <option key={device.deviceId || index} value={device.deviceIds}>
            {device.deviceName}
          </option>
        ))}
      </select>
    </div>

    <div style={{ marginLeft: "1114px", marginTop: "-18px" }}>
      <strong>Last Updated:</strong>{" "}
      {lastUpdated ? lastUpdated.toLocaleTimeString() : "..."}
    </div>

    <div className="batch-title1" style={{ marginTop: "-18px" }}>
      Machine Type: {lastData.machinetype || "Unknown"}
    </div>

    <div className="data-section1">
      {selectedDeviceId && deviceData.length > 0 ? (
        <div className="data-card1">
          {lastData.machinetype === "Flow Veyor" ? (
            <>
              <div
                className="data-row1"
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  flexWrap: "wrap",
                  marginBottom: "10px",
                }}
              >
                <div className="data-box1">
                  <strong>Machine Status:</strong>
                  <br />
                  <span
                    style={{
                      color:
                        lastData.machineStatus === "Running" ? "green" : "red",
                      fontWeight: "bold",
                    }}
                  >
                    {lastData.machineStatus}
                  </span>
                </div>
                <div className="data-box1">
                  <strong>Timestamp:</strong>
                  <br />
                  {new Date(lastData.timestamp).toLocaleString()}
                </div>
                <div className="data-box1">
                  <strong>Set Weight:</strong>
                  <br />
                  {lastData.setWeight}
                </div>
                <div className="data-box1">
                  <strong>Total Weight:</strong>
                  <br />
                  {lastData.totalWeight}
                </div>
              </div>

              <div
                className="batch-title1"
                style={{
                  textAlign: "left",
                  fontWeight: "bold",
                  fontSize: "16px",
                  marginBottom: "10px",
                }}
              >
                Batch Name: {lastData.batchName || "N/A"}
              </div>

              <div
                style={{
                  marginTop: "10px",
                  textAlign: "center",
                  fontWeight: "bold",
                  fontSize: "16px",
                  color: "#333",
                  display: "flex",
                  justifyContent: "center",
                  gap: "10px",
                }}
              >
                <button onClick={() => setViewType("daily")}>Daily</button>
                <button onClick={fetchMonthlyData}>Monthly</button>
                <button onClick={() => setViewType("yearly")}>Yearly</button>
                <button onClick={() => setViewType("history")}>History</button>
              </div>

              {viewType === "monthly" && (
                <div style={{ marginTop: "20px" }}>
                  {/* Horizontal Month Strip */}
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      overflowX: "auto",
                      whiteSpace: "nowrap",
                      gap: "8px",
                      padding: "10px 0",
                      borderBottom: "1px solid #ccc",
                    }}
                  >
                    <span style={{ fontSize: "20px", fontWeight: "bold" }}>←</span>

                    {[
                      "January", "February", "March", "April", "May", "June",
                      "July", "August", "September", "October", "November", "December"
                    ].map((month, index) => (
                      <button
                        key={index}
                        onClick={() => {
                          setSelectedMonth(index + 1);
                          fetchMonthlyData();
                        }}
                        style={{
                          // backgroundColor: selectedMonth === index + 1 ? "#007bff" : "#f0f0f0",
                          // color: selectedMonth === index + 1 ? "#fff" : "#000",
                          border: "none",
                          borderRadius: "20px",
                          padding: "8px 16px",
                          fontWeight: "bold",
                          fontSize: "14px",
                          cursor: "pointer",
                          whiteSpace: "nowrap",
                        }}
                      >
                        {month}
                      </button>
                    ))}

                    <span style={{ fontSize: "20px", fontWeight: "bold" }}>→</span>
                  </div>

                  {/* Year Input */}
                  <div style={{ marginTop: "10px", textAlign: "center" }}>
                    <strong>Year: </strong>
                    <input
                      type="number"
                      value={selectedYear}
                      onChange={(e) => {
                        setSelectedYear(e.target.value);
                        fetchMonthlyData();
                      }}
                      min="2000"
                      max={new Date().getFullYear()}
                      style={{
                        width: "80px",
                        padding: "6px",
                        borderRadius: "4px",
                        border: "1px solid #ccc", 
                        textAlign: "center",
                      }}
                    />
                  </div>

                  {/* Grand Total */}
                  <div style={{ textAlign: "center", margin: "10px 0" }}>
                    <strong>Grand Total:</strong> {grandTotal.toLocaleString()} kg
                  </div>

                  {/* Monthly Bar Chart */}
                  <div style={{ width: "100%", height: 400 }}>
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={summaryData}
                        margin={{ top: 20, right: 60, left: 60, bottom: 50 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                          dataKey="label"
                          tick={{ fontSize: 12 }}
                          label={{
                            value: "Date",
                            position: "insideBottom",
                            offset: -5,
                            fontSize: 14,
                          }}
                        />
                        <YAxis
                          tickFormatter={(value) => value.toLocaleString()}
                          label={{
                            value: "Total Weight (kg)",
                            angle: -90,
                            position: "insideLeft",
                            fontSize: 14,
                          }}
                        />
                        <Tooltip
                          formatter={(value) => value.toLocaleString()}
                          labelStyle={{ fontWeight: "bold" }}
                        />
                        <Bar dataKey="totalWeight" fill="#ff9100ff" barSize={40} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              )}
            </>
          ) : (
            <>
              <div className="batch-title1">Batch ID: {lastData.batchId || "N/A"}</div>
              <div className="data-row1">
                <div className="data-box1"><strong>Timestamp:</strong><br />{new Date(lastData.timestamp).toLocaleString()}</div>
                <div className="data-box1"><strong>Bag Weight Set:</strong><br />{lastData.setWeight}</div>
                <div className="data-box1"><strong>Actual Weight:</strong><br />{lastData.actualWeight}</div>
                <div className="data-box1"><strong>Bag Count:</strong><br />{lastData.bagCount}</div>
                <div className="data-box1"><strong>Accumulated Weight:</strong><br />{lastData.accumulatedWeight}</div>
                <div className="data-box1"><strong>Scale Used:</strong><br />{lastData.scaleUsed}</div>
              </div>
            </>
          )}

          <div className="data-chart-scroll">
            <div className="data-chart-container">
              <ResponsiveContainer width="100%" height="100%">
                {viewType !== "monthly" && (
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
                    <Tooltip labelFormatter={(label) => new Date(label).toLocaleString()} />
                    <ReferenceLine y={0} stroke="grey" strokeDasharray="5 5" />
                    <Area
                      type="monotone"
                      dataKey="totalWeight"
                      stroke="#ff9100ff"
                      fill="#ff9100ff"
                      strokeWidth={2}
                      dot={{ r: 0 }}
                      activeDot={{ r: 6 }}
                    />
                    <Line
                      type="monotone"
                      dataKey="totalWeight"
                      stroke="#ff9100ff"
                      strokeWidth={2}
                      dot={{ r: 0 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                )}
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
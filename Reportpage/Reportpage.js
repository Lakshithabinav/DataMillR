import React, { useState } from "react";
import axios from "axios";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from "recharts";
import "./Reportpage.css";
import config from "../config";
import { useNavigate } from "react-router-dom";

const Reportpage = () => {
  const [reportData, setReportData] = useState([]);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [machineType, setMachineType] = useState("");
  const [rawBatchData, setRawBatchData] = useState([]); // <-- added this

  const navigate = useNavigate();

  const startOfDay = new Date(fromDate || Date.now()).setHours(0, 0, 0, 0);
  const endOfDay = new Date(toDate || Date.now()).setHours(23, 59, 59, 999);

  
// 🔧 Add this function here
function formatDateTime(timestamp) {
  const dateObj = new Date(timestamp);
  const date = dateObj.toLocaleDateString("en-GB"); // DD/MM/YYYY
  const time = dateObj.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
  return `${date} | ${time}`;
} 

  const handleSubmit = async () => {
  if (!fromDate || !toDate) {
    alert("Please select both from and to dates.");
    return;
  }

  const userData = JSON.parse(sessionStorage.getItem("userData"));

  if (
    !userData ||
    !userData.devices ||
    !userData.devices[0] ||
    !userData.devices[0].deviceIds[0]
  ) {
    alert("Session expired or Device ID missing. Please log in again.");
    navigate("/");
    return;
  }

  const deviceId = userData.devices[0].deviceIds[0];
  const deviceName = userData.devices[0].deviceName || ""; // <-- added this

  const requestBody = {
    deviceId: deviceId,
    startDate: fromDate,
    endDate: toDate,
    deviceName: deviceName,
  };

  try {
    const res = await axios.post(`${config.BASE_URL}/user/data`, requestBody);

    const from = new Date(fromDate);
    const to = new Date(toDate);
    to.setHours(23, 59, 59, 999);

    if (Array.isArray(res.data)) {
      const filtered = res.data.filter((item) => {
        const ts = new Date(item.timestamp).getTime();
        return ts >= from.getTime() && ts <= to.getTime();
      });

      if (filtered.length > 0) {
        setMachineType(filtered[0].machinetype || "Packing Machine");
      }

      setReportData(filtered);
    } else {
      const rawBatch = res.data.batch || [];
      const extracted = [];

      rawBatch.forEach((batchItem) => {
        const allPoints = [
          ...(batchItem.batchdata || []),
          ...(batchItem.batchStartdata ? [batchItem.batchStartdata] : []),
          ...(batchItem.batchEnddata ? [batchItem.batchEnddata] : []),
        ];

        allPoints.forEach((item) => {
          const ts = new Date(item.timestamp).getTime();
          if (ts >= from.getTime() && ts <= to.getTime()) {
            extracted.push({
              ...item,
              timestamp: item.timestamp,
              presentWeight: item.presentWeight,
              totalWeight: parseFloat(item.totalWeight) || 0,
              batchName: item.batchName,
              statusText: item.status === 1 ? "Running" : "Stop",
            });
          }
        });
      });

      if (extracted.length > 0) {
        setMachineType("Flow Vayor");
      }

      setRawBatchData(rawBatch); // <-- store the raw batch data
      setReportData(extracted);
    }

    setSubmitted(true);
    setSelectedBatch(null);
  } catch (err) {
    console.error("Error fetching data:", err);
    alert("Failed to fetch report data. Please try again.");
  }
};


  const groupByBatch = () => {
    const grouped = {};
    reportData.forEach((item) => {
      if (!grouped[item.batchName]) {
        grouped[item.batchName] = [];
      }
      grouped[item.batchName].push(item);
    });
    return grouped;
  };

  const groupedData = groupByBatch();

  const formatTime = (t) =>
    new Date(t).toLocaleTimeString("en-IN", { hour: "numeric", hour12: true });

  const handleBack = () => {
    setSelectedBatch(null);
    setTimeout(() => {
      const container = document.getElementById("move");
      if (container) {
        container.scrollTo({ top: 0, behavior: "smooth" });
      }
    }, 100);
  };

const handleExcel = async () => {
  const userData = JSON.parse(sessionStorage.getItem("userData"));

  if (
    !userData ||
    !userData.devices ||
    !userData.devices[0] ||
    !userData.devices[0].deviceIds[0]
  ) {
    alert("Session expired or device info missing. Please log in again.");
    navigate("/");
    return;
  }

  const deviceId = userData.devices[0].deviceIds[0];
  const deviceName =
    typeof userData.devices[0].deviceName === "string"
      ? userData.devices[0].deviceName
      : userData.devices[0].deviceName[0];

  const machinetype = machineType || userData.devices[0].machinetype || "UnknownMachine";

  if (!fromDate || !toDate) {
    alert("Please select both From and To dates.");
    return;
  }

  const payload = {
    deviceId,
    startDate: fromDate,
    endDate: toDate,
    deviceName,
  };

  try {
    const response = await axios.post(
      `${config.BASE_URL}/user/export-excel`,
      payload,
      {
        responseType: "blob",
        headers: { "Content-Type": "application/json" },
      }
    );

    const blob = new Blob([response.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const formattedFrom = new Date(fromDate).toISOString().split("T")[0];
    const formattedTo = new Date(toDate).toISOString().split("T")[0];
    const safeDeviceName = deviceName.replace(/\s+/g, "_");
    const safeMachineType = machinetype.replace(/\s+/g, "_");

    const filename = `Report_${safeDeviceName}_${safeMachineType}_${formattedFrom}_to_${formattedTo}.xlsx`;

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    console.log("✅ Excel file downloaded:", filename);
  } catch (err) {
    console.error("❌ Failed to export Excel:", err);
    alert("Failed to export Excel. Please try again later.");
  }
};


const batchSummaries = rawBatchData.map((batch) => {
    const start = batch.batchStartdata;
    const end = batch.batchEnddata;
    return {
      batchName: end?.batchName || "N/A",
      totalWeight: parseFloat(end?.totalWeight) || 0,
      startTime: start?.timestamp ? formatDateTime(start.timestamp) : "-",
      endTime: end?.timestamp ? formatDateTime(end.timestamp) : "-",
      statusText: end?.status === 1 ? "Running" : "Stop",
    };
  });

  return (
    <div id="move">
      <h2 className="report-heading">Report Data</h2>

      <div className="date-filter">
        <label>
          From Date:
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
        </label>
        <label>
          To Date:
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </label>

        {selectedBatch ? (
          <button onClick={handleBack}>Back</button>
        ) : (
          <button onClick={handleSubmit}>Submit</button>
        )}
        <button onClick={handleExcel}>Export Excel</button>
      </div>

      {submitted && machineType === "Packing Machine" && (
        <div className="table-container">
          <table className="styled-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Batch ID</th>
                <th>Bag Weight Set</th>
                <th>Actual Weight</th>
                <th>Scale Used</th>
                <th>Bag Count</th>
                <th>Accumulated Weight</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {reportData.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: "center" }}>
                    No data found for selected date range.
                  </td>
                </tr>
              ) : (
                reportData.map((item, index) => (
                  <tr key={index}>
                    <td>{new Date(item.timestamp).toLocaleString()}</td>
                    <td>{item.batchId}</td>
                    <td>{item.bagWeightSet}</td>
                    <td>{item.actualWeight}</td>
                    <td>{item.scaleUsed}</td>
                    <td>{item.bagCount}</td>
                    <td>{item.accumulatedWeight}</td>
                    <td>{item.machineStatus}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {submitted && machineType === "Flow Vayor" && (
        <div className="scrollable-content">
          {!selectedBatch ? (
            <div className="table-container">
              <table className="styled-table">
                <thead>
                  <tr>
                    <th>Start Date / Start Time</th>
                    <th>End Date / End Time</th>
                    <th>Batch</th>
                    <th>Total</th>
                    {/* <th>Last Updated Time</th> */}
                    {/* <th>Status</th> */}
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {reportData.length === 0 ? (
                    <tr>
                      <td colSpan="5" style={{ textAlign: "center" }}>
                        No data found for selected date range.
                      </td>
                    </tr>
                  ) : (
                    batchSummaries.map((r, i) => (
                      <tr key={i}>
                        <td>{r.startTime}</td>
                        <td>{r.endTime}</td>
                        <td>{r.batchName}</td>
                        <td>{r.totalWeight}</td>
                        {/* <td>{new Date(r.timestamp).toLocaleTimeString()}</td> */}
                        {/* <td>{r.statusText}</td> */}
                        <td>
                          <button onClick={() => setSelectedBatch(r.batchName)}>
                            View
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="data-card">
              <div className="data-header">
                <strong>Batch:</strong> {selectedBatch}
              </div>

              <div className="chart-container-scroll">
                <div className="chart-container">
                  {(() => {
                    const sortedData = groupedData[selectedBatch]
                      .map((d) => ({
                        ...d,
                        timestamp: new Date(d.timestamp).getTime(),
                        totalWeight: parseFloat(d.totalWeight) || 0,
                      }))
                      .sort((a, b) => a.timestamp - b.timestamp);

                    return (
                      <ResponsiveContainer width={1000} height={300}>
                        <AreaChart
                          data={sortedData}
                          margin={{ top: 20, right: 40, left: 40, bottom: 50 }}
                        >
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis
                            dataKey="timestamp"
                            type="number"
                            scale="time"
                            domain={["dataMin", "dataMax"]}
                            ticks={[
                              sortedData[0]?.timestamp,
                              sortedData[sortedData.length - 1]?.timestamp,
                            ]}
                            tickFormatter={(t) =>
                              new Date(t).toLocaleTimeString("en-IN", {
                                hour: "2-digit",
                                minute: "2-digit",
                              })
                            }
                            tick={{ fontSize: 12, angle: -45, textAnchor: "end" }}
                            interval={0}
                          />
                          <YAxis />
                          <Tooltip
                            labelFormatter={(lbl) =>
                              new Date(lbl).toLocaleString()
                            }
                          />
                          <ReferenceLine y={0} stroke="grey" strokeDasharray="5 5" />
                          <Area
                            type="monotone"
                            dataKey="totalWeight"
                            stroke="#ff9100ff"
                            fill="#ff9100ff"
                            strokeWidth={2}
                            dot={{ fill: "#ff9100ff", r: 3 }}
                            activeDot={{ r: 5 }}
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    );
                  })()}
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Reportpage;
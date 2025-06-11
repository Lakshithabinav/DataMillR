import React, { useState } from "react";
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
import "./Reportpage.css";

const Reportpage = () => {
  const [reportData, setReportData] = useState([]);
  const [selectedRow, setSelectedRow] = useState(null);
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = () => {
    if (fromDate && toDate) {
      axios
        .get("http://localhost:8082/api/upload-data", {
          params: { from: fromDate, to: toDate },
        })
        .then((response) => {
          setReportData(response.data);
          setSubmitted(true);
          setSelectedRow(null); // Reset selected row when new data is fetched
        })
        .catch((error) => {
          console.error("Error fetching report data:", error);
        });
    }
  };

  const graphData = () => {
    if (!selectedRow) return [];
    const startOfDay = new Date(selectedRow.timestamp);
    startOfDay.setHours(0, 0, 0, 0);
    return [
      {
        timestamp: startOfDay.getTime(),
        setWeight: 0,
        actualWeight: 0,
        totalWeight: 0,
        machineRunning: false,
        batchName: selectedRow.batchName,
      },
      selectedRow,
    ];
  };

  const getXTicks = () => {
    const ticks = [];
    const base = new Date();
    base.setHours(0, 0, 0, 0);
    for (let h = 0; h <= 24; h += 3) {
      const t = new Date(base);
      t.setHours(h);
      ticks.push(t.getTime());
    }
    return ticks;
  };

  const formatTime = (t) =>
    new Date(t).toLocaleTimeString("en-IN", { hour: "numeric", hour12: true });

  return (
    <div id="move">
      <h2 style={{ color: "#5d5a00" }}>Report Data</h2>

      <div
        className="date-filter"
        style={{ display: "flex", alignItems: "center", marginBottom: "1rem" }}
      >
        <label style={{ color: "#5d5a00", marginRight: "1rem" }}>
          From Date:
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            style={{ marginLeft: "0.5rem" }}
          />
        </label>
        <label style={{ color: "#5d5a00", marginRight: "1rem" }}>
          To Date:
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            style={{ marginLeft: "0.5rem" }}
          />
        </label>
        <button
          onClick={handleSubmit}
          style={{
            padding: "0.5rem 1rem",
            backgroundColor: "#5d5a00",
            color: "white",
            border: "none",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          Submit
        </button>
      </div>

      {submitted && fromDate && toDate && (
        <div className="scrollable-content">
          {!selectedRow ? (
            <div className="table-container">
              <table className="styled-table">
                <thead>
                  <tr>
                    <th>Device</th>
                    <th>Batch</th>
                    <th>Total</th>
                    <th>Set</th>
                    <th>Actual</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {reportData.map((r) => (
                    <tr key={r.id}>
                      <td>{r.deviceId}</td>
                      <td>{r.batchName}</td>
                      <td>{r.totalWeight}</td>
                      <td>{r.setWeight}</td>
                      <td>{r.actualWeight}</td>
                      <td>{new Date(r.timestamp).toLocaleTimeString()}</td>
                      <td style={{ color: r.machineRunning ? "green" : "red" }}>
                        {r.machineRunning ? "Running" : "Stopped"}
                      </td>
                      <td>
                        <button onClick={() => setSelectedRow(r)}>View</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="data-card">
              <div className="data-header">
                <div>
                  <strong>Machine Status:</strong>{" "}
                  <span style={{ color: selectedRow.machineRunning ? "green" : "red" }}>
                    {selectedRow.machineRunning ? "Running" : "Stopped"}
                  </span>
                </div>
                <div>
                  <strong>Time:</strong>{" "}
                  {new Date(selectedRow.timestamp).toLocaleString()}
                </div>
              </div>

              <div className="data-row">
                <div className="data-box">
                  <strong>Batch:</strong><br />{selectedRow.batchName}
                </div>
                <div className="data-box">
                  <strong>Set Weight:</strong><br />{selectedRow.setWeight}
                </div>
                <div className="data-box">
                  <strong>Actual Weight:</strong><br />{selectedRow.actualWeight}
                </div>
                <div className="data-box">
                  <strong>Total Weight:</strong><br />{selectedRow.totalWeight}
                </div>
              </div>

              <div className="chart-container">
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart
                    data={graphData()}
                    margin={{ top: 20, right: 40, left: 40, bottom: 50 }}
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
                      tickFormatter={formatTime}
                      tick={{ fontSize: 12 }}
                    />
                    <YAxis />
                    <Tooltip labelFormatter={(lbl) => new Date(lbl).toLocaleString()} />
                    <ReferenceLine y={0} stroke="grey" strokeDasharray="5 5" />
                    <Line
                      type="monotone"
                      dataKey="totalWeight"
                      stroke="#ff5555"
                      strokeWidth={2}
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>

              <div style={{ textAlign: "right", marginTop: "1rem" }}>
                <button
                  style={{
                    padding: "0.5rem 1rem",
                    background: "#5D5C61",
                    color: "white",
                    border: "none",
                    borderRadius: "6px",
                    cursor: "pointer",
                  }}
                  onClick={() => setSelectedRow(null)}
                >
                  Back
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Reportpage;

import React, { useState } from "react";
import axios from "axios";

const Reportpage = () => {
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [data, setData] = useState([]);
  const [columns, setColumns] = useState([]);

  const fetchData = async () => {
    if (!fromDate || !toDate) {
      alert("Please select both From and To dates.");
      return;
    }

    try {
      const response = await axios.post("http://localhost:8082/user/report", {
        fromDate,
        toDate,
      });

      const result = response.data;
      setData(result);
      if (result.length > 0) {
        setColumns(Object.keys(result[0]));
      } else {
        setColumns([]);
      }
    } catch (error) {
      console.error("Error fetching report data:", error);
      alert("Failed to fetch data.");
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Device Report</h2>
      <div style={{ marginBottom: "15px" }}>
        <label>
          From Date:{" "}
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
        </label>
        &nbsp;&nbsp;
        <label>
          To Date:{" "}
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </label> 
        &nbsp;&nbsp;
        <button onClick={fetchData}>Submit</button>
      </div>

      {data.length > 0 ? (
        <table border="1" cellPadding="8">
          <thead>
            <tr>
              {columns.map((col, idx) => (
                <th key={idx}>{col}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {columns.map((col, colIndex) => (
                  <td key={colIndex}>{row[col]}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No data found.</p>
      )}
    </div>
  );
};

export default Reportpage;

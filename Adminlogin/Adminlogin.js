import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Adminlogin.css";

const AdminLogin = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loginMessage, setLoginMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate(); 

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);

    setTimeout(() => {
      if (username === "Admin0000" && password === "Admin") {
        setLoading(false);
        navigate("/admindashboard"); 
      } else {
        setLoading(false);
        setLoginMessage("Invalid admin credentials.");
      }
    }, 1000); 
  };

  return (
    <div className="container1">
      {loading && <div className="loading-message">Loading...</div>}

      <div className="left-section1">
        <h1 className="company-name">DataMillr</h1>
      </div>

      <div className="right-section1">
        <h1>Admin Login</h1>
        <form onSubmit={handleSubmit}>
          <label>Admin ID</label>
          <input
            id="hi1"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <label>Password</label>
          <input
            id="hi1"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button type="submit" disabled={loading}>Login</button>
        </form>
        {loginMessage && <p className="login-message1">{loginMessage}</p>}
      </div>
    </div>
  );
};

export default AdminLogin;

import React, { useState } from "react";
import "./Adminlogin.css";

const AdminLogin = ({ onAdminSuccess }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loginMessage, setLoginMessage] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (username === "Admin0000" && password === "Admin") {
      onAdminSuccess();
    } else {
      setLoginMessage("Invalid admin credentials.");
    }
  };

  return (
    <div className="container">
      <div className="left-section">
        <h1 className="company-name">DataMillr</h1>
      </div>
      <div className="right-section">
        <h1>Admin Login</h1>
        <form onSubmit={handleSubmit}>
          <label>Admin ID</label>
          <input id="hi"value={username} onChange={e => setUsername(e.target.value)} />
          <label>Password</label>
          <input id="hi"type="password" value={password} onChange={e => setPassword(e.target.value)} />
          <button type="submit">Login</button>
        </form>
        {loginMessage && <p className="login-message">{loginMessage}</p>}
      </div>
    </div>
  );
};

export default AdminLogin;

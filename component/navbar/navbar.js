import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './navbar.css';

function Navbar() {
  const userData = sessionStorage.getItem("userData");
  const userDataJS = userData ? JSON.parse(userData) : null;
  const [view, setView] = useState('');
  const navigate = useNavigate();
  const location = useLocation();

  // Hide navbar on new user creation page
  if (location.pathname === '/newuserlogin') return null;

  function handleViewChange(view) {
    setView(view);
    navigate(`/${view}`);
  }

  function handleLogout() {
    sessionStorage.clear();
    navigate("/");
  }

  function handleProfile() {
    navigate("/userprofile");
  }

  return (
    <>
      <div className="navbar-container">
        <img
          src="https://raisonautomation.com/wp-content/uploads/2024/06/Raison-New-Logo-3.png"
          alt="Left Logo"
          className="navbar-logo"
        />
        <div className="company-name">
          {userDataJS ? userDataJS.companyName : ""}
        </div>
        <img
          src="https://emmppe.com/images/logo.png"
          alt="Right Logo"
          className="navbar-logo"
        />
      </div>

      {userDataJS && (
        <div className="topbar">
          <div className="topbar-menu">
            <span
              onClick={() => handleViewChange("userdashboard")}
              className={view === "userdashboard" ? "active" : ""}
            >
              Current Status
            </span>
            /
            <span
              onClick={() => handleViewChange("report")}
              className={view === "report" ? "active" : ""}
              style={{ marginLeft: "10px" }}
            >
              Report
            </span>
          </div>

          <div className="topbar-buttons">
            <button className="profile-button" onClick={handleProfile}>
              Profile
            </button>
            <button className="logout-button" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      )}
    </>
  );
}

export default Navbar;

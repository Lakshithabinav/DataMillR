import React, { useState, useEffect } from "react";
import Timesheet from "./Userlogin/Userlogin";
import AdminLogin from "./Adminlogin/Adminlogin";
import UserDashboard from "./Userdashboard/Userdashboard";
import AdminDashboard from "./Admindashboard/Dashboard";
import Reportpage from "./Reportpage/Reportpage";
import Newuserlogin from "./Userlogin/Newuserlogin";
import "./App.css"; // Ensure this CSS is present

function App() {
  const [view, setView] = useState("login");

  useEffect(() => {
    const path = window.location.pathname;

    if (path === "/admin") {
      const adminLoggedIn = sessionStorage.getItem("adminAuthenticated");
      setView(adminLoggedIn === "true" ? "adminDashboard" : "adminLogin");
    } else if (path === "/user") {
      const userData = sessionStorage.getItem("userData");
      const finalUserId = sessionStorage.getItem("finalUserId");
      const finalUserPassword = sessionStorage.getItem("finalUserPassword");

      if (userData || (finalUserId && finalUserPassword)) {
        setView("userDashboard");
      } else {
        setView("login");
      }
    } else if (path === "/report") {
      const userData = sessionStorage.getItem("userData");
      setView(userData ? "report" : "login");
    } else if (path === "/newuser") {
      setView("newUserLogin");
    } else {
      setView("login");
    }
  }, []);

  const handleNewUserDetected = () => {
    window.history.pushState({}, "", "/newuser");
    setView("newUserLogin");
  };

  const handleUserLoginSuccess = () => {
    window.history.pushState({}, "", "/user");
    setView("userDashboard");
  };

  const handleAdminLoginSuccess = () => {
    sessionStorage.setItem("adminAuthenticated", "true");
    window.history.pushState({}, "", "/admin");
    setView("adminDashboard");
  };

  const handleViewChange = (newView) => {
    const path = newView === "report" ? "/report" : "/user";
    window.history.pushState({}, "", path);
    setView(newView);
  };

  const handleLogout = () => {
    sessionStorage.clear();
    window.location.href = "/";
  };

  const renderTopbar = () => {
    if (view === "userDashboard" || view === "report") {
      return (
        <div
          className="topbar"
          style={{
            position: "fixed",
            top: "60px",
            left: 0,
            width: "100%",
            backgroundColor: "#5D5C61",
            color: "white",
            display: "flex",
            alignItems: "center",
            padding: "12px 20px",
            fontWeight: "bold",
            fontSize: "20px",
            zIndex: 999,
          }}
        >
          <div
            style={{
              flex: 1,
              display: "flex",
              justifyContent: "center",
              gap: "10px",
            }}
          >
            <span
              onClick={() => handleViewChange("userDashboard")}
              style={{
                cursor: "pointer",
                color: view === "userDashboard" ? "white" : "lightgray",
                textDecoration:
                  view === "userDashboard" ? "underline" : "none",
              }}
            >
              Current Status
            </span>
            /
            <span
              onClick={() => handleViewChange("report")}
              style={{
                cursor: "pointer",
                color: view === "report" ? "white" : "lightgray",
                textDecoration: view === "report" ? "underline" : "none",
                marginLeft: "10px",
              }}
            >
              Report
            </span>
          </div>

          <button
            onClick={handleLogout}
            style={{
              backgroundColor: "white",
              color: "#5D5C61",
              border: "none",
              borderRadius: "5px",
              padding: "8px 15px",
              cursor: "pointer",
              fontWeight: "bold",
            }}
          >
            Logout
          </button>
        </div>
      );
    }
    return null;
  };

  const containerClass = () => {
    if (view === "adminLogin") return "scrollable-container";
    return "scrollable-container hide-scrollbar";
  };

  return (
    <div className="App" style={{ height: "100vh", overflow: "hidden" }}>
      {/* Fixed Logo Bar */}
      <div
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          width: "100%",
          height: "60px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          padding: "0 40px",
          zIndex: 1000,
          background: "transparent",
        }}
      >
        <img
          src="https://raisonautomation.com/wp-content/uploads/2024/06/Raison-New-Logo-3.png"
          alt="Left Logo"
          style={{ height: "45px", objectFit: "contain" }}
        />
        <img
          src="https://emmppe.com/images/logo.png"
          alt="Right Logo"
          style={{ height: "45px", objectFit: "contain" }}
        />
      </div>

      {/* Scrollable Content Area */}
      <div
        className={containerClass()}
        style={{
          marginTop:
            view === "userDashboard" || view === "report" ? "120px" : "80px",
          height:
            view === "userDashboard" || view === "report"
              ? "calc(100vh - 120px)"
              : "calc(100vh - 80px)",
        }}
      >
        {renderTopbar()}
        {view === "login" && (
          <Timesheet
            onLoginSuccess={handleUserLoginSuccess}
            onNewUserDetected={handleNewUserDetected}
          />
        )}
        {view === "adminLogin" && (
          <AdminLogin onAdminSuccess={handleAdminLoginSuccess} />
        )}
        {view === "userDashboard" && <UserDashboard />}
        {view === "adminDashboard" && <AdminDashboard />}
        {view === "report" && <Reportpage />}
        {view === "newUserLogin" && <Newuserlogin />}
      </div>
    </div>
  );
}

export default App;

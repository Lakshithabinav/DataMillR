import React, { useState, useEffect } from "react";
import Timesheet from "./Userlogin/Userlogin";
import AdminLogin from "./Adminlogin/Adminlogin";
import UserDashboard from "./Userdashboard/Userdashboard";
import AdminDashboard from "./Admindashboard/Dashboard";

function App() {
  const [view, setView] = useState("login");

  useEffect(() => {
    const path = window.location.pathname;

    if (path === "/admin") {
      const adminLoggedIn = sessionStorage.getItem("adminAuthenticated");
      setView(adminLoggedIn === "true" ? "adminDashboard" : "adminLogin");
    } else if (path === "/user") {
      const userData = sessionStorage.getItem("userData");
      setView(userData ? "userDashboard" : "login");
    } else {
      setView("login");
    }
  }, []);

  const handleUserLoginSuccess = () => {
    window.history.pushState({}, "", "/user");
    setView("userDashboard");
  };

  const handleAdminLoginSuccess = () => {
    sessionStorage.setItem("adminAuthenticated", "true");
    window.history.pushState({}, "", "/admin");
    setView("adminDashboard");
  };

  return (
    <div className="App">
      {view === "login" && <Timesheet onLoginSuccess={handleUserLoginSuccess} />}
      {view === "adminLogin" && <AdminLogin onAdminSuccess={handleAdminLoginSuccess} />}
      {view === "userDashboard" && <UserDashboard />}
      {view === "adminDashboard" && <AdminDashboard />}
    </div>
  );
}

export default App;

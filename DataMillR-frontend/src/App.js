import React, { useState, useEffect } from "react";
import Timesheet from "./Userlogin/Userlogin";
import AdminLogin from "./Adminlogin/Adminlogin";
import UserDashboard from "./Userdashboard/Userdashboard";
import AdminDashboard from "./Admindashboard/Dashboard";
import Reportpage from "./Reportpage/Reportpage";
import Newuserlogin from "./Userlogin/Newuserlogin"; // ✅ Import new user login component

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
    } else if (path === "/report") {
      const userData = sessionStorage.getItem("userData");
      setView(userData ? "report" : "login");
    } else if (path === "/newuser") {
      setView("newUserLogin"); // ✅ New condition for forgot password
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
      {view === "report" && <Reportpage />}
      {view === "newUserLogin" && <Newuserlogin />} {/* ✅ Render Newuserlogin component */}
    </div>
  );
}

export default App;

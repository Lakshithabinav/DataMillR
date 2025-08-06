import React, { useState, useEffect } from "react";
import Timesheet from "./Userlogin/Userlogin";
import "./App.css";
import { BrowserRouter as Router, Routes, Route, useLocation } from "react-router-dom";
import NetworkStatusProvider from "./context/NetworkStatusContext";

import AdminLogin from "./Adminlogin/Adminlogin";
import UserDashboard from "./Userdashboard/Userdashboard";
import AdminDashboard from "./Admindashboard/Dashboard";
import Reportpage from "./Reportpage/Reportpage";
import Newuserlogin from "./Userlogin/Newuserlogin";
import Navbar from "./component/navbar/navbar";
import LoadingOverlay from "./component/LoadingOverlay/LoadingOverlay";
// import ServerDown from "./Pages/ServerDown/ServerDown";
import UserProfile from "./UserProfile/UserProfile"; // ✅ import UserProfile

function AppWrapper() {
  const location = useLocation();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const timeout = setTimeout(() => {
      setLoading(false);
    }, 500);
    return () => clearTimeout(timeout);
  }, [location]);

  return (
    <>
      {loading && <LoadingOverlay />}
      <Navbar />
      <Routes>
        <Route path="/" element={<Timesheet />} />
        <Route path="/admin" element={<AdminLogin />} />
        <Route path="/userdashboard" element={<UserDashboard />} />
        <Route path="/admindashboard" element={<AdminDashboard />} />
        <Route path="/report" element={<Reportpage />} />
        <Route path="/newuserlogin" element={<Newuserlogin />} />
        {/* <Route path="/serverDown" element={<ServerDown />} /> */}
        <Route path="/userprofile" element={<UserProfile />} /> {/* ✅ added profile route */}
      </Routes>
    </>
  );
}

function App() {
  return (
    <Router>
      <NetworkStatusProvider>
        <AppWrapper />
      </NetworkStatusProvider>
    </Router>
  );
}

export default App;

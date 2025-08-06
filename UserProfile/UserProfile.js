import React from "react";
import "./UserProfile.css";

function UserProfile() {
  const userData = sessionStorage.getItem("userData");
  const userDataJS = userData ? JSON.parse(userData) : null;

  return (
    <div className="profile-container">
      <h2>User Profile</h2>
      {userDataJS ? (
        <div className="profile-info">
          <p><strong>Company Name:</strong> {userDataJS.companyName}</p>
          <p><strong>Phone Number:</strong> {userDataJS.phoneNumber || "N/A"}</p>
        </div>
      ) : (
        <p>No user data found.</p>
      )}
    </div>
  );
}

export default UserProfile;

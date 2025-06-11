import React, { useState } from "react";
import "./Newuserlogin.css";

const Newuserlogin = () => {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [finalJson, setFinalJson] = useState(null);

  const handleCreate = () => {
    setError("");
    setSuccess("");

    if (!userId || !password || !confirmPassword) {
      setError("All fields are required.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    // Get old credentials from sessionStorage before removing
    const oldUserId = sessionStorage.getItem("newUserId") || "";
    const oldPassword = sessionStorage.getItem("newUserPassword") || "";

    // Save new credentials to sessionStorage
    sessionStorage.setItem("createdUserId", userId);
    sessionStorage.setItem("createdUserPassword", password);

    // Remove old credentials
    sessionStorage.removeItem("newUserId");
    sessionStorage.removeItem("newUserPassword");
    sessionStorage.removeItem("isNewUser");

    // Create final object and show success
    const result = {
      oldUserId,
      oldPassword,
      newUserId: userId,
      newPassword: password,
    };

    setFinalJson(result);
    setSuccess("Account created successfully! Redirecting to login...");

    // Redirect to login
    setTimeout(() => {
      window.history.pushState({}, "", "/");
      window.location.reload();
    }, 2000);
  };

  return (
    <div style={{ padding: "20px", maxWidth: "400px", margin: "0 auto" }}>
      <h2>Create New ID & Password</h2>
      <input
        type="text"
        placeholder="User ID"
        value={userId}
        onChange={(e) => setUserId(e.target.value)}
        style={{ display: "block", width: "100%", marginBottom: "10px" }}
      />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        style={{ display: "block", width: "100%", marginBottom: "10px" }}
      />
      <input
        type="password"
        placeholder="Confirm Password"
        value={confirmPassword}
        onChange={(e) => setConfirmPassword(e.target.value)}
        style={{ display: "block", width: "100%", marginBottom: "10px" }}
      />

      {error && <p style={{ color: "red" }}>{error}</p>}

      {success && (
        <div
          style={{
            marginTop: "20px",
            padding: "15px",
            backgroundColor: "#e0ffe0",
            border: "1px solid #b2ffb2",
            borderRadius: "8px",
            fontSize: "14px",
          }}
        >
          <strong>✅ Stored Successfully!</strong>
          <p>{success}</p>
          {finalJson && (
            <pre
              style={{
                backgroundColor: "#f4f4f4",
                padding: "10px",
                borderRadius: "5px",
                overflowX: "auto",
                fontSize: "13px",
              }}
            >
              {JSON.stringify(finalJson, null, 2)}
            </pre>
          )}
        </div>
      )}

      <button
        onClick={handleCreate}
        style={{
          backgroundColor: "#5D5C61",
          color: "white",
          border: "none",
          padding: "10px 20px",
          cursor: "pointer",
          fontWeight: "bold",
          borderRadius: "5px",
          marginTop: "10px",
        }}
      >
        Create
      </button>
    </div>
  );
};

export default Newuserlogin;

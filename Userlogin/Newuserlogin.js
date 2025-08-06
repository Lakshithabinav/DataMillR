import React, { useState } from "react";
import "./Newuserlogin.css";
import axios from "axios";
import Popup from "../component/PopUp/Popup";
import { useNavigate } from "react-router-dom";
// import LoadingOverlay from "../component/LoadingOverlay/LoadingOverlay";
import config from "../config";

const Newuserlogin = () => {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [finalJson, setFinalJson] = useState(null);
  const [showPopup, setShowPopup] = useState(false);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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

    const oldUserId = sessionStorage.getItem("oldUserId") || "";
    const oldPassword = sessionStorage.getItem("oldUserPassword") || "";

    const result = {
      oldUserId,
      oldPassword,
      newUserId: userId,
      newPassword: password,
    };

    setLoading(true);

    axios
      .post(`${config.BASE_URL}/api/auth/update-credentials`, result)
      .then((response) => {
        setLoading(false);
        console.log("Credentials updated:", response);
        setMessage(response.data.message);
        setShowPopup(true);

        setTimeout(() => {
          setShowPopup(false);
          sessionStorage.clear();
          navigate('/');
        }, 2000);
      })
      .catch((error) => {
        setLoading(false);
        console.error("Error updating credentials:", error);
        setError("Failed to update credentials. Try again.");
      });
  };

  return (
    <div className="newuser-body">
      {/* {loading && <LoadingOverlay />} */}
      <div className="newuser-container">
        {showPopup && (
          <Popup
            title="Credentials Updated Successfully"
            message={message}
            type="Success"
            onClose={() => setShowPopup(false)}
          />
        )}

        <h2>Change ID & Password</h2>

        <input
          className="newuser-input"
          type="text"
          placeholder="User ID"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
        />
        <input
          className="newuser-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <input
          className="newuser-input"
          type="password"
          placeholder="Confirm Password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />

        {error && <p className="error-message">{error}</p>}

        {success && (
          <div className="success-message">
            <strong>✅ Stored Successfully!</strong>
            <p>{success}</p>
            {finalJson && (
              <pre>{JSON.stringify(finalJson, null, 2)}</pre>
            )}
          </div>
        )}

        <button onClick={handleCreate} className="newuser-button">
          Create
        </button>
      </div>
    </div>
  );
};

export default Newuserlogin;

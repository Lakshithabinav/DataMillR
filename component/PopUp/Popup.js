import React from "react";
import "./Popup.css";

const Popup = ({ message, onClose }) => {
  return (
    <div className="popup-overlay">
      <div className="popup-box">
        <button className="popup-close" onClick={onClose}>
          X
        </button>
        <div className="popup-message">{message}</div>
      </div>
    </div>
  );
};

export default Popup;

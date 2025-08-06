import React from "react";
import "./Popup.css";

const Popup = ({ message, onClose, title = "Notification", type  }) => {
  console.log(type);
  return (
    <div className={`popup-overlay ${type}`}>
      <div className="popup-box popup-animate">
        <button className="popup-close" onClick={onClose}>
          x
        </button>
        <h3 className={`popup-title ${type === "failure" ? "popup-title-failure" : ""}`}>
  {title}
</h3>

        <div className="popup-message">{message}</div>
        
      </div>
    </div>
  );
};

export default Popup;

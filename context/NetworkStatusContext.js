import React, { createContext, useEffect, useState } from "react";

export const NetworkStatusContext = createContext();

const NetworkStatusProvider = ({ children }) => {
  const [isOnline, setIsOnline] = useState(true);

  const checkConnection = async () => {
    try {
      await fetch("https://www.google.com", { mode: "no-cors" });
      setIsOnline(true);
    } catch {
      setIsOnline(false);
    }
  };

  useEffect(() => {
    checkConnection(); 
    const interval = setInterval(checkConnection, 3000); 
    return () => clearInterval(interval);
  }, []);

  return (
    <NetworkStatusContext.Provider value={{ isOnline }}>
      {children}
    </NetworkStatusContext.Provider>
  );
};

export default NetworkStatusProvider;

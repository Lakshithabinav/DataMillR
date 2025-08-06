import React, { useEffect, useState, useContext, useRef } from 'react';
import './Userlogin.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Popup from '../component/PopUp/Popup';
import LoadingOverlay from '../component/LoadingOverlay/LoadingOverlay';
import { NetworkStatusContext } from '../context/NetworkStatusContext';
import config from '../config';

const Timesheet = () => {
  const [userIdInput, setUserIdInput] = useState('');
  const [userPasswordInput, setUserPasswordInput] = useState('');
  const [randomNumber, setRandomNumber] = useState(null);
  const [loginMessage, setLoginMessage] = useState('');
  const [userIp, setUserIp] = useState('');
  // const [finalCreatedJson, setFinalCreatedJson] = useState(null);
  const [showPopup, setShowPopup] = useState(false);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { isOnline } = useContext(NetworkStatusContext);

  const fetchCalledRef = useRef(false); // ✅ Guard for useEffect double call

  const convertToAscii = (input) =>
    input.split('').map((char) => char.charCodeAt(0));

  const processAsciiValues = (asciiArray, randomValue) =>
    asciiArray
      .map((num, i) =>
        i % 2 === 0
          ? parseInt(num) + parseInt(randomValue)
          : num - randomValue
      )
      .join(' ');

  const fetchData = async () => {
    try {
      setLoading(true);

      // 1. Get public IP address
      const ipResponse = await axios.get('https://api.ipify.org?format=json');
      const realIp = ipResponse.data.ip;
      console.log(`User IP: ${realIp}`);
      setUserIp(realIp);

      // 2. Send IP to backend
      const randomResponse = await axios.post(
        `${config.BASE_URL}/api/auth/init`,
        {
          ip: realIp,
        }
      );

      // 3. Handle backend response
      if (randomResponse.data?.randomNumber !== undefined) {
        console.log('Backend Response:', randomResponse.data);
        setRandomNumber(randomResponse.data.randomNumber);
      }
    } catch (err) {
      console.error('Error during init:', err);
      // Optionally navigate to server down page
      // navigate('/serverDown');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log('hii');
    sessionStorage.clear();

    if (!fetchCalledRef.current && isOnline) {
      fetchData();
      fetchCalledRef.current = true;
    } else if (!isOnline) {
      console.log('error');
      setShowPopup(true);
      setMessage('Please connect to the internet and try again.');
    }
  }, []);

  const handleLoginSubmit = (event) => {
    event.preventDefault();
    if (!userIdInput || !userPasswordInput || randomNumber === null) {
      alert(
        'Please fill in all fields and ensure the random number is loaded.'
      );
      return;
    }

    const encodedUserId = processAsciiValues(
      convertToAscii(userIdInput),
      randomNumber
    );
    const encodedPassword = processAsciiValues(
      convertToAscii(userPasswordInput),
      randomNumber
    );
    const finalProcessedResult = `${encodedUserId} 124 124 124 ${encodedPassword}`;
    console.log(finalProcessedResult);

    setLoading(true);
    axios
      .post(`${config.BASE_URL}/api/auth/login`, {
        hashedCredential: finalProcessedResult,
        ip: userIp,
        randomNumber: randomNumber,
      })
      .then((response) => {
        console.log(response.data);
        setLoading(false);
        if (response.data.loginSucess) {
          sessionStorage.setItem('userData', JSON.stringify(response.data));
          if (response.data.newUser) {
            sessionStorage.setItem('oldUserId', userIdInput);
            sessionStorage.setItem('oldUserPassword', userPasswordInput);
            navigate('/newuserlogin');
          } else {
            navigate('/userdashboard');
          }
        } else {
          setMessage('Incorrect User ID or Password. Please try again!');
          setShowPopup(true);
        }
      })
      .catch((error) => {
        console.log('err');
        setMessage(error.response?.data || 'Login failed');
        setShowPopup(true);
        console.log(error);
        setLoading(false);
        //   if (error.message === "Network Error") {
        //     if (isOnline) {
        //       navigate("/serverDown");
        //     } else {
        //       setShowPopup(true);
        //       setMessage("Please connect to the internet and try again.");
        //     }
        //   }
      });
  }; 

  const handleAdminClick = () => {
    window.location.href = '/admin';
  };

  return (
    <div className="container">
      {showPopup && (
        <Popup
          title="Warning!"
          message={message}
          type="failure"
          onClose={() => setShowPopup(false)}
        />
      )}

      {loading && (
        <>
          <LoadingOverlay />
          <div className="loading-message">Loading, please wait...</div>
        </>
      )}

      <div className="left-section">
        <h1 className="company-name">DataMillr</h1>
      </div>

      <div className="right-section">
        <h1>Welcome Back!</h1>
        <form onSubmit={handleLoginSubmit}>
          <label>User ID</label>
          <input
            type="text"
            value={userIdInput}
            onChange={(e) => setUserIdInput(e.target.value)}
            placeholder="Enter your userId"
          />
          <label>Password</label>
          <input
            type="password"
            value={userPasswordInput}
            onChange={(e) => setUserPasswordInput(e.target.value)}
            placeholder="Enter your password"
          />
          <div className="button-row">
            <button type="submit" disabled={randomNumber === null}>
              Log In
            </button>
            <button
              type="button"
              className="admin-button"
              onClick={handleAdminClick}
            >
              Admin
            </button>
          </div>
        </form>
        {loginMessage && <p className="login-message">{loginMessage}</p>}
      </div>
    </div>
  );
};

export default Timesheet;

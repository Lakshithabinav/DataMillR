import React, { useEffect, useState } from 'react';
import './Userlogin.css';
import axios from 'axios';

const Timesheet = ({ onLoginSuccess, onNewUserDetected }) => {
  const [userIdInput, setUserIdInput] = useState('');
  const [userPasswordInput, setUserPasswordInput] = useState('');
  const [randomNumber, setRandomNumber] = useState(null);
  const [loginMessage, setLoginMessage] = useState('');
  const [userIp, setUserIp] = useState('');
  const [finalCreatedJson, setFinalCreatedJson] = useState(null);

  const convertToAscii = (input) => {
    return input.split('').map((char) => char.charCodeAt(0));
  };

  const processAsciiValues = (asciiArray, randomValue) => {
    return asciiArray
      .map((num, i) => {
        const parsedNum = parseInt(num, 10);
        const rand = parseInt(randomValue, 10);
        return i % 2 === 0 ? parsedNum + rand : parsedNum - rand;
      })
      .join(' ');
  };

  useEffect(() => {
    const cancelTokenSource = axios.CancelToken.source();

    const fetchData = async () => {
      try {
        const ipResponse = await axios.get('https://api.ipify.org?format=json', {
          cancelToken: cancelTokenSource.token,
        });

        if (ipResponse.data?.ip) {
          setUserIp(ipResponse.data.ip);

          const randomNumberResponse = await axios.post(
            'http://localhost:8082/api/auth/init',
            { ip: ipResponse.data.ip }
          );

          if (randomNumberResponse.data?.randomNumber !== undefined) {
            setRandomNumber(randomNumberResponse.data.randomNumber);
          }
        }
      } catch (error) {
        if (!axios.isCancel(error)) {
          console.error('Error fetching data:', error);
        }
      }
    };

    fetchData();

    // 💾 Read created user JSON from sessionStorage and set to state
    const storedJson = sessionStorage.getItem('finalCreatedJson');
    if (storedJson) {
      setFinalCreatedJson(JSON.parse(storedJson));
    }

    return () => {
      cancelTokenSource.cancel();
    };
  }, []);

  const handleLoginSubmit = (event) => {
    event.preventDefault();

    if (!userIdInput || !userPasswordInput || randomNumber === null) {
      alert('Please fill in all fields and ensure the random number is loaded.');
      return;
    }

    // 🌱 Check for new admin-mapped credentials
    const storedNewId = sessionStorage.getItem('newUserId');
    const storedNewPass = sessionStorage.getItem('newUserPassword');

    if (
      storedNewId &&
      storedNewPass &&
      userIdInput === storedNewId &&
      userPasswordInput === storedNewPass
    ) {
      sessionStorage.setItem('userData', JSON.stringify({ userId: storedNewId }));
      sessionStorage.setItem('isNewUser', 'true');
      setLoginMessage('New user login successful! Redirecting to create your credentials...');
      setTimeout(() => {
        if (onNewUserDetected) onNewUserDetected();
      }, 1000);
      return;
    }

    // ✅ Check for user-created credentials from Newuserlogin.js
    const createdUserId = sessionStorage.getItem('createdUserId');
    const createdUserPassword = sessionStorage.getItem('createdUserPassword');

    if (
      createdUserId &&
      createdUserPassword &&
      userIdInput === createdUserId &&
      userPasswordInput === createdUserPassword
    ) {
      sessionStorage.setItem('userData', JSON.stringify({ userId: createdUserId }));

      // Optional cleanup after successful login
      sessionStorage.removeItem('createdUserId');
      sessionStorage.removeItem('createdUserPassword');
      sessionStorage.removeItem('isNewUser');
      sessionStorage.removeItem('finalCreatedJson');

      setLoginMessage('Login successful with your created credentials!');
      setTimeout(() => {
        if (onLoginSuccess) onLoginSuccess();
      }, 1000);
      return;
    }

    // 🔐 Normal backend login
    const userIdAsciiArray = convertToAscii(userIdInput);
    const passwordAsciiArray = convertToAscii(userPasswordInput);
    const encodedUserId = processAsciiValues(userIdAsciiArray, randomNumber);
    const encodedPassword = processAsciiValues(passwordAsciiArray, randomNumber);
    const separator = '124 124 124';
    const finalProcessedResult = `${encodedUserId} ${separator} ${encodedPassword}`;

    axios
      .post('http://localhost:8082/api/auth/login', {
        hashedCredential: finalProcessedResult,
      })
      .then((response) => {
        if (response.data?.success === true) {
          sessionStorage.setItem('userData', JSON.stringify(response.data));
          setLoginMessage('Login successful!');
          if (onLoginSuccess) onLoginSuccess();
        } else {
          setLoginMessage('Invalid username or password. Please try again.');
        }
      })
      .catch((error) => {
        console.error('Error validating credentials:', error);
        setLoginMessage('An error occurred during login.');
      });
  };

  const handleAdminClick = () => {
    window.location.href = '/admin';
  };

  return (
    <div className="container">
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
              {randomNumber === null ? 'Loading...' : 'Log In'}
            </button>
            <button type="button" className="admin-button" onClick={handleAdminClick}>
              Admin
            </button>
          </div>
        </form>
        {loginMessage && <p className="login-message">{loginMessage}</p>}

        {finalCreatedJson && (
          <div
            style={{
              marginTop: '20px',
              backgroundColor: '#e0ffe0',
              border: '1px solid #b2ffb2',
              padding: '10px',
              borderRadius: '8px',
              fontSize: '13px',
            }}
          >
            <strong>✅ New User Details</strong>
            <pre
              style={{
                backgroundColor: '#f4f4f4',
                padding: '10px',
                borderRadius: '5px',
                overflowX: 'auto',
              }}
            >
              {JSON.stringify(finalCreatedJson, null, 2)}
            </pre>
          </div>
        )}
      </div>
    </div>
  );
};

export default Timesheet;

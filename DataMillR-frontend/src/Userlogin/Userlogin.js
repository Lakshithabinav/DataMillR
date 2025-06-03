import React, { useEffect, useState } from 'react';
import './Userlogin.css';
import axios from 'axios';

const Timesheet = ({ onLoginSuccess }) => {
    const [userIdInput, setUserIdInput] = useState('');
    const [userPasswordInput, setUserPasswordInput] = useState('');
    const [randomNumber, setRandomNumber] = useState(null);
    const [loginMessage, setLoginMessage] = useState('');
    const [userIp, setUserIp] = useState('');

    const convertToAscii = (input) => {
        return input.split('').map((char) => char.charCodeAt(0));
    };

    const processAsciiValues = (asciiArray, randomValue) => {
        return asciiArray.map((num, i) => {
            const parsedNum = parseInt(num, 10);
            const rand = parseInt(randomValue, 10);
            return i % 2 === 0 ? parsedNum + rand : parsedNum - rand;
        }).join(' ');
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

                    const randomNumberResponse = await axios.post('http://localhost:8082/api/auth/init', {
                        ip: ipResponse.data.ip,
                    });

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

        // ✅ Check new user credentials from sessionStorage
        const storedId = sessionStorage.getItem('newUserId');
        const storedPass = sessionStorage.getItem('newUserPassword');

        if (userIdInput === storedId && userPasswordInput === storedPass) {
            sessionStorage.setItem('userData', JSON.stringify({ userId: storedId }));
            setLoginMessage('New user login successful!');
            if (onLoginSuccess) onLoginSuccess();
            window.location.href = '/user';
            return;
        }

        // Continue to regular backend login
        const userIdAsciiArray = convertToAscii(userIdInput);
        const passwordAsciiArray = convertToAscii(userPasswordInput);
        const encodedUserId = processAsciiValues(userIdAsciiArray, randomNumber);
        const encodedPassword = processAsciiValues(passwordAsciiArray, randomNumber);
        const separator = '124 124 124';
        const finalProcessedResult = `${encodedUserId} ${separator} ${encodedPassword}`;

        axios.post('http://localhost:8082/api/auth/login', {
            hashedCredential: finalProcessedResult
        })
        .then((response) => {
            console.log(response);
            if (response.data.loginSucess) {
                sessionStorage.setItem('userData', JSON.stringify(response.data));
                setLoginMessage('Login successful!');
                if (onLoginSuccess) onLoginSuccess();
                window.location.href = '/user';
            } else {
                setLoginMessage('Invalid username or password. Please try again.');
            }
        })
        .catch((error) => {
            console.error('Error validating credentials:', error);
            setLoginMessage('An error occurred while validating the credentials. Please try again later.');
        });
    };

    const handleAdminClick = () => {
        window.location.href = '/admin';
    };

    const handleForgotPasswordClick = () => {
        window.location.href = '/newuser';
    };

    return (
        <div className="container">
            <div className="left-section">
                <h1 className="company-name">DataMillr</h1>
            </div>
            <div className="right-section">
                <h1>Welcome Back!</h1>
                <form onSubmit={handleLoginSubmit}>
                    <label htmlFor="userId">User ID</label>
                    <input
                        type="text"
                        id="userId"
                        value={userIdInput}
                        onChange={(e) => setUserIdInput(e.target.value)}
                        placeholder="Enter your userId"
                    />

                    <label htmlFor="userPassword">Password</label>
                    <input
                        type="password"
                        id="userPassword"
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

                    <div className="forgot-password">
                        <button type="button" className="link-button" onClick={handleForgotPasswordClick}>
                            New User
                        </button>
                    </div>
                </form>

                {loginMessage && <p className="login-message">{loginMessage}</p>}
            </div>
        </div>
    );
};

export default Timesheet;

import React, { useEffect, useState } from 'react';
import './userLogin.css';
import axios from 'axios';

const Timesheet = () => {
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
                    } else {
                        console.error('Random number not found in the response');
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
            if (response.data?.success === true) {
                setLoginMessage('Login successful!');
            } else {
                setLoginMessage('Invalid username or password. Please try again.');
            }
        })
        .catch((error) => {
            console.error('Error validating credentials:', error);
            setLoginMessage('An error occurred while validating the credentials. Please try again later.');
        });
    };

    return (
        <div className="container">
            <div className="left-section">
                <h1 className="company-name">RAISON AUTOMATION</h1>
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
                    <button type="submit" disabled={randomNumber === null}>
                        {randomNumber === null ? 'Loading...' : 'Log In'}
                    </button>
                </form>
                {loginMessage && <p className="login-message">{loginMessage}</p>}
            </div>
        </div>
    );
};

export default Timesheet;

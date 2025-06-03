import React, { useState } from 'react';

const NewUserLogin = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();

        // Save the new user credentials in sessionStorage
        sessionStorage.setItem('newUserId', username);
        sessionStorage.setItem('newUserPassword', password);

        alert('Account created! Please log in using your credentials.');
        window.location.href = '/userlogin'; // redirect to login page
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Create Account</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input
                        type="text"
                        id="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <br />
                <div>
                    <label htmlFor="password">Password:</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <br />
                <button type="submit">Submit</button>
            </form>
        </div>
    );
};

export default NewUserLogin;

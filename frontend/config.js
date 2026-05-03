// Set backend URL globally before app loads
window.BACKEND_URL = process.env.REACT_APP_API_URL || (
    window.location.hostname === 'localhost'
        ? 'http://localhost:8080'
        : 'https://custom-cipher-backend.railway.app'
);

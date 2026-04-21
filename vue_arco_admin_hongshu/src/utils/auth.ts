const TOKEN_KEY = 'token';
const ACCESS_TOKEN_KEY = 'accesstoken';
const EXPIRES_IN_KEY = 'Admin-Expires-In';

const isLogin = () => {
    return !!localStorage.getItem(TOKEN_KEY);
};

const getToken = () => {
    return localStorage.getItem(TOKEN_KEY);
};

const setToken = (token: string) => {
    localStorage.setItem(TOKEN_KEY, token);
};

const clearToken = () => {
    localStorage.removeItem(TOKEN_KEY);
};

// AccessToken for backend pass-through (align with legacy project)
const getAccessToken = () => {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
};

const setAccessToken = (token: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
};

const removeAccessToken = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
};

// Expires In helpers (seconds or timestamp as string)
const getExpiresIn = () => {
    return localStorage.getItem(EXPIRES_IN_KEY) || '-1';
};

const setExpiresIn = (value: string) => {
    localStorage.setItem(EXPIRES_IN_KEY, value);
};

const removeExpiresIn = () => {
    localStorage.removeItem(EXPIRES_IN_KEY);
};

export {
    isLogin,
    getToken,
    setToken,
    clearToken,
    getAccessToken,
    setAccessToken,
    removeAccessToken,
    getExpiresIn,
    setExpiresIn,
    removeExpiresIn,
};

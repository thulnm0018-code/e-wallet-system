import axios, { AxiosRequestConfig } from 'axios';

interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  _retry?: boolean;
}

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config as CustomAxiosRequestConfig;

    if (!originalRequest || originalRequest._retry || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    if (originalRequest.url?.includes('/auth/login')) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      await axios.post('http://localhost:8080/api/auth/refresh', {}, { withCredentials: true });
      return api(originalRequest);
    } catch (refreshError) {
      if (!originalRequest.url?.includes('/auth/refresh')) {
        window.location.href = '/login';
      }
      return Promise.reject(refreshError);
    }
  }
);

export default api;
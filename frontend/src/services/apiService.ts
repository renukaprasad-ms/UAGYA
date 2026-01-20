import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { clearAuth } from "./authState";

type ApiErrorPayload = {
  error?: string;
  message?: string;
  success?: boolean;
};

type RetryableRequestConfig = InternalAxiosRequestConfig & { _retry?: boolean };

const apiService = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

const refreshClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

apiService.interceptors.request.use((config) => {
  config.withCredentials = true;
  return config;
});

let refreshPromise: Promise<void> | null = null;

const refreshToken = async () => {
  await refreshClient.post("/api/auth/refresh");
};

apiService.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorPayload>) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "Request failed.";
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !originalRequest.url?.includes("/api/auth/refresh")
    ) {
      originalRequest._retry = true;
      if (!refreshPromise) {
        refreshPromise = refreshToken().finally(() => {
          refreshPromise = null;
        });
      }

      return refreshPromise
        .then(() => apiService(originalRequest))
        .catch(() => {
          clearAuth();
          return Promise.reject(new Error(message));
        });
    }

    return Promise.reject(new Error(message));
  },
);

export default apiService;

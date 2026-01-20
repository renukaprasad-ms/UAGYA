type AuthUser = {
  firstname?: string;
  lastname?: string;
  email?: string;
  role?: string;
  status?: string;
  id?: number;
};

const AUTH_KEY = "uagya.authenticated";
const USER_KEY = "uagya.user";

export const setAuthenticated = (value: boolean) => {
  if (value) {
    sessionStorage.setItem(AUTH_KEY, "true");
  } else {
    sessionStorage.removeItem(AUTH_KEY);
  }
};

export const isAuthenticated = () => sessionStorage.getItem(AUTH_KEY) === "true";

export const setAuthUser = (user: AuthUser | null) => {
  if (!user) {
    sessionStorage.removeItem(USER_KEY);
    return;
  }
  sessionStorage.setItem(USER_KEY, JSON.stringify(user));
};

export const getAuthUser = (): AuthUser | null => {
  const raw = sessionStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
};

export const clearAuth = () => {
  sessionStorage.removeItem(AUTH_KEY);
  sessionStorage.removeItem(USER_KEY);
};

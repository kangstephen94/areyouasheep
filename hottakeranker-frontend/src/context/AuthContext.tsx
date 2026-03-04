import { createContext, useState, useCallback, useEffect, type ReactNode } from 'react';
import { setAccessToken, getAccessToken } from '../services/api';
import * as authService from '../services/authService';
import type { LoginRequest, RegisterRequest } from '../types/api';

export interface AuthUser {
  id: number;
  displayName: string;
}

export interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

function parseJwtPayload(token: string): { sub: string; exp: number } {
  const base64 = token.split('.')[1];
  const json = atob(base64.replace(/-/g, '+').replace(/_/g, '/'));
  return JSON.parse(json);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    // Restore session from stored token on mount
    const token = getAccessToken();
    if (token) {
      try {
        const payload = parseJwtPayload(token);
        if (payload.exp * 1000 > Date.now()) {
          return { id: Number(payload.sub), displayName: sessionStorage.getItem('htr_name') ?? 'User' };
        }
      } catch {
        // Token invalid, clear it
      }
      setAccessToken(null);
    }
    return null;
  });

  // Keep token in sync if user logs out in another tab
  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === 'htr_token' && !e.newValue) {
        setUser(null);
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    const res = await authService.login(data);
    const payload = parseJwtPayload(res.token);
    setAccessToken(res.token);
    const name = data.email.split('@')[0];
    sessionStorage.setItem('htr_name', name);
    setUser({ id: Number(payload.sub), displayName: name });
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    const registerRes = await authService.register(data);
    const loginRes = await authService.login({ email: data.email, password: data.password });
    setAccessToken(loginRes.token);
    sessionStorage.setItem('htr_name', registerRes.displayName);
    setUser({ id: registerRes.id, displayName: registerRes.displayName });
  }, []);

  const logout = useCallback(() => {
    setAccessToken(null);
    sessionStorage.removeItem('htr_name');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

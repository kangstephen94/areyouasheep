import api from './api';
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types/api';

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const res = await api.post<LoginResponse>('/auth/login', data);
  return res.data;
}

export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  const res = await api.post<RegisterResponse>('/auth/register', data);
  return res.data;
}

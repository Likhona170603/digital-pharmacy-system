import api, { clearSession } from './api';

export function normalizeAuth(data = {}) {
  const user = data.user || data;
  return {
    id: user.id,
    role: String(user.role || '').toUpperCase(),
    fullName: user.fullName || user.name || '',
    email: user.email || '',
    phone: user.phone || ''
  };
}

export async function loginRequest(email, password, remember = false) {
  const data = await api.post('/auth/login', { email: email.trim().toLowerCase(), password });
  if (!data?.token || !data?.user) throw new Error('The server did not return a valid login session.');
  clearSession();
  const storage = remember ? localStorage : sessionStorage;
  storage.setItem('pharmaflow_token', data.token);
  storage.setItem('pharmaflow_user', JSON.stringify(normalizeAuth(data)));
  return normalizeAuth(data);
}

export async function registerRequest(payload) {
  return api.post('/auth/register', payload);
}

export function logoutRequest() {
  clearSession();
}

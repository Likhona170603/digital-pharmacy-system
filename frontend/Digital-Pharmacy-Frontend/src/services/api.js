const configuredBase = import.meta.env?.VITE_API_BASE_URL;
const API_BASE_URL = (configuredBase || 'http://localhost:8080/digitalpharmacysystem').replace(/\/+$/, '');

export function getToken() {
  return localStorage.getItem('pharmaflow_token') || sessionStorage.getItem('pharmaflow_token');
}

export function getStoredUser() {
  try {
    const value = localStorage.getItem('pharmaflow_user') || sessionStorage.getItem('pharmaflow_user');
    return value ? JSON.parse(value) : null;
  } catch {
    return null;
  }
}

export function clearSession() {
  ['pharmaflow_token', 'pharmaflow_user'].forEach(key => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
}

async function request(endpoint, options = {}) {
  const token = getToken();
  const headers = { Accept: 'application/json', ...(options.headers || {}) };
  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
  if (options.body !== undefined && !isFormData && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  if (token && !headers.Authorization) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`, {
      ...options,
      headers,
      body: isFormData || typeof options.body === 'string' || options.body === undefined
        ? options.body
        : JSON.stringify(options.body)
    });
  } catch (cause) {
    const error = new Error('Could not connect to PharmaFlow. Check the API URL and confirm the backend is running.');
    error.cause = cause;
    error.status = 0;
    throw error;
  }

  const responseText = await response.text();
  let data = null;
  if (responseText) {
    try { data = JSON.parse(responseText); } catch { data = responseText; }
  }
  if (!response.ok) {
    const error = new Error(typeof data === 'string' ? data : data?.message || data?.error || `Request failed (${response.status}).`);
    error.status = response.status;
    error.data = data;
    if (response.status === 401) window.dispatchEvent(new Event('pharmaflow-session-expired'));
    throw error;
  }
  return data;
}

const api = {
  request,
  get: endpoint => request(endpoint),
  post: (endpoint, body) => request(endpoint, { method: 'POST', body }),
  put: (endpoint, body) => request(endpoint, { method: 'PUT', body }),
  patch: (endpoint, body) => request(endpoint, { method: 'PATCH', body }),
  delete: endpoint => request(endpoint, { method: 'DELETE' }),
  upload: (endpoint, formData) => request(endpoint, { method: 'POST', body: formData }),
  async download(endpoint) {
    const token = getToken();
    let response;
    try {
      response = await fetch(`${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`, {
        headers: { Accept: '*/*', ...(token ? { Authorization: `Bearer ${token}` } : {}) }
      });
    } catch {
      throw new Error('Could not connect to the server to open this file.');
    }
    if (!response.ok) {
      if (response.status === 401) window.dispatchEvent(new Event('pharmaflow-session-expired'));
      const error = new Error(`Could not open the file (HTTP ${response.status}).`);
      error.status = response.status;
      throw error;
    }
    return response.blob();
  }
};
export default api;
export { API_BASE_URL };

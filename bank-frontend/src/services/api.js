const API = 'http://localhost:8085/api';

export async function request(url, options = {}) {
  const token =
    url === '/login'
      ? null
      : localStorage.getItem('amc_token');

  const response = await fetch(API + url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token
        ? { Authorization: `Bearer ${token}` }
        : {}),
      ...(options.headers || {}),
    },
  });

  if (!response.ok) {
    let message = 'Request failed';

    try {
      const data = await response.json();
      message = data.message || message;
    } catch {
      // Keep default message
    }

    if (response.status === 401 && url !== '/login') {
      clearLogin();
      window.location.reload();
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function clearLogin() {
  localStorage.removeItem('amc_token');
  localStorage.removeItem('amc_username');
  localStorage.removeItem('amc_role');
}
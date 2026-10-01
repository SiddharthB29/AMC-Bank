import { useState } from 'react';

import Login from './pages/Login';
import { clearLogin } from './services/api';

import AdminApplication from './pages/admin/AdminApplication';
import CustomerApplication from './pages/customer/CustomerApplication';

const APP_VERSION = 'v5-customer-transactions';

function resetOldSession() {
  if (
    localStorage.getItem('amc_app_version') !== APP_VERSION
  ) {
    localStorage.removeItem('amc_token');
    localStorage.removeItem('amc_username');
    localStorage.removeItem('amc_role');

    localStorage.setItem(
      'amc_app_version',
      APP_VERSION
    );
  }
}

resetOldSession();

export default function App() {
  const [loggedIn, setLoggedIn] = useState(
    Boolean(localStorage.getItem('amc_token'))
  );

  function handleLogin() {
    setLoggedIn(true);
  }

  function handleLogout() {
    clearLogin();
    setLoggedIn(false);
  }

  // Not logged in → Login page
  if (!loggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  const role =
    localStorage.getItem('amc_role') || '';

  const username =
    localStorage.getItem('amc_username') || '';

  // Customer
  if (role === 'ROLE_CUSTOMER') {
    return (
      <CustomerApplication
        username={username}
        role={role}
        onLogout={handleLogout}
      />
    );
  }

  // Admin
  if (role === 'ROLE_ADMIN') {
    return (
      <AdminApplication
        username={username || 'admin'}
        role={role}
        onLogout={handleLogout}
      />
    );
  }

  // Invalid role → logout
  clearLogin();

  return (
    <Login onLogin={handleLogin} />
  );
}
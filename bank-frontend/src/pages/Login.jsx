import { useState } from 'react';
import { request } from '../services/api';

export default function Login({ onLogin }) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function login(e) {
    e.preventDefault();

    setMessage('');
    setLoading(true);

    try {
      const data = await request('/login', {
        method: 'POST',
        body: JSON.stringify({
          username,
          password,
        }),
      });

      // Keep the existing role validation
      if (
        !['ROLE_ADMIN', 'ROLE_CUSTOMER'].includes(data.role)
      ) {
        throw new Error(
          `Unsupported login role: ${data.role}`
        );
      }

      // Save authentication details
      localStorage.setItem('amc_token', data.token);
      localStorage.setItem(
        'amc_username',
        data.username
      );
      localStorage.setItem(
        'amc_role',
        data.role
      );

      // Verify the newly issued token
      const auth = await request('/auth/me');

      if (!auth.authorities?.includes(data.role)) {
        localStorage.removeItem('amc_token');
        localStorage.removeItem('amc_username');
        localStorage.removeItem('amc_role');

        throw new Error(
          'Login role verification failed. Please login again.'
        );
      }

      onLogin();
    } catch (err) {
      setMessage(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-slate-50">

      {/* ========================= */}
      {/* Background Decoration */}
      {/* ========================= */}

      <div className="pointer-events-none absolute inset-0 overflow-hidden">

        <div className="absolute -left-32 -top-32 h-80 w-80 rounded-full bg-blue-100/70 blur-3xl" />

        <div className="absolute -bottom-40 -right-32 h-96 w-96 rounded-full bg-indigo-100/60 blur-3xl" />

        <div className="absolute left-1/2 top-1/3 h-72 w-72 -translate-x-1/2 rounded-full bg-blue-50/80 blur-3xl" />

      </div>

      {/* ========================= */}
      {/* Main */}
      {/* ========================= */}

      <div className="relative z-10 flex min-h-screen items-center justify-center px-5 py-10">

        <div className="w-full max-w-md">

          {/* ========================= */}
          {/* Login Card */}
          {/* ========================= */}

          <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-xl shadow-slate-200/60 sm:p-8">

            {/* Heading */}

            <div className="mb-7">

              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-600">
                Secure Access
              </p>

              <h2 className="mt-2 text-2xl font-bold text-slate-900">
                Welcome back
              </h2>

              <p className="mt-2 text-sm leading-6 text-slate-500">
                Sign in to access your AMC Bank account.
              </p>

            </div>

            {/* ========================= */}
            {/* Login Form */}
            {/* ========================= */}

            <form
              onSubmit={login}
              className="space-y-5"
            >

              {/* Username */}

              <div>
                <label
                  htmlFor="username"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Username / Customer Email
                </label>

                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) =>
                    setUsername(e.target.value)
                  }
                  placeholder="Enter your username"
                  required
                  className="
                    w-full
                    rounded-xl
                    border
                    border-slate-200
                    bg-slate-50
                    px-4
                    py-3
                    text-sm
                    text-slate-900
                    outline-none
                    transition
                    placeholder:text-slate-400
                    focus:border-blue-500
                    focus:bg-white
                    focus:ring-4
                    focus:ring-blue-500/10
                  "
                />
              </div>

              {/* Password */}

              <div>

                <div className="mb-2 flex items-center justify-between">

                  <label
                    htmlFor="password"
                    className="text-sm font-medium text-slate-700"
                  >
                    Password
                  </label>

                  <span className="text-xs font-medium text-slate-400">
                    Secure login
                  </span>

                </div>

                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) =>
                    setPassword(e.target.value)
                  }
                  placeholder="Enter your password"
                  required
                  className="
                    w-full
                    rounded-xl
                    border
                    border-slate-200
                    bg-slate-50
                    px-4
                    py-3
                    text-sm
                    text-slate-900
                    outline-none
                    transition
                    placeholder:text-slate-400
                    focus:border-blue-500
                    focus:bg-white
                    focus:ring-4
                    focus:ring-blue-500/10
                  "
                />

              </div>

              {/* Error Message */}

              {message && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-600">
                  {message}
                </div>
              )}

              {/* Login Button */}

              <button
                type="submit"
                disabled={loading}
                className="
                  w-full
                  rounded-xl
                  bg-gradient-to-r
                  from-blue-600
                  to-indigo-600
                  px-4
                  py-3.5
                  text-sm
                  font-semibold
                  text-white
                  shadow-md
                  shadow-blue-600/20
                  transition-all
                  duration-200
                  hover:-translate-y-0.5
                  hover:shadow-lg
                  hover:shadow-blue-600/25
                  focus:outline-none
                  focus:ring-4
                  focus:ring-blue-500/20
                  disabled:cursor-not-allowed
                  disabled:opacity-60
                  disabled:hover:translate-y-0
                "
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">

                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />

                    Signing in...

                  </span>
                ) : (
                  <span className="flex items-center justify-center gap-2">

                    Sign in

                    <span className="text-lg">
                      →
                    </span>

                  </span>
                )}
              </button>

            </form>

            {/* ========================= */}
            {/* Security Footer */}
            {/* ========================= */}

            <div className="mt-7 flex items-center justify-center gap-2 border-t border-slate-100 pt-5">

              <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-50 text-xs text-emerald-600">
                ✓
              </div>

              <span className="text-xs font-medium text-slate-400">
                Secure access to your banking account
              </span>

            </div>

          </div>

          {/* ========================= */}
          {/* Footer */}
          {/* ========================= */}

          <p className="mt-6 text-center text-xs text-slate-400">
            © 2026 AMC Bank. All rights reserved.
          </p>

        </div>
      </div>
    </div>
  );
}
import { useEffect, useState } from 'react';

import { request } from '../../services/api';

import Header from '../../components/Header';
import Message from '../../components/Message';

import CustomerDashboard from './CustomerDashboard';
import Accounts from './Accounts';
import Loans from './Loans';
import ChangePassword from './ChangePassword';

export default function CustomerApplication({
  username,
  role,
  onLogout,
}) {
  // ==============================
  // View
  // ==============================

  const [view, setView] = useState('dashboard');

  // ==============================
  // Customer Data
  // ==============================

  const [profile, setProfile] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [loans, setLoans] = useState([]);

  // ==============================
  // Message
  // ==============================

  const [message, setMessage] = useState('');

  // ==============================
  // Password
  // ==============================

  const [passwordForm, setPasswordForm] =
    useState({
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    });

  // ==============================
  // Initial Data
  // ==============================

  useEffect(() => {
    loadMyData();
  }, []);

  // ==============================
  // Common Error Handler
  // ==============================

  async function run(action) {
    setMessage('');

    try {
      await action();
    } catch (err) {
      setMessage(err.message);
    }
  }

  // ==============================
  // Load Customer Data
  // ==============================

  async function loadMyData() {
    await run(async () => {
      const [
        profileData,
        accountData,
        loanData,
      ] = await Promise.all([
        request('/customer/me'),
        request('/customer/me/accounts'),
        request('/customer/me/loans'),
      ]);

      setProfile(profileData);
      setAccounts(accountData);
      setLoans(loanData);
    });
  }

  // ==============================
  // Change Password
  // ==============================

  async function changePassword(e) {
    e.preventDefault();

    await run(async () => {

      if (
        passwordForm.newPassword !==
        passwordForm.confirmPassword
      ) {
        throw new Error(
          'New password and confirm password do not match'
        );
      }

      await request(
        '/customer/me/password',
        {
          method: 'POST',

          body: JSON.stringify({
            currentPassword:
              passwordForm.currentPassword,

            newPassword:
              passwordForm.newPassword,
          }),
        }
      );

      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });

      setMessage(
        'Password changed successfully'
      );
    });
  }

  // ==============================
  // Deposit / Withdraw
  // ==============================

  async function customerMoney(
    accountId,
    operation
  ) {
    const account = accounts.find(
      (a) => a.id === accountId
    );

    if (!account) {
      setMessage('Account not found');
      return;
    }

    const balanceText = Number(
      account.balance || 0
    ).toLocaleString();

    const input = window.prompt(
      operation === 'withdraw'
        ? `Available balance: ₹${balanceText}\nEnter amount to withdraw:`
        : `Current balance: ₹${balanceText}\nEnter amount to deposit:`
    );

    if (input === null) {
      return;
    }

    const amount = Number(input);

    if (
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      setMessage(
        'Amount must be greater than zero'
      );

      return;
    }

    // Browser-side validation.
    // Backend validates this again.
    if (
      operation === 'withdraw' &&
      amount > Number(account.balance || 0)
    ) {
      setMessage(
        `Withdrawal denied. Available balance is ₹${balanceText}`
      );

      return;
    }

    await run(async () => {

      await request(
        `/customer/me/accounts/${accountId}/${operation}?amount=${encodeURIComponent(
          input
        )}`,
        {
          method: 'POST',
        }
      );

      const updatedAccounts =
        await request(
          '/customer/me/accounts'
        );

      setAccounts(updatedAccounts);

      setMessage(
        operation === 'deposit'
          ? 'Deposit completed successfully'
          : 'Withdrawal completed successfully'
      );
    });
  }

  // ==============================
  // Calculations
  // ==============================

  const totalBalance = accounts.reduce(
    (sum, account) =>
      sum + Number(account.balance || 0),
    0
  );

  const activeLoans = loans.filter(
    (loan) => loan.status === 'ACTIVE'
  ).length;

  // ==============================
  // Render
  // ==============================

  return (
    <div className="min-h-screen bg-slate-50">

      {/* Header */}
      <Header
        username={username}
        role={role}
        onLogout={onLogout}
      >

        <button
          className="nav-button"
          onClick={() =>
            setView('dashboard')
          }
        >
          My Dashboard
        </button>

        <button
          className="nav-button"
          onClick={() =>
            setView('accounts')
          }
        >
          My Accounts
        </button>

        <button
          className="nav-button"
          onClick={() =>
            setView('loans')
          }
        >
          My Loans
        </button>

        <button
          className="nav-button"
          onClick={() =>
            setView('password')
          }
        >
          Change Password
        </button>

      </Header>

      {/* Main */}
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">

        {/* Messages */}
        <Message text={message} />

        {/* Dashboard */}
        {view === 'dashboard' && (
          <CustomerDashboard
            profile={profile}
            accounts={accounts}
            loans={loans}
            totalBalance={totalBalance}
            activeLoans={activeLoans}
            onAccounts={() =>
              setView('accounts')
            }
            onLoans={() =>
              setView('loans')
            }
          />
        )}

        {/* Accounts */}
        {view === 'accounts' && (
          <Accounts
            accounts={accounts}
            customerMoney={customerMoney}
          />
        )}

        {/* Loans */}
        {view === 'loans' && (
          <Loans
            loans={loans}
          />
        )}

        {/* Password */}
        {view === 'password' && (
          <ChangePassword
            passwordForm={passwordForm}
            setPasswordForm={
              setPasswordForm
            }
            changePassword={
              changePassword
            }
          />
        )}

      </main>
    </div>
  );
}
import { useState } from 'react';

import AccountTable from '../../components/AccountTable';
import TransactionHistory from '../../components/TransactionHistory';

function Accounts({
  accounts,
  customerMoney,
}) {
  const [selectedAccount, setSelectedAccount] = useState(null);

  // Show transaction history for selected account
  if (selectedAccount) {
    return (
      <TransactionHistory
        account={selectedAccount}
        onBack={() => setSelectedAccount(null)}
      />
    );
  }

  return (
    <section className="space-y-6">

      {/* Header */}
      <div>
        <p className="text-sm font-medium text-blue-600">
          Personal Banking
        </p>

        <h2 className="mt-1 text-2xl font-bold text-slate-900">
          My Accounts
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          View your accounts and manage your transactions.
        </p>
      </div>

      {/* Transaction Information */}
      <div className="rounded-2xl border border-blue-100 bg-blue-50 p-5">
        <div className="flex gap-4">

          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-blue-600 shadow-sm">
            ₹
          </div>

          <div>
            <h3 className="text-sm font-semibold text-blue-900">
              Deposit & Withdraw
            </h3>

            <p className="mt-1 text-sm leading-6 text-blue-700">
              Use the buttons beside your account to deposit
              or withdraw money. Withdrawals are allowed only
              when the requested amount is within your available
              balance.
            </p>
          </div>

        </div>
      </div>

      {/* Accounts */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="flex flex-col gap-2 border-b border-slate-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">

          <div>
            <h3 className="text-lg font-semibold text-slate-900">
              Your Accounts
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              {accounts.length} account
              {accounts.length !== 1 ? 's' : ''} linked to your profile.
            </p>
          </div>

          <span className="w-fit rounded-full bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700">
            MY ACCOUNTS
          </span>

        </div>

        <div className="overflow-x-auto p-2">

          <AccountTable
            accounts={accounts}
            showActions
            onMoney={customerMoney}
          />

        </div>

        {/* Transaction History Actions */}
        {accounts.length > 0 && (
          <div className="border-t border-slate-200 px-6 py-4">

            <div className="mb-3">
              <p className="text-sm font-semibold text-slate-900">
                Transaction History
              </p>

              <p className="text-xs text-slate-500">
                View deposits and withdrawals made on each account.
              </p>
            </div>

            <div className="flex flex-wrap gap-3">

              {accounts.map((account) => (
                <button
                  key={account.id}
                  type="button"
                  onClick={() => setSelectedAccount(account)}
                  className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
                >
                  <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
                    ₹
                  </span>

                  <span>
                    {account.accountType || 'Account'} #{account.id}
                  </span>

                  <span className="text-blue-600">
                    →
                  </span>
                </button>
              ))}

            </div>

          </div>
        )}

      </div>

      {/* Security Information */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

        <div className="flex gap-4">

          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
            ✓
          </div>

          <div>
            <h3 className="text-sm font-semibold text-slate-900">
              Secure Transactions
            </h3>

            <p className="mt-1 text-sm leading-6 text-slate-500">
              You can transact only on accounts linked to your
              own customer login. The bank backend validates
              account ownership and available balance.
            </p>
          </div>

        </div>

      </div>

    </section>
  );
}

export default Accounts;
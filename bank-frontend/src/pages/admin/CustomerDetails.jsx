import React from 'react';
import AccountTable from '../../components/AccountTable';
import LoanFields from '../../components/LoanFields';
import LoanTable from '../../components/LoanTable';

function CustomerDetails({
  selectedCustomer,
  accounts,
  customerLoans,
  accountForm,
  setAccountForm,
  openAccount,
  loanForm,
  setLoanForm,
  sanctionLoanFromCustomer,
  money,
  onBack,
}) {
  return (
    <section className="space-y-6">

      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div>
          <p className="text-sm font-medium text-blue-600">
            Customer Management
          </p>

          <h2 className="mt-1 text-2xl font-bold text-slate-900">
            {selectedCustomer.name}
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Customer login: {selectedCustomer.email}
          </p>
        </div>

        <button
          onClick={onBack}
          className="w-fit rounded-xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          ← Back to Customers
        </button>

      </div>

      {/* Customer Summary */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            Customer ID
          </p>

          <p className="mt-2 text-xl font-bold text-slate-900">
            #{selectedCustomer.id}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            Email
          </p>

          <p className="mt-2 truncate text-sm font-semibold text-slate-900">
            {selectedCustomer.email}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            PAN Number
          </p>

          <p className="mt-2 font-mono text-sm font-semibold text-slate-900">
            {selectedCustomer.panNumber}
          </p>
        </div>

      </div>

      {/* Account + Loan Actions */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">

        {/* Open Account */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <h3 className="text-lg font-semibold text-slate-900">
              Open Account
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Create a new bank account for this customer.
            </p>
          </div>

          <form
            onSubmit={openAccount}
            className="space-y-5"
          >

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                Account Type
              </label>

              <select
                value={accountForm.type}
                onChange={(e) =>
                  setAccountForm({
                    ...accountForm,
                    type: e.target.value,
                  })
                }
                className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
              >
                <option value="SAVINGS">
                  Savings Account
                </option>

                <option value="CURRENT">
                  Current Account
                </option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                Opening Balance
              </label>

              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-400">
                  ₹
                </span>

                <input
                  type="number"
                  min="0"
                  value={accountForm.balance}
                  onChange={(e) =>
                    setAccountForm({
                      ...accountForm,
                      balance: e.target.value,
                    })
                  }
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 pl-9 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-blue-700"
            >
              Open Account
            </button>

          </form>
        </div>

        {/* Sanction Loan */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">
            <h3 className="text-lg font-semibold text-slate-900">
              Sanction Loan
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Create a new loan for this customer.
            </p>
          </div>

          <form
            onSubmit={sanctionLoanFromCustomer}
            className="space-y-5"
          >

            <LoanFields
              loanForm={loanForm}
              setLoanForm={setLoanForm}
            />

            <button
              type="submit"
              className="w-full rounded-xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
            >
              Sanction Loan
            </button>

          </form>
        </div>

      </div>

      {/* Accounts */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="border-b border-slate-200 px-6 py-5">
          <h3 className="text-lg font-semibold text-slate-900">
            Customer Accounts
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            Manage accounts and perform transactions.
          </p>
        </div>

        <div className="overflow-x-auto p-2">
          <AccountTable
            accounts={accounts}
            showActions
            onMoney={money}
          />
        </div>

      </div>

      {/* Loans */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="border-b border-slate-200 px-6 py-5">
          <h3 className="text-lg font-semibold text-slate-900">
            Customer Loans
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            View all loans sanctioned for this customer.
          </p>
        </div>

        <div className="overflow-x-auto p-2">
          <LoanTable loans={customerLoans} />
        </div>

      </div>

    </section>
  );
}

export default CustomerDetails;
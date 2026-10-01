import React from 'react';

import LoanFields from '../../components/LoanFields';
import LoanRules from '../../components/LoanRules';
import LoanTable from '../../components/LoanTable';

function Loans({
  customers,
  allLoans,
  loanCustomerId,
  setLoanCustomerId,
  loanForm,
  setLoanForm,
  sanctionLoanFromModule,
  openLoansModule,
}) {
  return (
    <section className="space-y-6">

      {/* ==============================
          Page Header
      ============================== */}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">

        <div>
          <p className="text-sm font-medium text-blue-600">
            Banking Operations
          </p>

          <h2 className="mt-1 text-2xl font-bold text-slate-900">
            Loan Module
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Sanction loans and manage all sanctioned loans.
          </p>
        </div>

        <div className="flex items-center gap-3">

          <div className="rounded-xl bg-blue-50 px-4 py-2">
            <span className="text-xs font-semibold text-blue-700">
              {allLoans.length} LOAN
              {allLoans.length !== 1 ? 'S' : ''}
            </span>
          </div>

          <button
            onClick={openLoansModule}
            className="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            ↻ Refresh
          </button>

        </div>

      </div>

      {/* ==============================
          Loan Creation + Rules
      ============================== */}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">

        {/* Sanction Loan */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">

            <div className="flex items-center gap-3">

              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-700">
                ₹
              </div>

              <div>
                <h3 className="text-lg font-semibold text-slate-900">
                  Sanction New Loan
                </h3>

                <p className="text-sm text-slate-500">
                  Select a customer and configure the loan.
                </p>
              </div>

            </div>

          </div>

          {customers.length === 0 ? (

            <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-8 text-center">

              <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-200 text-lg">
                👤
              </div>

              <h4 className="mt-3 text-sm font-semibold text-slate-800">
                No customers available
              </h4>

              <p className="mt-1 text-xs text-slate-500">
                Add a customer before sanctioning a loan.
              </p>

            </div>

          ) : (

            <form
              onSubmit={sanctionLoanFromModule}
              className="space-y-5"
            >

              {/* Customer */}
              <div>

                <label className="mb-2 block text-sm font-medium text-slate-700">
                  Customer
                </label>

                <select
                  value={loanCustomerId}
                  onChange={(e) =>
                    setLoanCustomerId(e.target.value)
                  }
                  required
                  className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                >

                  <option value="">
                    Select Customer
                  </option>

                  {customers.map((customer) => (
                    <option
                      key={customer.id}
                      value={customer.id}
                    >
                      {customer.name} - {customer.email}
                    </option>
                  ))}

                </select>

              </div>

              {/* Loan Fields */}
              <LoanFields
                loanForm={loanForm}
                setLoanForm={setLoanForm}
              />

              {/* Submit */}
              <button
                type="submit"
                className="w-full rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 hover:shadow-md"
              >
                Sanction Loan
              </button>

            </form>

          )}

        </div>

        {/* Loan Rules */}
        <LoanRules />

      </div>

      {/* ==============================
          All Loans
      ============================== */}

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="flex flex-col gap-3 border-b border-slate-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">

          <div>
            <h3 className="text-lg font-semibold text-slate-900">
              All Loans
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Complete list of loans sanctioned through the bank.
            </p>
          </div>

          <div className="rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-600">
            {allLoans.length} RECORD
            {allLoans.length !== 1 ? 'S' : ''}
          </div>

        </div>

        <div className="overflow-x-auto p-2">

          <LoanTable
            loans={allLoans}
            showCustomer
          />

        </div>

      </div>

    </section>
  );
}

export default Loans;
import React from 'react';

function LoanFields({
  loanForm,
  setLoanForm,
}) {
  return (
    <div className="space-y-5">

      {/* Loan Type */}
      <div>
        <label className="mb-2 block text-sm font-medium text-slate-700">
          Loan Type
        </label>

        <select
          value={loanForm.type}
          onChange={(e) =>
            setLoanForm({
              ...loanForm,
              type: e.target.value,
            })
          }
          className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
        >
          <option value="HOME">
            Home Loan
          </option>

          <option value="CAR">
            Car Loan
          </option>

          <option value="PERSONAL">
            Personal Loan
          </option>
        </select>
      </div>

      {/* Principal */}
      <div>
        <label className="mb-2 block text-sm font-medium text-slate-700">
          Principal Amount
        </label>

        <div className="relative">

          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-400">
            ₹
          </span>

          <input
            type="number"
            min="10000"
            value={loanForm.principal}
            onChange={(e) =>
              setLoanForm({
                ...loanForm,
                principal: e.target.value,
              })
            }
            required
            className="w-full rounded-xl border border-slate-300 px-4 py-3 pl-9 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
          />

        </div>
      </div>

      {/* Interest + Tenure */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">

        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Interest Rate (%)
          </label>

          <div className="relative">

            <input
              type="number"
              step="0.01"
              min="0"
              value={loanForm.interestRate}
              onChange={(e) =>
                setLoanForm({
                  ...loanForm,
                  interestRate: e.target.value,
                })
              }
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3 pr-10 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm text-slate-400">
              %
            </span>

          </div>
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Tenure
          </label>

          <div className="relative">

            <input
              type="number"
              min="1"
              value={loanForm.tenureMonths}
              onChange={(e) =>
                setLoanForm({
                  ...loanForm,
                  tenureMonths: e.target.value,
                })
              }
              required
              className="w-full rounded-xl border border-slate-300 px-4 py-3 pr-16 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-slate-400">
              months
            </span>

          </div>
        </div>

      </div>

    </div>
  );
}

export default LoanFields;
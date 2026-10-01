import React from 'react';

function LoanRules() {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

      <div className="mb-6">
        <p className="text-sm font-medium text-blue-600">
          Eligibility
        </p>

        <h3 className="mt-1 text-lg font-semibold text-slate-900">
          Loan Rules
        </h3>

        <p className="mt-1 text-sm text-slate-500">
          Keep these requirements in mind while sanctioning a loan.
        </p>
      </div>

      <div className="space-y-4">

        {/* Rule 1 */}
        <div className="flex gap-4 rounded-xl bg-slate-50 p-4">

          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-sm font-bold text-blue-700">
            ₹
          </div>

          <div>
            <p className="text-sm font-semibold text-slate-800">
              Minimum Principal
            </p>

            <p className="mt-1 text-xs leading-5 text-slate-500">
              The requested loan principal must be at least ₹10,000.
            </p>
          </div>

        </div>

        {/* Rule 2 */}
        <div className="flex gap-4 rounded-xl bg-slate-50 p-4">

          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-sm font-bold text-blue-700">
            3
          </div>

          <div>
            <p className="text-sm font-semibold text-slate-800">
              Active Loan Limit
            </p>

            <p className="mt-1 text-xs leading-5 text-slate-500">
              A customer can have a maximum of 3 active loans.
            </p>
          </div>

        </div>

        {/* Rule 3 */}
        <div className="flex gap-4 rounded-xl bg-slate-50 p-4">

          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-sm font-bold text-blue-700">
            10%
          </div>

          <div>
            <p className="text-sm font-semibold text-slate-800">
              Account Balance Requirement
            </p>

            <p className="mt-1 text-xs leading-5 text-slate-500">
              The customer's active account balance must be at least 10% of the requested principal.
            </p>
          </div>

        </div>

      </div>

      <div className="mt-6 rounded-xl border border-blue-100 bg-blue-50 p-4">
        <p className="text-xs leading-5 text-blue-700">
          Loan approval is subject to the bank's configured eligibility rules.
        </p>
      </div>

    </div>
  );
}

export default LoanRules;
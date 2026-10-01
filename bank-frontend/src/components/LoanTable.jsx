import React from 'react';

function LoanTable({
  loans,
  showCustomer = false,
}) {
  return (
    <div className="overflow-x-auto">

      <table className="min-w-full text-left">

        <thead className="bg-slate-50">
          <tr>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Loan ID
            </th>

            {showCustomer && (
              <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                Customer
              </th>
            )}

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Type
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Principal
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Interest
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Tenure
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              EMI
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Status
            </th>

          </tr>
        </thead>

        <tbody className="divide-y divide-slate-100">

          {loans.map((loan) => (
            <tr
              key={loan.id}
              className="transition hover:bg-slate-50"
            >

              {/* Loan ID */}
              <td className="px-5 py-4">

                <span className="font-mono text-sm font-semibold text-slate-700">
                  #{loan.id}
                </span>

              </td>

              {/* Customer */}
              {showCustomer && (
                <td className="px-5 py-4">

                  <div>
                    <p className="text-sm font-semibold text-slate-900">
                      {loan.customer?.name ||
                        loan.customerName ||
                        `Customer #${loan.customerId || '—'}`}
                    </p>

                    {(loan.customer?.email ||
                      loan.customerEmail) && (
                      <p className="mt-1 text-xs text-slate-400">
                        {loan.customer?.email ||
                          loan.customerEmail}
                      </p>
                    )}

                  </div>

                </td>
              )}

              {/* Type */}
              <td className="px-5 py-4">

                <span className="rounded-lg bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700">
                  {loan.type}
                </span>

              </td>

              {/* Principal */}
              <td className="px-5 py-4">

                <span className="text-sm font-semibold text-slate-900">
                  ₹
                  {Number(
                    loan.principal || 0
                  ).toLocaleString('en-IN', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}
                </span>

              </td>

              {/* Interest */}
              <td className="px-5 py-4 text-sm text-slate-600">
                {loan.interestRate}%
              </td>

              {/* Tenure */}
              <td className="px-5 py-4 text-sm text-slate-600">
                {loan.tenureMonths} months
              </td>

              {/* EMI */}
              <td className="px-5 py-4">

                <span className="text-sm font-semibold text-slate-900">
                  ₹
                  {Number(
                    loan.emi || 0
                  ).toLocaleString('en-IN', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })}
                </span>

              </td>

              {/* Status */}
              <td className="px-5 py-4">

                <span
                  className={`rounded-full px-3 py-1.5 text-xs font-semibold ${
                    loan.status === 'ACTIVE'
                      ? 'bg-emerald-50 text-emerald-700'
                      : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {loan.status || 'ACTIVE'}
                </span>

              </td>

            </tr>
          ))}

          {loans.length === 0 && (
            <tr>

              <td
                colSpan={showCustomer ? 8 : 7}
                className="px-5 py-12 text-center"
              >

                <div className="mx-auto max-w-sm">

                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100">
                    ₹
                  </div>

                  <p className="mt-3 text-sm font-semibold text-slate-800">
                    No loans found
                  </p>

                  <p className="mt-1 text-xs text-slate-500">
                    No loans have been sanctioned yet.
                  </p>

                </div>

              </td>

            </tr>
          )}

        </tbody>

      </table>

    </div>
  );
}

export default LoanTable;
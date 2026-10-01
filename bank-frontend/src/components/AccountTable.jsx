import React from 'react';

function AccountTable({
  accounts,
  showActions = false,
  onMoney,
}) {
  return (
    <div className="overflow-x-auto">

      <table className="min-w-full text-left">

        <thead className="bg-slate-50">
          <tr>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Account ID
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Type
            </th>

            <th className="px-5 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
              Balance
            </th>

            {showActions && (
              <th className="px-5 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">
                Actions
              </th>
            )}

          </tr>
        </thead>

        <tbody className="divide-y divide-slate-100">

          {accounts.map((account) => (
            <tr
              key={account.id}
              className="transition hover:bg-slate-50"
            >

              {/* Account ID */}
              <td className="px-5 py-4">

                <span className="font-mono text-sm font-semibold text-slate-700">
                  #{account.id}
                </span>

              </td>

              {/* Account Type */}
              <td className="px-5 py-4">

                <span
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold ${
                    account.type === 'SAVINGS'
                      ? 'bg-blue-50 text-blue-700'
                      : 'bg-purple-50 text-purple-700'
                  }`}
                >
                  {account.type}
                </span>

              </td>

              {/* Balance */}
              <td className="px-5 py-4">

                <span className="text-sm font-bold text-slate-900">
                  ₹
                  {Number(account.balance || 0).toLocaleString(
                    'en-IN',
                    {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    }
                  )}
                </span>

              </td>

              {/* Actions */}
              {showActions && (
                <td className="px-5 py-4">

                  <div className="flex justify-end gap-2">

                    <button
                      onClick={() =>
                        onMoney(account.id, 'deposit')
                      }
                      className="rounded-lg bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                    >
                      Deposit
                    </button>

                    <button
                      onClick={() =>
                        onMoney(account.id, 'withdraw')
                      }
                      className="rounded-lg bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-700 transition hover:bg-amber-100"
                    >
                      Withdraw
                    </button>

                  </div>

                </td>
              )}

            </tr>
          ))}

          {accounts.length === 0 && (
            <tr>
              <td
                colSpan={showActions ? 4 : 3}
                className="px-5 py-12 text-center"
              >

                <div className="mx-auto max-w-sm">

                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100">
                    🏦
                  </div>

                  <p className="mt-3 text-sm font-semibold text-slate-800">
                    No accounts found
                  </p>

                  <p className="mt-1 text-xs text-slate-500">
                    This customer does not have any accounts yet.
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

export default AccountTable;
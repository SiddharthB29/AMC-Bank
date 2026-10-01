import { useEffect, useState } from 'react';
import { request } from '../services/api';

export default function TransactionHistory({
  account,
  onBack,
}) {
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const PAGE_SIZE = 10;

  useEffect(() => {
    loadTransactions();
  }, [account.id, page]);

  async function loadTransactions() {
    setLoading(true);
    setError('');

    try {
      const data = await request(
        `/customer/accounts/${account.id}/transactions?page=${page}&size=${PAGE_SIZE}`
      );

      setTransactions(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(
        err.message || 'Failed to load transaction history'
      );
    } finally {
      setLoading(false);
    }
  }

  function formatAmount(amount) {
    return Number(amount).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    });
  }

  function formatDate(timestamp) {
    if (!timestamp) return '-';

    return new Date(timestamp).toLocaleString('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }

  return (
    <div className="space-y-6">

      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

        <div>
          <p className="text-sm font-medium text-blue-600">
            Account #{account.id}
          </p>

          <h2 className="mt-1 text-2xl font-bold text-slate-900">
            Transaction History
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            View deposits and withdrawals for this account.
          </p>
        </div>

        <button
          type="button"
          onClick={onBack}
          className="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          ← Back to Accounts
        </button>

      </div>

      {/* Account Summary */}
      <div className="rounded-2xl bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white shadow-lg">

        <p className="text-sm text-blue-100">
          {account.type} Account
        </p>

        <p className="mt-2 text-3xl font-bold">
          {formatAmount(account.balance)}
        </p>

        <p className="mt-1 text-sm text-blue-100">
          Current balance
        </p>

      </div>

      {/* Loading */}
      {loading && (
        <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center shadow-sm">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600" />

          <p className="mt-4 text-sm text-slate-500">
            Loading transaction history...
          </p>
        </div>
      )}

      {/* Error */}
      {!loading && error && (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-5 text-red-700">
          <p className="font-semibold">
            Unable to load transactions
          </p>

          <p className="mt-1 text-sm">
            {error}
          </p>

          <button
            type="button"
            onClick={loadTransactions}
            className="mt-4 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700"
          >
            Try Again
          </button>
        </div>
      )}

      {/* Transactions */}
      {!loading && !error && (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

          <div className="border-b border-slate-200 px-6 py-4">
            <h3 className="font-semibold text-slate-900">
              Recent Transactions
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Latest transactions are shown first.
            </p>
          </div>

          {transactions.length === 0 ? (
            <div className="p-10 text-center">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-2xl">
                ₹
              </div>

              <h3 className="mt-4 font-semibold text-slate-900">
                No transactions yet
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                Deposits and withdrawals will appear here.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">

              <table className="min-w-full">

                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Type
                    </th>

                    <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Amount
                    </th>

                    <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Balance After
                    </th>

                    <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Date & Time
                    </th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-slate-100">

                  {transactions.map((transaction) => {
                    const isDeposit =
                      transaction.type === 'DEPOSIT';

                    return (
                      <tr
                        key={transaction.transactionId}
                        className="transition hover:bg-slate-50"
                      >

                        <td className="px-6 py-4">
                          <span
                            className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
                              isDeposit
                                ? 'bg-emerald-100 text-emerald-700'
                                : 'bg-red-100 text-red-700'
                            }`}
                          >
                            {isDeposit
                              ? '↓ Deposit'
                              : '↑ Withdrawal'}
                          </span>
                        </td>

                        <td
                          className={`px-6 py-4 text-right font-semibold ${
                            isDeposit
                              ? 'text-emerald-600'
                              : 'text-red-600'
                          }`}
                        >
                          {isDeposit ? '+' : '-'}
                          {formatAmount(transaction.amount)}
                        </td>

                        <td className="px-6 py-4 text-right font-medium text-slate-900">
                          {formatAmount(
                            transaction.balanceAfterTransaction
                          )}
                        </td>

                        <td className="px-6 py-4 text-right text-sm text-slate-500">
                          {formatDate(transaction.timestamp)}
                        </td>

                      </tr>
                    );
                  })}

                </tbody>

              </table>

            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-slate-200 px-6 py-4">

              <button
                type="button"
                disabled={page === 0}
                onClick={() =>
                  setPage((current) => current - 1)
                }
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
              >
                ← Previous
              </button>

              <span className="text-sm text-slate-500">
                Page {page + 1} of {totalPages}
              </span>

              <button
                type="button"
                disabled={page >= totalPages - 1}
                onClick={() =>
                  setPage((current) => current + 1)
                }
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
              >
                Next →
              </button>

            </div>
          )}

        </div>
      )}

    </div>
  );
}
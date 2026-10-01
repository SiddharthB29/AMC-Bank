function CustomerDashboard({
  profile,
  accounts,
  loans,
  totalBalance,
  activeLoans,
  onAccounts,
  onLoans,
}) {
  return (
    <section className="space-y-6">

      {/* Header */}
      <div>
        <p className="text-sm font-medium text-blue-600">
          AMC Bank
        </p>

        <h2 className="mt-1 text-2xl font-bold text-slate-900">
          My Dashboard
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          View your accounts, balance and loan information.
        </p>
      </div>

      {/* Welcome */}
      <div className="overflow-hidden rounded-2xl bg-slate-900 p-6 text-white shadow-sm">

        <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">

          <div>

            <p className="text-sm text-slate-300">
              Welcome back
            </p>

            <h3 className="mt-1 text-2xl font-bold">
              {profile
                ? profile.name
                : 'Customer'}
            </h3>

            <p className="mt-2 max-w-xl text-sm leading-6 text-slate-300">
              This dashboard shows only your own AMC Bank
              information.
            </p>

          </div>

          <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-white/10 text-2xl font-bold">
            {profile?.name
              ?.charAt(0)
              ?.toUpperCase() || 'C'}
          </div>

        </div>

      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">

        {/* Accounts */}
        <button
          onClick={onAccounts}
          className="group rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-0.5 hover:border-blue-200 hover:shadow-md"
        >

          <div className="flex items-center justify-between">

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
              🏦
            </div>

            <span className="text-xs font-semibold text-blue-600 opacity-0 transition group-hover:opacity-100">
              View →
            </span>

          </div>

          <p className="mt-5 text-sm text-slate-500">
            My Accounts
          </p>

          <p className="mt-1 text-2xl font-bold text-slate-900">
            {accounts.length}
          </p>

        </button>

        {/* Balance */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
            ₹
          </div>

          <p className="mt-5 text-sm text-slate-500">
            Total Balance
          </p>

          <p className="mt-1 text-2xl font-bold text-slate-900">
            ₹
            {totalBalance.toLocaleString(
              'en-IN'
            )}
          </p>

        </div>

        {/* Loans */}
        <button
          onClick={onLoans}
          className="group rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-sm transition hover:-translate-y-0.5 hover:border-blue-200 hover:shadow-md"
        >

          <div className="flex items-center justify-between">

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-purple-50 text-purple-600">
              ₹
            </div>

            <span className="text-xs font-semibold text-purple-600 opacity-0 transition group-hover:opacity-100">
              View →
            </span>

          </div>

          <p className="mt-5 text-sm text-slate-500">
            Active Loans
          </p>

          <p className="mt-1 text-2xl font-bold text-slate-900">
            {activeLoans}
          </p>

        </button>

      </div>

      {/* Profile */}
      {profile && (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-6">

            <h3 className="text-lg font-semibold text-slate-900">
              My Profile
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Your registered customer information.
            </p>

          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Name
              </p>

              <p className="mt-2 text-sm font-semibold text-slate-900">
                {profile.name}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Email / Login ID
              </p>

              <p className="mt-2 truncate text-sm font-semibold text-slate-900">
                {profile.email}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                City
              </p>

              <p className="mt-2 text-sm font-semibold text-slate-900">
                {profile.city || '-'}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                PAN
              </p>

              <p className="mt-2 font-mono text-sm font-semibold text-slate-900">
                {profile.panNumber}
              </p>
            </div>

          </div>

        </div>
      )}

    </section>
  );
}

export default CustomerDashboard;
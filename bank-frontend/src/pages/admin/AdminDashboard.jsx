export default function AdminDashboard({
  stats,
  onCustomers,
  onLoans,
}) {
  return (
    <section className="space-y-8">

      {/* Page heading */}
      <div>
        <p className="text-sm font-semibold uppercase tracking-wider text-blue-600">
          Overview
        </p>

        <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">
          Admin Dashboard
        </h1>

        <p className="mt-2 text-sm text-slate-500">
          Manage customers, accounts and loans from one place.
        </p>
      </div>

      {/* Statistics */}
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">
                Total Customers
              </p>

              <p className="mt-3 text-3xl font-bold text-slate-900">
                {stats.customers}
              </p>

              <p className="mt-2 text-xs text-slate-400">
                Registered customers
              </p>
            </div>

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
              👥
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">
                Active Accounts
              </p>

              <p className="mt-3 text-3xl font-bold text-slate-900">
                {stats.activeAccounts}
              </p>

              <p className="mt-2 text-xs text-slate-400">
                Currently active
              </p>
            </div>

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
              💳
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md sm:col-span-2 lg:col-span-1">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-slate-500">
                Active Loans
              </p>

              <p className="mt-3 text-3xl font-bold text-slate-900">
                {stats.activeLoans}
              </p>

              <p className="mt-2 text-xs text-slate-400">
                Currently sanctioned
              </p>
            </div>

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-violet-50 text-violet-600">
              📋
            </div>
          </div>
        </div>

      </div>

      {/* Quick actions */}
      <div>
        <div className="mb-4">
          <h2 className="text-lg font-bold text-slate-900">
            Quick Actions
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Access frequently used banking operations.
          </p>
        </div>

        <div className="grid gap-5 lg:grid-cols-2">

          {/* Customer */}
          <div className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-700 p-7 text-white shadow-lg shadow-blue-900/10">
            <div className="relative z-10">

              <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-white/15 text-xl backdrop-blur">
                👤
              </div>

              <h3 className="text-xl font-bold">
                Customer Management
              </h3>

              <p className="mt-2 max-w-md text-sm leading-6 text-blue-100">
                Create customer profiles, manage login credentials,
                view accounts and update customer information.
              </p>

              <button
                onClick={onCustomers}
                className="mt-6 inline-flex items-center gap-2 rounded-xl bg-white px-5 py-2.5 text-sm font-semibold text-blue-700 transition hover:bg-blue-50"
              >
                Manage Customers
                <span>→</span>
              </button>

            </div>

            <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-white/10" />
            <div className="absolute -bottom-20 -right-5 h-52 w-52 rounded-full bg-white/5" />
          </div>

          {/* Loans */}
          <div className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-800 to-slate-950 p-7 text-white shadow-lg shadow-slate-900/10">
            <div className="relative z-10">

              <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-white/10 text-xl">
                🏦
              </div>

              <h3 className="text-xl font-bold">
                Loan Management
              </h3>

              <p className="mt-2 max-w-md text-sm leading-6 text-slate-300">
                Sanction HOME, CAR and PERSONAL loans,
                calculate EMI and monitor sanctioned loans.
              </p>

              <button
                onClick={onLoans}
                className="mt-6 inline-flex items-center gap-2 rounded-xl border border-white/20 bg-white/10 px-5 py-2.5 text-sm font-semibold text-white backdrop-blur transition hover:bg-white/20"
              >
                Open Loan Module
                <span>→</span>
              </button>

            </div>

            <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-blue-500/10" />
            <div className="absolute -bottom-20 -right-5 h-52 w-52 rounded-full bg-indigo-500/10" />
          </div>

        </div>
      </div>

      {/* System status */}
      <div className="flex flex-col gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <div className="h-2.5 w-2.5 rounded-full bg-emerald-500 shadow-sm shadow-emerald-500/50" />

          <div>
            <p className="text-sm font-semibold text-emerald-800">
              Banking system operational
            </p>

            <p className="text-xs text-emerald-700/70">
              All core banking services are available.
            </p>
          </div>
        </div>

        <span className="text-xs font-medium text-emerald-700">
          SYSTEM ONLINE
        </span>
      </div>

    </section>
  );
}
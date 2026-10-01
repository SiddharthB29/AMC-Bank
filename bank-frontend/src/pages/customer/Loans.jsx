import LoanTable from '../../components/LoanTable';

function Loans({
  loans,
}) {
  const activeLoans = loans.filter(
    (loan) => loan.status === 'ACTIVE'
  );

  return (
    <section className="space-y-6">

      {/* Header */}
      <div>
        <p className="text-sm font-medium text-blue-600">
          Personal Banking
        </p>

        <h2 className="mt-1 text-2xl font-bold text-slate-900">
          My Loans
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          View your sanctioned loans, interest rates and EMI details.
        </p>
      </div>

      {/* Loan Summary */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            Total Loans
          </p>

          <p className="mt-2 text-2xl font-bold text-slate-900">
            {loans.length}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            Active Loans
          </p>

          <p className="mt-2 text-2xl font-bold text-emerald-600">
            {activeLoans.length}
          </p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
            Loan Status
          </p>

          <p className="mt-2 text-sm font-semibold text-slate-900">
            {activeLoans.length > 0
              ? 'Active'
              : 'No Active Loans'}
          </p>
        </div>

      </div>

      {/* Loans Table */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <div className="border-b border-slate-200 px-6 py-5">

          <h3 className="text-lg font-semibold text-slate-900">
            Loan Details
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            Your loan information and EMI details.
          </p>

        </div>

        <div className="overflow-x-auto p-2">

          <LoanTable
            loans={loans}
          />

        </div>

      </div>

      {/* Information */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">

        <div className="flex gap-4">

          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-slate-100 text-slate-600">
            i
          </div>

          <div>

            <h3 className="text-sm font-semibold text-slate-900">
              Loan Information
            </h3>

            <p className="mt-1 text-sm leading-6 text-slate-500">
              Customers can view their own loans and EMI.
              Loan sanctioning is handled by the bank administrator.
            </p>

          </div>

        </div>

      </div>

    </section>
  );
}

export default Loans;
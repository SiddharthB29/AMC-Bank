function ChangePassword({
  passwordForm,
  setPasswordForm,
  changePassword,
}) {
  return (
    <section className="mx-auto max-w-2xl">

      {/* Header */}
      <div className="mb-6">

        <p className="text-sm font-medium text-blue-600">
          Account Security
        </p>

        <h2 className="mt-1 text-2xl font-bold text-slate-900">
          Change Password
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Update your AMC Bank login password.
        </p>

      </div>

      {/* Form Card */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">

        <div className="mb-7 rounded-xl bg-slate-50 p-4">

          <p className="text-sm font-medium text-slate-800">
            Keep your account secure
          </p>

          <p className="mt-1 text-xs leading-5 text-slate-500">
            Enter your current password and choose a new
            password with at least 6 characters.
          </p>

        </div>

        <form
          onSubmit={changePassword}
          className="space-y-5"
        >

          {/* Current Password */}
          <div>

            <label className="mb-2 block text-sm font-medium text-slate-700">
              Current Password
            </label>

            <input
              type="password"
              value={passwordForm.currentPassword}
              onChange={(e) =>
                setPasswordForm({
                  ...passwordForm,
                  currentPassword: e.target.value,
                })
              }
              required
              placeholder="Enter current password"
              className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

          </div>

          {/* New Password */}
          <div>

            <label className="mb-2 block text-sm font-medium text-slate-700">
              New Password
            </label>

            <input
              type="password"
              minLength="6"
              value={passwordForm.newPassword}
              onChange={(e) =>
                setPasswordForm({
                  ...passwordForm,
                  newPassword: e.target.value,
                })
              }
              required
              placeholder="Enter new password"
              className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

            <p className="mt-2 text-xs text-slate-400">
              Minimum 6 characters.
            </p>

          </div>

          {/* Confirm Password */}
          <div>

            <label className="mb-2 block text-sm font-medium text-slate-700">
              Confirm New Password
            </label>

            <input
              type="password"
              minLength="6"
              value={passwordForm.confirmPassword}
              onChange={(e) =>
                setPasswordForm({
                  ...passwordForm,
                  confirmPassword: e.target.value,
                })
              }
              required
              placeholder="Confirm new password"
              className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

          </div>

          {/* Submit */}
          <button
            type="submit"
            className="w-full rounded-xl bg-blue-600 px-5 py-3.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 hover:shadow-md"
          >
            Change Password
          </button>

        </form>

      </div>

    </section>
  );
}

export default ChangePassword;
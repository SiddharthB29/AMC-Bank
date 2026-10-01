export default function Header({
  username,
  role,
  onLogout,
  children,
}) {
  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-white">
      <div className="mx-auto flex min-h-[78px] max-w-7xl items-center gap-6 px-4 sm:px-6 lg:px-8">

        {/* Brand */}
        <div className="flex shrink-0 items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 shadow-md shadow-blue-600/20">
            <span className="text-xl font-bold text-white">
              A
            </span>
          </div>

          <div className="hidden sm:block">
            <h1 className="text-lg font-bold tracking-tight text-slate-900">
              AMC Bank
            </h1>

            <p className="text-xs font-medium text-slate-400">
              Digital Banking
            </p>
          </div>
        </div>

        {/* Navigation */}
        <nav
          className="
            ml-auto
            flex
            items-center
            gap-2
            [&_button]:rounded-lg
            [&_button]:px-3.5
            [&_button]:py-2.5
            [&_button]:text-sm
            [&_button]:font-medium
            [&_button]:text-slate-600
            [&_button]:transition-all
            [&_button]:duration-200
            [&_button:hover]:bg-blue-50
            [&_button:hover]:text-blue-600
          "
        >
          {children}
        </nav>

        {/* Logout */}
        <button
          type="button"
          onClick={onLogout}
          className="
            shrink-0
            rounded-lg
            border
            border-red-200
            bg-red-50
            px-4
            py-2.5
            text-sm
            font-semibold
            text-red-600
            transition-all
            duration-200
            hover:border-red-300
            hover:bg-red-100
            hover:text-red-700
            active:scale-95
          "
        >
          Logout
        </button>

        {/* User */}
        <div className="hidden shrink-0 items-center gap-3 border-l border-slate-200 pl-5 lg:flex">

          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-50 text-sm font-bold text-blue-600">
            {username?.charAt(0)?.toUpperCase() || "U"}
          </div>

          <div className="max-w-[150px]">
            <p
              className="truncate text-sm font-semibold text-slate-800"
              title={username}
            >
              {username || "User"}
            </p>

            <p className="mt-0.5 text-xs font-medium text-slate-400">
              {role === "ROLE_ADMIN"
                ? "Administrator"
                : "Customer"}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
import React from 'react';

function Customers({
  customers,
  search,
  setSearch,
  loadCustomers,
  customerForm,
  setCustomerForm,
  editingId,
  setEditingId,
  saveCustomer,
  emptyCustomerForm,
  openCustomer,
  editCustomer,
  deleteCustomer,
}) {
  return (
    <section className="space-y-6">

      {/* Page Header */}
      <div>
        <p className="text-sm font-medium text-blue-600">
          Customer Management
        </p>

        <h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">
          Customers
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Create, manage and view customer accounts and login credentials.
        </p>
      </div>

      {/* Top Cards */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">

        {/* Customer Form */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm xl:col-span-2">

          <div className="mb-6 flex items-start justify-between">
            <div>
              <h3 className="text-lg font-semibold text-slate-900">
                {editingId
                  ? 'Edit Customer'
                  : 'Add Customer'}
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                {editingId
                  ? 'Update the customer information below.'
                  : 'Create a new customer and their login.'}
              </p>
            </div>

            <div className="rounded-xl bg-blue-50 px-3 py-2 text-xs font-semibold text-blue-700">
              {editingId ? 'EDIT MODE' : 'NEW CUSTOMER'}
            </div>
          </div>

          <form
            onSubmit={saveCustomer}
            className="space-y-5"
          >

            {/* Name + Email */}
            <div className="grid grid-cols-1 gap-5 md:grid-cols-2">

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  Full Name
                </label>

                <input
                  type="text"
                  placeholder="Enter customer name"
                  value={customerForm.name}
                  onChange={(e) =>
                    setCustomerForm({
                      ...customerForm,
                      name: e.target.value,
                    })
                  }
                  required
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  Email / Login ID
                </label>

                <input
                  type="email"
                  placeholder="customer@example.com"
                  value={customerForm.email}
                  onChange={(e) =>
                    setCustomerForm({
                      ...customerForm,
                      email: e.target.value,
                    })
                  }
                  required
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />
              </div>

            </div>

            {/* City + PAN */}
            <div className="grid grid-cols-1 gap-5 md:grid-cols-2">

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  City
                </label>

                <input
                  type="text"
                  placeholder="Enter city"
                  value={customerForm.city}
                  onChange={(e) =>
                    setCustomerForm({
                      ...customerForm,
                      city: e.target.value,
                    })
                  }
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  PAN Number
                </label>

                <input
                  type="text"
                  placeholder="ABCDE1234F"
                  value={customerForm.panNumber}
                  onChange={(e) =>
                    setCustomerForm({
                      ...customerForm,
                      panNumber: e.target.value.toUpperCase(),
                    })
                  }
                  required
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm uppercase outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />
              </div>

            </div>

            {/* Password */}
            {!editingId && (
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  First Login Password
                </label>

                <input
                  type="password"
                  minLength="6"
                  placeholder="Minimum 6 characters"
                  value={customerForm.initialPassword}
                  onChange={(e) =>
                    setCustomerForm({
                      ...customerForm,
                      initialPassword: e.target.value,
                    })
                  }
                  required
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                />

                <p className="mt-2 text-xs text-slate-500">
                  Give this temporary password to the customer.
                  They can change it after logging in.
                </p>
              </div>
            )}

            {editingId && (
              <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
                <p className="text-sm font-medium text-amber-800">
                  Login information
                </p>

                <p className="mt-1 text-xs text-amber-700">
                  Changing the email also changes the customer's
                  login ID. Password is not changed here.
                </p>
              </div>
            )}

            {/* Buttons */}
            <div className="flex flex-wrap gap-3 pt-2">

              <button
                type="submit"
                className="rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 hover:shadow-md"
              >
                {editingId
                  ? 'Update Customer'
                  : 'Create Customer + Login'}
              </button>

              {editingId && (
                <button
                  type="button"
                  onClick={() => {
                    setEditingId(null);
                    setCustomerForm(emptyCustomerForm());
                  }}
                  className="rounded-xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                >
                  Cancel
                </button>
              )}

            </div>

          </form>
        </div>

        {/* Search Card */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

          <div className="mb-5">
            <h3 className="text-lg font-semibold text-slate-900">
              Find Customer
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Search customers by name or city.
            </p>
          </div>

          <div className="space-y-4">

            <input
              type="text"
              placeholder="Search name or city..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            />

            <div className="flex gap-2">

              <button
                onClick={() => loadCustomers()}
                className="flex-1 rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
              >
                Search
              </button>

              <button
                onClick={() => {
                  setSearch('');
                  loadCustomers('');
                }}
                className="rounded-xl border border-slate-300 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              >
                Clear
              </button>

            </div>

          </div>

          {/* Login Rule */}
          <div className="mt-6 rounded-xl bg-slate-50 p-4">
            <p className="text-sm font-semibold text-slate-800">
              Customer login rule
            </p>

            <p className="mt-2 text-xs leading-5 text-slate-500">
              Login ID = customer's email address.
              Password = first password entered by the admin.
            </p>
          </div>

        </div>

      </div>

      {/* Customer List */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        {/* Table Header */}
        <div className="flex flex-col gap-2 border-b border-slate-200 px-6 py-5 sm:flex-row sm:items-center sm:justify-between">

          <div>
            <h3 className="text-lg font-semibold text-slate-900">
              Customer Directory
            </h3>

            <p className="text-sm text-slate-500">
              {customers.length} customer
              {customers.length !== 1 ? 's' : ''} found
            </p>
          </div>

          <div className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
            CUSTOMER RECORDS
          </div>

        </div>

        {/* Table */}
        <div className="overflow-x-auto">

          <table className="min-w-full text-left">

            <thead className="bg-slate-50">
              <tr>

                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                  ID
                </th>

                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Customer
                </th>

                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Login Email
                </th>

                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                  City
                </th>

                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-slate-500">
                  PAN
                </th>

                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Actions
                </th>

              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100">

              {customers.map((customer) => (
                <tr
                  key={customer.id}
                  className="transition hover:bg-slate-50"
                >

                  {/* ID */}
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-slate-500">
                    #{customer.id}
                  </td>

                  {/* Customer */}
                  <td className="whitespace-nowrap px-6 py-4">

                    <div className="flex items-center gap-3">

                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 text-sm font-bold text-blue-700">
                        {customer.name?.charAt(0)?.toUpperCase() || '?'}
                      </div>

                      <div>
                        <p className="text-sm font-semibold text-slate-900">
                          {customer.name}
                        </p>

                        <p className="text-xs text-slate-400">
                          Customer ID #{customer.id}
                        </p>
                      </div>

                    </div>

                  </td>

                  {/* Email */}
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-slate-600">
                    {customer.email}
                  </td>

                  {/* City */}
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-slate-600">
                    {customer.city || '—'}
                  </td>

                  {/* PAN */}
                  <td className="whitespace-nowrap px-6 py-4">

                    <span className="rounded-lg bg-slate-100 px-3 py-1.5 font-mono text-xs font-medium text-slate-700">
                      {customer.panNumber}
                    </span>

                  </td>

                  {/* Actions */}
                  <td className="whitespace-nowrap px-6 py-4">

                    <div className="flex justify-end gap-2">

                      <button
                        onClick={() => openCustomer(customer)}
                        className="rounded-lg bg-blue-50 px-3 py-2 text-xs font-semibold text-blue-700 transition hover:bg-blue-100"
                      >
                        View
                      </button>

                      <button
                        onClick={() => editCustomer(customer)}
                        className="rounded-lg bg-slate-100 px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-200"
                      >
                        Edit
                      </button>

                      <button
                        onClick={() => deleteCustomer(customer.id)}
                        className="rounded-lg bg-red-50 px-3 py-2 text-xs font-semibold text-red-600 transition hover:bg-red-100"
                      >
                        Delete
                      </button>

                    </div>

                  </td>

                </tr>
              ))}

              {/* Empty State */}
              {customers.length === 0 && (
                <tr>
                  <td
                    colSpan="6"
                    className="px-6 py-16 text-center"
                  >

                    <div className="mx-auto max-w-sm">

                      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-xl">
                        👤
                      </div>

                      <h4 className="mt-4 text-sm font-semibold text-slate-900">
                        No customers found
                      </h4>

                      <p className="mt-1 text-sm text-slate-500">
                        Try another search or create a new customer.
                      </p>

                    </div>

                  </td>
                </tr>
              )}

            </tbody>

          </table>

        </div>

      </div>

    </section>
  );
}

export default Customers;
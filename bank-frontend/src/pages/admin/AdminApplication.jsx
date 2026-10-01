import { useEffect, useState } from 'react';

import { request } from '../../services/api';

import Header from '../../components/Header';
import Message from '../../components/Message';

import AdminDashboard from './AdminDashboard';
import Customers from './Customers';
import CustomerDetails from './CustomerDetails';
import Loans from './Loans';

export default function AdminApplication({
  username,
  role,
  onLogout,
}) {
  // ==============================
  // View
  // ==============================

  const [view, setView] = useState('dashboard');

  // ==============================
  // Dashboard
  // ==============================

  const [stats, setStats] = useState({
    customers: 0,
    activeAccounts: 0,
    activeLoans: 0,
  });

  // ==============================
  // Customers
  // ==============================

  const [customers, setCustomers] = useState([]);
  const [search, setSearch] = useState('');

  // ==============================
  // Customer Details
  // ==============================

  const [selectedCustomer, setSelectedCustomer] =
    useState(null);

  const [accounts, setAccounts] = useState([]);
  const [customerLoans, setCustomerLoans] =
    useState([]);

  // ==============================
  // Loans
  // ==============================

  const [allLoans, setAllLoans] = useState([]);

  const [loanCustomerId, setLoanCustomerId] =
    useState('');

  // ==============================
  // Customer Form
  // ==============================

  const [editingId, setEditingId] =
    useState(null);

  const [customerForm, setCustomerForm] =
    useState({
      name: '',
      email: '',
      city: '',
      panNumber: '',
      initialPassword: '',
    });

  // ==============================
  // Account Form
  // ==============================

  const [accountForm, setAccountForm] =
    useState({
      type: 'SAVINGS',
      balance: 0,
    });

  // ==============================
  // Loan Form
  // ==============================

  const [loanForm, setLoanForm] =
    useState({
      type: 'HOME',
      principal: 10000,
      interestRate: 8,
      tenureMonths: 12,
    });

  // ==============================
  // Message
  // ==============================

  const [message, setMessage] = useState('');

  // ==============================
  // Initial Data
  // ==============================

  useEffect(() => {
    loadDashboard();
    loadCustomers('');
  }, []);

  // ==============================
  // Common Error Handler
  // ==============================

  async function run(action) {
    setMessage('');

    try {
      await action();
    } catch (err) {
      setMessage(err.message);
    }
  }

  // ==============================
  // Dashboard
  // ==============================

  async function loadDashboard() {
    const data = await request('/dashboard');
    setStats(data);
  }

  // ==============================
  // Customers
  // ==============================

  async function loadCustomers(text = search) {
    const data = await request(
      `/customers?search=${encodeURIComponent(text)}`
    );

    setCustomers(data);

    return data;
  }

  function emptyCustomerForm() {
    return {
      name: '',
      email: '',
      city: '',
      panNumber: '',
      initialPassword: '',
    };
  }

  async function saveCustomer(e) {
    e.preventDefault();

    await run(async () => {
      if (editingId) {
        const {
          initialPassword,
          ...updateData
        } = customerForm;

        await request(`/customers/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(updateData),
        });
      } else {
        await request('/customers', {
          method: 'POST',
          body: JSON.stringify(customerForm),
        });
      }

      setEditingId(null);
      setCustomerForm(emptyCustomerForm());

      await loadCustomers();
      await loadDashboard();

      setMessage(
        editingId
          ? 'Customer updated successfully'
          : 'Customer and login created successfully'
      );
    });
  }

  function editCustomer(customer) {
    setEditingId(customer.id);

    setCustomerForm({
      name: customer.name,
      email: customer.email,
      city: customer.city || '',
      panNumber: customer.panNumber,
      initialPassword: '',
    });

    setView('customers');
  }

  async function deleteCustomer(id) {
    if (
      !confirm(
        'Delete this customer and customer login?'
      )
    ) {
      return;
    }

    await run(async () => {
      await request(`/customers/${id}`, {
        method: 'DELETE',
      });

      await loadCustomers();
      await loadDashboard();

      setMessage(
        'Customer and login deleted'
      );
    });
  }

  // ==============================
  // Customer Details
  // ==============================

  async function openCustomer(customer) {
    await run(async () => {
      setSelectedCustomer(customer);

      const customerAccounts =
        await request(
          `/customers/${customer.id}/accounts`
        );

      const customerLoansData =
        await request(
          `/customers/${customer.id}/loans`
        );

      setAccounts(customerAccounts);
      setCustomerLoans(customerLoansData);

      setView('customer');
    });
  }

  // ==============================
  // Accounts
  // ==============================

  async function openAccount(e) {
    e.preventDefault();

    await run(async () => {
      await request(
        `/customers/${selectedCustomer.id}/accounts`,
        {
          method: 'POST',
          body: JSON.stringify({
            ...accountForm,
            balance: Number(accountForm.balance),
          }),
        }
      );

      setAccounts(
        await request(
          `/customers/${selectedCustomer.id}/accounts`
        )
      );

      await loadDashboard();

      setMessage(
        'Account opened successfully'
      );
    });
  }

  async function money(accountId, operation) {
    const amount = prompt(
      `Enter amount to ${operation}:`
    );

    if (!amount) {
      return;
    }

    await run(async () => {
      await request(
        `/accounts/${accountId}/${operation}?amount=${encodeURIComponent(
          amount
        )}`,
        {
          method: 'POST',
        }
      );

      setAccounts(
        await request(
          `/customers/${selectedCustomer.id}/accounts`
        )
      );

      setMessage(
        `${operation} completed`
      );
    });
  }

  // ==============================
  // Loans
  // ==============================

  function loanPayload() {
    return {
      ...loanForm,

      principal: Number(
        loanForm.principal
      ),

      interestRate: Number(
        loanForm.interestRate
      ),

      tenureMonths: Number(
        loanForm.tenureMonths
      ),
    };
  }

  async function sanctionLoanForCustomer(
    customerId
  ) {
    await request(
      `/customers/${customerId}/loans`,
      {
        method: 'POST',
        body: JSON.stringify(
          loanPayload()
        ),
      }
    );
  }

  async function sanctionLoanFromCustomer(e) {
    e.preventDefault();

    await run(async () => {
      await sanctionLoanForCustomer(
        selectedCustomer.id
      );

      setCustomerLoans(
        await request(
          `/customers/${selectedCustomer.id}/loans`
        )
      );

      await loadDashboard();

      setMessage(
        'Loan sanctioned successfully'
      );
    });
  }

  async function sanctionLoanFromModule(e) {
    e.preventDefault();

    await run(async () => {
      if (!loanCustomerId) {
        throw new Error(
          'Please select a customer'
        );
      }

      await sanctionLoanForCustomer(
        loanCustomerId
      );

      setAllLoans(
        await request('/loans')
      );

      await loadDashboard();

      setMessage(
        'Loan sanctioned successfully'
      );
    });
  }

  async function openLoansModule() {
    await run(async () => {
      const [
        customerData,
        loanData,
      ] = await Promise.all([
        request('/customers?search='),
        request('/loans'),
      ]);

      setCustomers(customerData);
      setAllLoans(loanData);

      if (
        !loanCustomerId &&
        customerData.length > 0
      ) {
        setLoanCustomerId(
          String(customerData[0].id)
        );
      }

      setView('loans');
    });
  }

  // ==============================
  // Render
  // ==============================

  return (
    <div className="min-h-screen bg-slate-50">

      {/* Header */}
      <Header
        username={username}
        role={role}
        onLogout={onLogout}
      >
        <button
          className="nav-button"
          onClick={() => {
            setView('dashboard');
            loadDashboard();
          }}
        >
          Dashboard
        </button>

        <button
          className="nav-button"
          onClick={() => {
            setView('customers');
            loadCustomers();
          }}
        >
          Customers
        </button>

        <button
          className="nav-button"
          onClick={openLoansModule}
        >
          Loans
        </button>
      </Header>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">

        {/* Messages */}
        <Message text={message} />

        {/* ==============================
            Dashboard
        ============================== */}

        {view === 'dashboard' && (
          <AdminDashboard
            stats={stats}
            onCustomers={() =>
              setView('customers')
            }
            onLoans={openLoansModule}
          />
        )}

        {/* ==============================
            Customers
        ============================== */}

        {view === 'customers' && (
          <Customers
            customers={customers}
            search={search}
            setSearch={setSearch}
            customerForm={customerForm}
            setCustomerForm={setCustomerForm}
            editingId={editingId}
            setEditingId={setEditingId}
            saveCustomer={saveCustomer}
            editCustomer={editCustomer}
            deleteCustomer={deleteCustomer}
            openCustomer={openCustomer}
            loadCustomers={loadCustomers}
            emptyCustomerForm={
              emptyCustomerForm
            }
          />
        )}

        {/* ==============================
            Customer Details
        ============================== */}

        {view === 'customer' &&
          selectedCustomer && (
            <CustomerDetails
              selectedCustomer={
                selectedCustomer
              }

              accounts={accounts}

              customerLoans={
                customerLoans
              }

              accountForm={
                accountForm
              }

              setAccountForm={
                setAccountForm
              }

              openAccount={
                openAccount
              }

              loanForm={
                loanForm
              }

              setLoanForm={
                setLoanForm
              }

              sanctionLoanFromCustomer={
                sanctionLoanFromCustomer
              }

              money={money}

              onBack={() =>
                setView('customers')
              }
            />
          )}

        {/* ==============================
            Loans
        ============================== */}

        {view === 'loans' && (
          <Loans
            customers={customers}
            allLoans={allLoans}
            loanCustomerId={
              loanCustomerId
            }
            setLoanCustomerId={
              setLoanCustomerId
            }
            loanForm={loanForm}
            setLoanForm={
              setLoanForm
            }
            sanctionLoanFromModule={
              sanctionLoanFromModule
            }
            openLoansModule={
              openLoansModule
            }
          />
        )}

      </main>
    </div>
  );
}
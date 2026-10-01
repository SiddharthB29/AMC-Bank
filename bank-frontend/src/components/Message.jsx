export default function Message({ text }) {
  if (!text) {
    return null;
  }

  const good =
    text.toLowerCase().includes('success') ||
    text.includes('completed') ||
    text.includes('deleted');

  return (
    <div
      className={`mb-6 flex items-center gap-3 rounded-xl border px-4 py-3 text-sm font-medium ${
        good
          ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
          : 'border-red-200 bg-red-50 text-red-700'
      }`}
    >
      <span
        className={`flex h-7 w-7 items-center justify-center rounded-full ${
          good
            ? 'bg-emerald-100'
            : 'bg-red-100'
        }`}
      >
        {good ? '✓' : '!'}
      </span>

      <span>{text}</span>
    </div>
  );
}
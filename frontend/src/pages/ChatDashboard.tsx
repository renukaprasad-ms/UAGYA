import { useState, type FormEvent } from "react";
import { getAuthUser } from "../services/authState";
import apiService from "../services/apiService";

const stats = [
  {
    label: "Active chats",
    value: "12",
    note: "4 waiting for follow-up",
  },
  {
    label: "Resolution rate",
    value: "92%",
    note: "Last 7 days",
  },
  {
    label: "Avg response",
    value: "42s",
    note: "Across all bots",
  },
];

const threads = [
  {
    id: "thread-1",
    title: "Subscription upgrade",
    preview: "Customer asking about yearly plan.",
    time: "2m ago",
  },
  {
    id: "thread-2",
    title: "Refund status",
    preview: "Chargeback on order #2318.",
    time: "12m ago",
  },
  {
    id: "thread-3",
    title: "Integration help",
    preview: "Need webhook example payload.",
    time: "1h ago",
  },
  {
    id: "thread-4",
    title: "Razorpay setup",
    preview: "Testing environment checklist.",
    time: "3h ago",
  },
];

const messages = [
  {
    id: "msg-1",
    role: "assistant",
    content:
      "Hey Renuka, I pulled the latest billing events. Want me to summarize the failed payments?",
  },
  {
    id: "msg-2",
    role: "user",
    content: "Yes, and draft a reply for anyone impacted by the upgrade issue.",
  },
  {
    id: "msg-3",
    role: "assistant",
    content:
      "On it. I found 3 failed charges, mostly due to expired cards. I can send a friendly follow-up with a payment link.",
  },
];

const bulkRegisterTotal = 100000;

type RegisterResponse = {
  success?: boolean;
  message?: string;
  error?: string;
};

export default function ChatDashboard() {
  const user = getAuthUser();
  const [draft, setDraft] = useState("");
  const [bulkStatus, setBulkStatus] = useState<"idle" | "running" | "done">(
    "idle",
  );
  const [bulkMessage, setBulkMessage] = useState<string | null>(null);
  const [bulkProgress, setBulkProgress] = useState({
    completed: 0,
    success: 0,
    failed: 0,
  });

  const handleSend = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!draft.trim()) return;
    setDraft("");
  };

  const handleBulkRegister = async () => {
    if (bulkStatus === "running") return;

    setBulkStatus("running");
    setBulkMessage(null);
    setBulkProgress({ completed: 0, success: 0, failed: 0 });

    const requests: Array<Promise<{ data?: RegisterResponse }>> = [];

    for (let index = 100; index <= bulkRegisterTotal; index += 1) {
      const payload = {
        firstname: `example${index}`,
        lastname: `${index}`,
        email: `renukaprasad${index}@gmail.com`,
        password: "Test@1234",
      };

      requests.push(
        apiService.post<RegisterResponse>("/api/user/create", payload),
      );
    }

    try {
      const results = await Promise.allSettled(requests);
      let success = 0;
      let failed = 0;

      results.forEach((result) => {
        if (
          result.status === "fulfilled" &&
          result.value.data?.success !== false
        ) {
          success += 1;
        } else {
          failed += 1;
        }
      });

      setBulkProgress({
        completed: results.length,
        success,
        failed,
      });
      setBulkMessage(`Completed ${success} succeeded, ${failed} failed.`);
    } finally {
      setBulkStatus("done");
    }
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.35em] text-cyan-300/80">
            UAGYA DASHBOARD
          </p>
          <h2 className="font-display text-2xl font-semibold text-white">
            Command your chatbot operations in one place.
          </h2>
          <p className="text-sm text-slate-400">
            {user?.firstname
              ? `Welcome back, ${user.firstname}.`
              : "Track conversations, drafts, and live routing."}
          </p>
        </div>
        <div className="flex flex-col items-start gap-2 sm:items-end">
          <div className="flex flex-wrap gap-3">
            <button
              className="rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-semibold text-slate-200 transition hover:bg-white/10"
              type="button"
            >
              New chat
            </button>
            <button
              className="rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-2 text-sm font-semibold text-slate-900 transition hover:brightness-105"
              type="button"
            >
              Launch flow
            </button>
            <button
              className="rounded-xl border border-amber-400/30 bg-amber-500/10 px-4 py-2 text-sm font-semibold text-amber-100 transition hover:border-amber-300/60 hover:bg-amber-500/20 disabled:cursor-not-allowed disabled:opacity-60"
              type="button"
              onClick={handleBulkRegister}
              disabled={bulkStatus === "running"}
            >
              {bulkStatus === "running"
                ? `Registering ${bulkProgress.completed}/${bulkRegisterTotal}`
                : "Test register x100"}
            </button>
          </div>
          {bulkStatus !== "idle" ? (
            <p className="text-xs text-slate-400">
              {bulkStatus === "running"
                ? `Success ${bulkProgress.success}, failed ${bulkProgress.failed}`
                : bulkMessage}
            </p>
          ) : null}
        </div>
      </header>

      <div className="grid gap-4 sm:grid-cols-3">
        {stats.map((stat) => (
          <div
            key={stat.label}
            className="rounded-2xl border border-white/10 bg-white/5 p-4 shadow-sm shadow-black/40"
          >
            <p className="text-xs uppercase tracking-widest text-slate-400">
              {stat.label}
            </p>
            <p className="mt-2 text-2xl font-semibold text-white">
              {stat.value}
            </p>
            <p className="mt-1 text-xs text-slate-400">{stat.note}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
        <aside className="rounded-2xl border border-white/10 bg-white/5 p-4">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-white">Active threads</p>
            <span className="text-xs text-slate-400">{threads.length}</span>
          </div>
          <div className="mt-4 space-y-3">
            {threads.map((thread) => (
              <button
                key={thread.id}
                className="w-full rounded-xl border border-white/10 bg-slate-900/40 px-3 py-3 text-left text-xs text-slate-200 transition hover:border-cyan-400/40 hover:bg-slate-900/60"
                type="button"
              >
                <p className="text-sm font-semibold text-white">
                  {thread.title}
                </p>
                <p className="mt-1 text-xs text-slate-400">{thread.preview}</p>
                <p className="mt-2 text-[11px] uppercase tracking-widest text-slate-500">
                  {thread.time}
                </p>
              </button>
            ))}
          </div>
        </aside>

        <section className="flex flex-col rounded-2xl border border-[color:var(--panel-border)] bg-[color:var(--panel)]/90 p-6 shadow-[0_30px_80px_-40px_rgba(0,0,0,0.8)] backdrop-blur">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="text-sm font-semibold text-white">
                Subscription upgrade
              </p>
              <p className="text-xs text-slate-400">Channel: Billing support</p>
            </div>
            <span className="rounded-full border border-cyan-400/40 bg-cyan-500/10 px-3 py-1 text-xs text-cyan-200">
              Live
            </span>
          </div>

          <div className="mt-6 flex-1 space-y-4">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`max-w-[80%] rounded-2xl border px-4 py-3 text-sm ${
                  message.role === "user"
                    ? "ml-auto border-cyan-400/40 bg-cyan-500/10 text-cyan-50"
                    : "border-white/10 bg-white/5 text-slate-100"
                }`}
              >
                {message.content}
              </div>
            ))}
          </div>

          <form className="mt-6 flex flex-col gap-3" onSubmit={handleSend}>
            <textarea
              className="min-h-[100px] w-full resize-none rounded-xl border border-white/10 bg-slate-900/60 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-500/30"
              placeholder="Draft a response or ask your bot to summarize..."
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
            />
            <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-slate-400">
              <span>Tip: Use /summarize to get a quick recap.</span>
              <button
                className="rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-2 text-sm font-semibold text-slate-900 transition hover:brightness-105"
                type="submit"
              >
                Send message
              </button>
            </div>
          </form>
        </section>
      </div>
    </div>
  );
}

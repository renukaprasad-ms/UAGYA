import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import apiService from "../services/apiService";

const inputClassName =
  "w-full rounded-xl border border-white/10 bg-slate-900/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 shadow-sm focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-500/30";

const labelClassName =
  "text-xs font-semibold uppercase tracking-wide text-slate-400";

type LoginResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: {
    firstname?: string;
    lastname?: string;
    email?: string;
  };
};

type CheckEmailResponse = {
  success?: boolean;
  message?: string;
  data?: {
    exists?: boolean;
  };
};

export default function Login() {
  const navigate = useNavigate();
  const [formValues, setFormValues] = useState({
    email: "",
    password: "",
    remember: false,
  });
  const [status, setStatus] = useState<
    "idle" | "loading" | "success" | "error"
  >("idle");
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [emailCheckStatus, setEmailCheckStatus] = useState<
    "idle" | "checking" | "exists" | "missing" | "error"
  >("idle");
  const [emailCheckMessage, setEmailCheckMessage] = useState<string | null>(null);

  useEffect(() => {
    const email = formValues.email.trim();

    if (!email || !email.includes("@")) {
      setEmailCheckStatus("idle");
      setEmailCheckMessage(null);
      return;
    }

    setEmailCheckStatus("idle");
    setEmailCheckMessage(null);

    let cancelled = false;
    const timer = setTimeout(() => {
      setEmailCheckStatus("checking");

      apiService
        .post<CheckEmailResponse>("/api/user/check-email", { email })
        .then((response) => {
          if (cancelled) return;
          const payload = response.data;

          if (payload?.success === false) {
            setEmailCheckStatus("error");
            setEmailCheckMessage(
              payload?.message || "Unable to verify email right now.",
            );
            return;
          }

          if (payload?.data?.exists) {
            setEmailCheckStatus("exists");
            setEmailCheckMessage(null);
            return;
          }

          setEmailCheckStatus("missing");
          setEmailCheckMessage("Email not found. Please register first.");
        })
        .catch((error) => {
          if (cancelled) return;
          setEmailCheckStatus("error");
          setEmailCheckMessage(
            error instanceof Error
              ? error.message
              : "Unable to verify email right now.",
          );
        });
    }, 400);

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [formValues.email]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setStatus("loading");
    setStatusMessage(null);

    if (emailCheckStatus === "missing") {
      setStatus("error");
      setStatusMessage("Email not found. Please register first.");
      return;
    }

    try {
      const response = await apiService.post<LoginResponse>("/api/auth/login", {
        email: formValues.email.trim(),
        password: formValues.password,
      });
      const payload = response.data;

      if (payload?.success === false) {
        setStatus("error");
        setStatusMessage(payload?.message || payload?.error || "Login failed.");
        return;
      }

      const resolvedEmail = payload?.data?.email || formValues.email.trim();
      setStatus("success");
      setStatusMessage(payload?.message || "OTP sent to your email.");
      navigate("/verify-otp", {
        state: {
          email: resolvedEmail,
          flow: "login",
          message: payload?.message || "OTP sent to your email.",
        },
      });
    } catch (error) {
      setStatus("error");
      setStatusMessage(
        error instanceof Error ? error.message : "Unable to log in right now.",
      );
    }
  };

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div className="space-y-2">
        <label className={labelClassName} htmlFor="login-email">
          Email
        </label>
        <input
          className={inputClassName}
          id="login-email"
          name="email"
          type="email"
          placeholder="name@company.ai"
          autoComplete="email"
          required
          value={formValues.email}
          onChange={(event) =>
            setFormValues((prev) => ({ ...prev, email: event.target.value }))
          }
        />
        {emailCheckStatus === "checking" ? (
          <p className="text-xs text-slate-400">Checking email...</p>
        ) : null}
        {emailCheckStatus === "missing" && emailCheckMessage ? (
          <p className="text-xs text-rose-300">{emailCheckMessage}</p>
        ) : null}
        {emailCheckStatus === "error" && emailCheckMessage ? (
          <p className="text-xs text-rose-300">{emailCheckMessage}</p>
        ) : null}
      </div>
      <div className="space-y-2">
        <label className={labelClassName} htmlFor="login-password">
          Password
        </label>
        <input
          className={inputClassName}
          id="login-password"
          name="password"
          type="password"
          placeholder="Your secure password"
          autoComplete="current-password"
          required
          value={formValues.password}
          onChange={(event) =>
            setFormValues((prev) => ({ ...prev, password: event.target.value }))
          }
        />
      </div>
      <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-slate-400">
        <label className="flex items-center gap-2">
          <input
            className="h-4 w-4 rounded border-white/20 bg-slate-900 text-cyan-400 focus:ring-cyan-500/40"
            type="checkbox"
            name="remember"
            checked={formValues.remember}
            onChange={(event) =>
              setFormValues((prev) => ({
                ...prev,
                remember: event.target.checked,
              }))
            }
          />
          Remember this device
        </label>
        <a className="text-cyan-300/80 transition hover:text-cyan-200" href="#">
          Forgot password?
        </a>
      </div>
      <button
        className="w-full rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-3 text-sm font-semibold text-slate-900 shadow-lg shadow-cyan-500/30 transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70"
        type="submit"
        disabled={status === "loading" || emailCheckStatus === "missing"}
      >
        {status === "loading" ? "Sending OTP..." : "Continue to console"}
      </button>
      {status === "error" && statusMessage ? (
        <div className="rounded-xl border border-rose-500/40 bg-rose-500/10 px-4 py-3 text-xs text-rose-200">
          {statusMessage}
        </div>
      ) : null}
      <p className="text-xs text-slate-400">
        New here? Create a workspace to start building.
      </p>
    </form>
  );
}

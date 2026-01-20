import { useEffect, useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import apiService from "../services/apiService";
import { setAuthenticated, setAuthUser } from "../services/authState";

const inputClassName =
  "w-full rounded-xl border border-white/10 bg-slate-900/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 shadow-sm focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-500/30";

const labelClassName =
  "text-xs font-semibold uppercase tracking-wide text-slate-400";

type VerifyOtpResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: {
    firstname?: string;
    lastname?: string;
    email?: string;
    role?: string;
    status?: string;
    id?: number;
  };
};

type ResendOtpResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: string;
};

export default function VerifyOtp() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state || {}) as {
    email?: string;
    flow?: "login" | "register";
    message?: string;
  };

  const [email, setEmail] = useState(
    state.email || sessionStorage.getItem("otpEmail") || "",
  );
  const [otp, setOtp] = useState("");
  const [status, setStatus] = useState<
    "idle" | "loading" | "success" | "error"
  >("idle");
  const [statusMessage, setStatusMessage] = useState<string | null>(
    state.message || null,
  );
  const [resendStatus, setResendStatus] = useState<
    "idle" | "loading" | "success" | "error"
  >("idle");
  const [resendMessage, setResendMessage] = useState<string | null>(null);

  useEffect(() => {
    if (state.email) {
      sessionStorage.setItem("otpEmail", state.email);
    }
  }, [state.email]);

  const handleVerify = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setStatus("loading");
    setStatusMessage(null);

    if (!email.trim()) {
      setStatus("error");
      setStatusMessage("Email is required to verify the OTP.");
      return;
    }

    try {
      const response = await apiService.post<VerifyOtpResponse>(
        "/api/auth/verify-otp",
        {
          email: email.trim(),
          otp: otp.trim(),
        },
      );
      const payload = response.data;

      if (payload?.success === false) {
        setStatus("error");
        setStatusMessage(payload?.message || payload?.error || "OTP failed.");
        return;
      }

      try {
        await apiService.post("/api/auth/refresh");
      } catch {
        // Ignore refresh failures here; next request will retry if needed.
      }

      setAuthenticated(true);
      setAuthUser(payload?.data ?? null);
      setStatus("success");
      setStatusMessage(payload?.message || "OTP verified successfully.");
      navigate("/select-plan", { state: { user: payload?.data || null } });
    } catch (error) {
      setStatus("error");
      setStatusMessage(
        error instanceof Error ? error.message : "Unable to verify OTP.",
      );
    }
  };

  const handleResend = async () => {
    setResendStatus("loading");
    setResendMessage(null);

    if (!email.trim()) {
      setResendStatus("error");
      setResendMessage("Email is required to resend the OTP.");
      return;
    }

    try {
      const response = await apiService.post<ResendOtpResponse>(
        "/api/auth/resend-otp",
        {
          email: email.trim(),
        },
      );
      const payload = response.data;

      if (payload?.success === false) {
        setResendStatus("error");
        setResendMessage(
          payload?.message || payload?.error || "Resend failed.",
        );
        return;
      }

      setResendStatus("success");
      setResendMessage(payload?.message || "OTP sent to email.");
    } catch (error) {
      setResendStatus("error");
      setResendMessage(
        error instanceof Error ? error.message : "Unable to resend OTP.",
      );
    }
  };

  return (
    <div className="space-y-5">
      <form className="space-y-5" onSubmit={handleVerify}>
        <div className="space-y-2">
          <label className={labelClassName} htmlFor="otp-email">
            Email
          </label>
          <input
            className={inputClassName}
            id="otp-email"
            name="email"
            type="email"
            placeholder="name@company.ai"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>
        <div className="space-y-2">
          <label className={labelClassName} htmlFor="otp-code">
            OTP code
          </label>
          <input
            className={inputClassName}
            id="otp-code"
            name="otp"
            type="text"
            inputMode="numeric"
            placeholder="Enter the 6-digit code"
            autoComplete="one-time-code"
            required
            value={otp}
            onChange={(event) => setOtp(event.target.value)}
          />
          <p className="text-xs text-slate-400">
            Use the code sent to your inbox. It expires in 10 minutes.
          </p>
        </div>
        <button
          className="w-full rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-3 text-sm font-semibold text-slate-900 shadow-lg shadow-cyan-500/30 transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70"
          type="submit"
          disabled={status === "loading"}
        >
          {status === "loading" ? "Verifying OTP..." : "Verify and continue"}
        </button>
        {status !== "idle" && statusMessage ? (
          <div
            className={`rounded-xl border px-4 py-3 text-xs ${
              status === "success"
                ? "border-emerald-500/40 bg-emerald-500/10 text-emerald-200"
                : "border-rose-500/40 bg-rose-500/10 text-rose-200"
            }`}
          >
            {statusMessage}
          </div>
        ) : null}
      </form>

      <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-slate-400">
        <button
          className="text-cyan-200 transition hover:text-cyan-100 disabled:cursor-not-allowed disabled:opacity-60"
          type="button"
          onClick={handleResend}
          disabled={resendStatus === "loading"}
        >
          {resendStatus === "loading" ? "Sending OTP..." : "Resend OTP"}
        </button>
        <Link
          className="text-cyan-200 transition hover:text-cyan-100"
          to={state.flow === "register" ? "/register" : "/login"}
        >
          {state.flow === "register" ? "Back to register" : "Back to login"}
        </Link>
      </div>
      {resendMessage ? (
        <div
          className={`rounded-xl border px-4 py-3 text-xs ${
            resendStatus === "success"
              ? "border-emerald-500/40 bg-emerald-500/10 text-emerald-200"
              : "border-rose-500/40 bg-rose-500/10 text-rose-200"
          }`}
        >
          {resendMessage}
        </div>
      ) : null}
    </div>
  );
}

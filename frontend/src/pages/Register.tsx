import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import apiService from "../services/apiService";

const inputClassName =
  "w-full rounded-xl border border-white/10 bg-slate-900/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-500 shadow-sm focus:border-cyan-400/60 focus:outline-none focus:ring-2 focus:ring-cyan-500/30";

const labelClassName =
  "text-xs font-semibold uppercase tracking-wide text-slate-400";

type RegisterResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: {
    firstname?: string;
    lastname?: string;
    email?: string;
  };
};

export default function Register() {
  const navigate = useNavigate();
  const [formValues, setFormValues] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    terms: false,
  });
  const [status, setStatus] = useState<
    "idle" | "loading" | "success" | "error"
  >("idle");
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setStatusMessage(null);

    if (formValues.password !== formValues.confirmPassword) {
      setStatus("error");
      setStatusMessage("Passwords do not match.");
      return;
    }

    setStatus("loading");

    try {
      const response = await apiService.post<RegisterResponse>(
        "/api/user/create",
        {
          firstname: formValues.firstName.trim(),
          lastname: formValues.lastName.trim(),
          email: formValues.email.trim(),
          password: formValues.password,
        },
      );
      const payload = response.data;

      if (payload?.success === false) {
        setStatus("error");
        setStatusMessage(payload?.message || payload?.error || "Registration failed.");
        return;
      }

      const resolvedEmail = payload?.data?.email || formValues.email.trim();
      setStatus("success");
      navigate("/verify-otp", {
        state: {
          email: resolvedEmail,
          flow: "register",
          message: payload?.message || "OTP sent to your email.",
        },
      });
    } catch (error) {
      setStatus("error");
      setStatusMessage(
        error instanceof Error
          ? error.message
          : "Unable to connect right now. Please try again.",
      );
    }
  };

  return (
    <form className="space-y-5" onSubmit={handleSubmit}>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <label className={labelClassName} htmlFor="register-first-name">
            First name
          </label>
          <input
            className={inputClassName}
            id="register-first-name"
            name="firstName"
            type="text"
            placeholder="Renuka"
            autoComplete="given-name"
            required
            value={formValues.firstName}
            onChange={(event) =>
              setFormValues((prev) => ({
                ...prev,
                firstName: event.target.value,
              }))
            }
          />
        </div>
        <div className="space-y-2">
          <label className={labelClassName} htmlFor="register-last-name">
            Last name
          </label>
          <input
            className={inputClassName}
            id="register-last-name"
            name="lastName"
            type="text"
            placeholder="P"
            autoComplete="family-name"
            required
            value={formValues.lastName}
            onChange={(event) =>
              setFormValues((prev) => ({
                ...prev,
                lastName: event.target.value,
              }))
            }
          />
        </div>
      </div>
      <div className="space-y-2">
        <label className={labelClassName} htmlFor="register-email">
          Work email
        </label>
        <input
          className={inputClassName}
          id="register-email"
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
      </div>
      <div className="space-y-2">
        <label className={labelClassName} htmlFor="register-password">
          Password
        </label>
        <input
          className={inputClassName}
          id="register-password"
          name="password"
          type="password"
          placeholder="Create a secure password"
          autoComplete="new-password"
          required
          value={formValues.password}
          onChange={(event) =>
            setFormValues((prev) => ({ ...prev, password: event.target.value }))
          }
        />
      </div>
      <div className="space-y-2">
        <label className={labelClassName} htmlFor="register-confirm">
          Confirm password
        </label>
        <input
          className={inputClassName}
          id="register-confirm"
          name="confirmPassword"
          type="password"
          placeholder="Confirm your password"
          autoComplete="new-password"
          required
          value={formValues.confirmPassword}
          onChange={(event) =>
            setFormValues((prev) => ({
              ...prev,
              confirmPassword: event.target.value,
            }))
          }
        />
      </div>
      <label className="flex items-start gap-3 text-xs text-slate-400">
        <input
          className="mt-1 h-4 w-4 rounded border-white/20 bg-slate-900 text-cyan-400 focus:ring-cyan-500/40"
          type="checkbox"
          name="terms"
          required
          checked={formValues.terms}
          onChange={(event) =>
            setFormValues((prev) => ({ ...prev, terms: event.target.checked }))
          }
        />
        <span>I agree to the Terms and acknowledge the Privacy Policy.</span>
      </label>
      <button
        className="w-full rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-3 text-sm font-semibold text-slate-900 shadow-lg shadow-cyan-500/30 transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70"
        type="submit"
        disabled={status === "loading"}
      >
        {status === "loading" ? "Creating workspace..." : "Create workspace"}
      </button>
      {status === "error" && statusMessage ? (
        <div className="rounded-xl border border-rose-500/40 bg-rose-500/10 px-4 py-3 text-xs text-rose-200">
          {statusMessage}
        </div>
      ) : null}
      <p className="text-xs text-slate-400">
        Already have access? Switch to login.
      </p>
    </form>
  );
}

import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import planService from "../services/planService";
import { getAuthUser } from "../services/authState";

type Plan = {
  id: number;
  code: string;
  name: string;
  planType: string;
  billingCycle: string;
  price: number;
  currency: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

type PlansResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: Plan[];
};

type CheckoutResponse = {
  success?: boolean;
  message?: string;
  error?: string;
  data?: {
    provider?: string;
    orderId?: string;
    amount?: number;
    currency?: string;
  };
};

type RazorpayOptions = {
  key: string;
  order_id: string;
  amount?: number;
  currency?: string;
  name?: string;
  description?: string;
  prefill?: {
    name?: string;
    email?: string;
  };
  handler?: (response: unknown) => void;
  modal?: {
    ondismiss?: () => void;
  };
};

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayOptions) => { open: () => void };
  }
}

export default function SelectPlan() {
  const location = useLocation();
  const state = (location.state || {}) as {
    user?: {
      firstname?: string;
      lastname?: string;
      email?: string;
    } | null;
  };
  const user = state.user || getAuthUser();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [status, setStatus] = useState<"idle" | "loading" | "error">("idle");
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [selectedPlan, setSelectedPlan] = useState<Plan | null>(null);
  const [showGateway, setShowGateway] = useState(false);
  const [gatewayChoice, setGatewayChoice] = useState("razorpay");
  const [checkoutStatus, setCheckoutStatus] = useState<
    "idle" | "loading" | "error"
  >("idle");
  const [checkoutMessage, setCheckoutMessage] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    const loadPlans = async () => {
      setStatus("loading");
      setStatusMessage(null);

      try {
        const response = await planService.get<PlansResponse>("/api/plan");
        const payload = response.data;

        if (payload?.success === false) {
          throw new Error(
            payload?.message || payload?.error || "Failed to load plans.",
          );
        }

        const activePlans = (payload?.data || []).filter((plan) => plan.active);
        if (mounted) {
          setPlans(activePlans);
          setStatus("idle");
        }
      } catch (error) {
        if (mounted) {
          setStatus("error");
          setStatusMessage(
            error instanceof Error ? error.message : "Failed to load plans.",
          );
        }
      }
    };

    loadPlans();

    return () => {
      mounted = false;
    };
  }, []);

  const currencyFormatter = useMemo(() => {
    return (currency: string) =>
      new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: currency || "USD",
      });
  }, []);

  const formatPlanPrice = (plan: Plan) => {
    const formatter = currencyFormatter(plan.currency);
    const amount = formatter.format(plan.price);
    const cycle =
      plan.planType === "ONE_TIME"
        ? "one-time"
        : plan.billingCycle.toLowerCase();
    return `${amount} / ${cycle}`;
  };

  const openGatewayModal = (plan: Plan) => {
    setSelectedPlan(plan);
    setGatewayChoice("razorpay");
    setShowGateway(true);
    setCheckoutStatus("idle");
    setCheckoutMessage(null);
  };

  const closeGatewayModal = () => {
    setShowGateway(false);
  };

  const loadRazorpaySdk = () =>
    new Promise<boolean>((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }

      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });

  const handleProceed = async () => {
    if (!selectedPlan) return;

    const authUser = getAuthUser();
    if (!authUser?.id) {
      setCheckoutStatus("error");
      setCheckoutMessage("Missing user information. Please log in again.");
      return;
    }

    const razorpayKey = import.meta.env.VITE_RAZORPAY_KEY_ID;
    if (!razorpayKey) {
      setCheckoutStatus("error");
      setCheckoutMessage("Razorpay key is missing.");
      return;
    }

    setCheckoutStatus("loading");
    setCheckoutMessage(null);

    try {
      const response = await planService.post<CheckoutResponse>(
        "/api/checkout",
        {
          userId: String(authUser.id),
          planId: selectedPlan.id,
          paymentProvider: "RAZORPAY",
          paymentMethod: "UPI",
        },
      );
      const payload = response.data;

      if (payload?.success === false) {
        setCheckoutStatus("error");
        setCheckoutMessage(
          payload?.message || payload?.error || "Checkout failed.",
        );
        return;
      }

      const orderId = payload?.data?.orderId;
      if (!orderId) {
        setCheckoutStatus("error");
        setCheckoutMessage("Unable to start payment. Missing order details.");
        return;
      }

      const sdkReady = await loadRazorpaySdk();
      if (!sdkReady || !window.Razorpay) {
        setCheckoutStatus("error");
        setCheckoutMessage("Failed to load Razorpay checkout.");
        return;
      }

      const rawAmount = payload?.data?.amount;
      const amount = Number.isInteger(rawAmount)
        ? rawAmount
        : typeof rawAmount === "number"
          ? Math.round(rawAmount * 100)
          : undefined;

      const razorpay = new window.Razorpay({
        key: razorpayKey,
        order_id: orderId,
        amount,
        currency: payload?.data?.currency || "USD",
        name: "UAGYA",
        description: selectedPlan.name,
        prefill: {
          name: user?.firstname
            ? `${user.firstname} ${user.lastname || ""}`.trim()
            : undefined,
          email: user?.email,
        },
        handler: () => {
          setCheckoutStatus("idle");
        },
        modal: {
          ondismiss: () => {
            setCheckoutStatus("idle");
          },
        },
      });

      razorpay.open();
      setShowGateway(false);
    } catch (error) {
      setCheckoutStatus("error");
      setCheckoutMessage(
        error instanceof Error ? error.message : "Checkout failed.",
      );
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <p className="text-xs font-semibold uppercase tracking-[0.35em] text-cyan-300/80">
          SELECT PLAN
        </p>
        <h2 className="font-display text-2xl font-semibold text-white">
          Choose the plan that fits your launch.
        </h2>
        <p className="text-sm text-slate-400">
          {user?.firstname
            ? `Welcome, ${user.firstname}. Pick a plan to activate your workspace.`
            : "Pick a plan to activate your workspace and unlock chat flows."}
        </p>
      </div>
      <div className="grid gap-4">
        {status === "loading" ? (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-5 text-sm text-slate-300">
            Loading plans...
          </div>
        ) : null}

        {status === "error" && statusMessage ? (
          <div className="rounded-2xl border border-rose-500/40 bg-rose-500/10 p-5 text-sm text-rose-200">
            {statusMessage}
          </div>
        ) : null}

        {status === "idle" && plans.length === 0 ? (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-5 text-sm text-slate-300">
            No active plans available right now.
          </div>
        ) : null}

        {plans.map((plan) => (
          <div
            key={plan.id}
            className={`rounded-2xl border bg-white/5 p-5 shadow-sm shadow-black/40 ${
              selectedPlan?.id === plan.id
                ? "border-cyan-400/60"
                : "border-white/10"
            }`}
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-white">{plan.name}</h3>
                <p className="text-xs text-slate-400">
                  {plan.planType} • {plan.billingCycle}
                </p>
              </div>
              <span className="text-sm font-semibold text-cyan-200">
                {formatPlanPrice(plan)}
              </span>
            </div>
            <button
              className="mt-4 w-full rounded-xl border border-cyan-400/40 bg-cyan-500/10 px-4 py-2 text-sm font-semibold text-cyan-100 transition hover:bg-cyan-500/20"
              type="button"
              onClick={() => openGatewayModal(plan)}
            >
              Select {plan.name}
            </button>
          </div>
        ))}
      </div>
      {showGateway && selectedPlan ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 px-6 py-10">
          <div className="w-full max-w-lg rounded-2xl border border-white/10 bg-slate-950/90 p-6 shadow-[0_25px_70px_-40px_rgba(0,0,0,0.9)]">
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-1">
                <h3 className="text-lg font-semibold text-white">
                  Choose a payment gateway
                </h3>
                <p className="text-xs text-slate-400">
                  Plan: {selectedPlan.name} - {formatPlanPrice(selectedPlan)}
                </p>
              </div>
              <button
                className="text-sm text-slate-400 transition hover:text-white"
                type="button"
                onClick={closeGatewayModal}
              >
                Close
              </button>
            </div>

            <div className="mt-6 space-y-3">
              <label className="flex items-start gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-slate-200">
                <input
                  className="mt-1 h-4 w-4 rounded border-white/20 bg-slate-900 text-cyan-400 focus:ring-cyan-500/40"
                  type="radio"
                  name="gateway"
                  value="razorpay"
                  checked={gatewayChoice === "razorpay"}
                  onChange={() => setGatewayChoice("razorpay")}
                />
                <span>
                  <span className="block font-semibold">Razorpay</span>
                  <span className="block text-xs text-slate-400">
                    Cards, UPI, netbanking, and wallets.
                  </span>
                </span>
              </label>
            </div>

            <div className="mt-6 flex flex-col gap-3 sm:flex-row">
              <button
                className="flex-1 rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-semibold text-slate-200 transition hover:bg-white/10"
                type="button"
                onClick={closeGatewayModal}
              >
                Cancel
              </button>
              <button
                className="flex-1 rounded-xl bg-gradient-to-r from-cyan-400 via-sky-400 to-emerald-400 px-4 py-2 text-sm font-semibold text-slate-900 transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70"
                type="button"
                onClick={handleProceed}
                disabled={checkoutStatus === "loading"}
              >
                {checkoutStatus === "loading"
                  ? "Starting checkout..."
                  : "Continue with Razorpay"}
              </button>
            </div>
            {checkoutStatus === "error" && checkoutMessage ? (
              <div className="mt-4 rounded-xl border border-rose-500/40 bg-rose-500/10 px-4 py-3 text-xs text-rose-200">
                {checkoutMessage}
              </div>
            ) : null}
          </div>
        </div>
      ) : null}
    </div>
  );
}

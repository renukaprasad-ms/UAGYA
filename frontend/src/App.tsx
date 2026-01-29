import {
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import VerifyOtp from "./pages/VerifyOtp";
import SelectPlan from "./pages/SelectPlan";
import ChatDashboard from "./pages/ChatDashboard";
import { isAuthenticated } from "./services/authState";

const PublicRoute = () => {
  return isAuthenticated() ? <Navigate to="/select-plan" replace /> : <Outlet />;
};

const PrivateRoute = () => {
  return isAuthenticated() ? <Outlet /> : <Navigate to="/login" replace />;
};

function App() {
  const location = useLocation();
  const pathname = location.pathname;
  const isDashboard = pathname.startsWith("/dashboard");
  const isLoginRoute = pathname === "/login";
  const showAuthTabs = pathname === "/login" || pathname === "/register";

  const cardCopy = (() => {
    switch (pathname) {
      case "/register":
        return {
          title: "Create your workspace",
          subtitle: "Launch your UAGYA workspace in under a minute.",
        };
      case "/verify-otp":
        return {
          title: "Verify your OTP",
          subtitle: "Enter the code we sent to confirm your email.",
        };
      case "/select-plan":
        return {
          title: "Select your plan",
          subtitle: "Activate your workspace with a plan that fits.",
        };
      default:
        return {
          title: "Welcome back",
          subtitle: "Pick up where your last conversation left off.",
        };
    }
  })();

  const tabClassName = (active: boolean) =>
    [
      "rounded-full px-4 py-2 text-xs font-semibold transition",
      active
        ? "bg-cyan-300 text-slate-900 shadow-sm shadow-cyan-500/30"
        : "text-slate-300 hover:text-white",
    ].join(" ");

  const routes = (
    <Routes location={location}>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-otp" element={<VerifyOtp />} />
      </Route>
      <Route element={<PrivateRoute />}>
        <Route path="/select-plan" element={<SelectPlan />} />
        <Route path="/dashboard" element={<ChatDashboard />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );

  return (
    <div className="relative min-h-screen overflow-hidden">
      <div className="pointer-events-none absolute -top-32 right-6 h-72 w-72 rounded-full bg-cyan-500/25 blur-3xl animate-float" />
      <div
        className="pointer-events-none absolute -bottom-32 left-6 h-80 w-80 rounded-full bg-emerald-500/20 blur-3xl animate-float"
        style={{ animationDelay: '1.6s' }}
      />
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(255,255,255,0.06),transparent_55%)]" />

      {isDashboard ? (
        <div className="relative z-10 mx-auto flex min-h-screen w-full max-w-7xl flex-col px-6 py-12">
          {routes}
        </div>
      ) : (
        <div className="relative z-10 mx-auto flex min-h-screen w-full max-w-6xl flex-col justify-center gap-12 px-6 py-16 lg:grid lg:grid-cols-[1.05fr_0.95fr] lg:gap-12">
          <section className="space-y-8 font-body animate-fade-in">
            <p className="text-xs font-semibold uppercase tracking-[0.35em] text-cyan-300/80">
              UAGYA CONSOLE
            </p>
            <h1 className="font-display text-4xl font-semibold leading-tight text-white md:text-5xl">
              Your chatbot HQ, tuned for late-night builders.
            </h1>
            <p className="max-w-xl text-base text-slate-300">
              Sign in to manage agents, datasets, and live conversations in a
              calm, low-glare workspace built for real shipping.
            </p>
            <div className="grid gap-4 sm:grid-cols-2">
              <div
                className="rounded-2xl border border-white/10 bg-white/5 p-4 shadow-sm shadow-black/40 animate-fade-in"
                style={{ animationDelay: "120ms" }}
              >
                <p className="text-xs uppercase tracking-widest text-slate-400">
                  Conversation memory
                </p>
                <p className="mt-3 text-sm text-slate-200">
                  Keep every reply grounded with persistent context and guardrails.
                </p>
              </div>
              <div
                className="rounded-2xl border border-white/10 bg-white/5 p-4 shadow-sm shadow-black/40 animate-fade-in"
                style={{ animationDelay: "220ms" }}
              >
                <p className="text-xs uppercase tracking-widest text-slate-400">
                  Deploy-ready flows
                </p>
                <p className="mt-3 text-sm text-slate-200">
                  Ship and monitor bot workflows with a clean, focused entry point.
                </p>
              </div>
            </div>
          </section>

          <section className="rounded-3xl border border-[color:var(--panel-border)] bg-[color:var(--panel)]/90 p-8 shadow-[0_30px_80px_-40px_rgba(0,0,0,0.9)] backdrop-blur animate-fade-in-slow">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="space-y-1">
                <h2 className="font-display text-xl font-semibold text-white">
                  {cardCopy.title}
                </h2>
                <p className="text-sm text-slate-400">
                  {cardCopy.subtitle}
                </p>
              </div>
              {showAuthTabs ? (
                <div className="inline-flex rounded-full border border-white/10 bg-slate-900/70 p-1">
                  <Link className={tabClassName(isLoginRoute)} to="/login">
                    Login
                  </Link>
                  <Link className={tabClassName(!isLoginRoute)} to="/register">
                    Register
                  </Link>
                </div>
              ) : null}
            </div>
            <div key={pathname} className="mt-8 animate-fade-in">
              {routes}
            </div>
          </section>
        </div>
      )}
    </div>
  )
}

export default App

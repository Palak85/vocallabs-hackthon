import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { label: "Platform", path: "/" },
    { label: "Solutions", path: "/solutions" },
    { label: "Resources", path: "/resources" },
    { label: "Pricing", path: "/pricing" },
  ];

  return (
    <nav className="bg-surface/90 backdrop-blur-md shadow-xs transition-all duration-200 ease-in-out fixed top-0 left-0 w-full z-50 border-b border-outline-variant/50">
      <div className="flex justify-between items-center px-6 py-4 max-w-7xl mx-auto">
        {/* Logo */}
        <Link
          to="/"
          className="flex items-center gap-2 text-headline-sm font-bold text-primary cursor-pointer hover:opacity-90 transition-opacity"
        >
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center text-on-primary">
            <span className="material-symbols-outlined text-[20px]">
              smart_toy
            </span>
          </div>
          <span>SuperviseAI Hub</span>
        </Link>

        {/* Desktop Nav Links */}
        <div className="hidden md:flex items-center gap-7">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.label}
                to={item.path}
                className={`text-body-sm font-semibold transition-all tracking-wide relative py-1 ${
                  isActive
                    ? "text-primary"
                    : "text-on-surface-variant hover:text-primary"
                }`}
              >
                {item.label}
                {isActive && (
                  <span className="absolute bottom-0 left-0 w-full h-0.5 bg-primary rounded-full" />
                )}
              </Link>
            );
          })}
        </div>

        {/* Desktop CTA */}
        <div className="hidden md:flex items-center gap-3">
          <button
            onClick={() => navigate("/dashboard")}
            className="text-on-surface-variant hover:text-primary hover:bg-surface-container font-semibold text-label-md px-3.5 py-2 rounded-lg border border-outline-variant/60 transition-all flex items-center gap-1.5 cursor-pointer"
          >
            <span className="material-symbols-outlined text-[16px]">
              admin_panel_settings
            </span>
            Admin Dashboard
          </button>
          <button
            onClick={() => navigate("/customer")}
            className="bg-primary text-on-primary font-semibold text-label-md px-5 py-2.5 rounded-lg shadow-sm hover:bg-primary-container hover:text-on-primary-container transition-all flex items-center gap-1.5 cursor-pointer"
          >
            Get Started
            <span className="material-symbols-outlined text-[16px]">
              arrow_forward
            </span>
          </button>
        </div>

        {/* Mobile Menu Toggle */}
        <button
          className="md:hidden text-on-surface-variant cursor-pointer p-1"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          <span className="material-symbols-outlined text-2xl">
            {mobileOpen ? "close" : "menu"}
          </span>
        </button>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="md:hidden bg-surface border-t border-outline-variant px-6 py-5 space-y-4 shadow-lg">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.label}
                to={item.path}
                onClick={() => setMobileOpen(false)}
                className={`block text-body-sm font-semibold py-2 px-3 rounded-lg transition-colors ${
                  isActive
                    ? "bg-primary-fixed text-primary font-bold"
                    : "text-on-surface-variant hover:text-primary hover:bg-surface-container-low"
                }`}
              >
                {item.label}
              </Link>
            );
          })}
          <hr className="border-outline-variant" />
          <div className="flex flex-col gap-2">
            <button
              onClick={() => {
                navigate("/dashboard");
                setMobileOpen(false);
              }}
              className="w-full bg-surface-container text-on-surface font-semibold text-label-md px-4 py-2.5 rounded-lg border border-outline-variant transition-colors cursor-pointer flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined text-[16px]">
                admin_panel_settings
              </span>
              Admin Dashboard
            </button>
            <button
              onClick={() => {
                navigate("/customer");
                setMobileOpen(false);
              }}
              className="w-full bg-primary text-on-primary font-semibold text-label-md px-4 py-3 rounded-lg shadow-sm hover:bg-primary-container transition-colors cursor-pointer flex items-center justify-center gap-2"
            >
              Get Started
              <span className="material-symbols-outlined text-[16px]">
                arrow_forward
              </span>
            </button>
          </div>
        </div>
      )}
    </nav>
  );
}

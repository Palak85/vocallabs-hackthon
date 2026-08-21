import { useState } from "react";

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <nav className="bg-surface/80 backdrop-blur-md shadow-sm transition-all duration-200 ease-in-out fixed top-0 left-0 w-full z-50">
      <div className="flex justify-between items-center px-6 py-4 max-w-7xl mx-auto bg-surface">
        {/* Logo */}
        <div className="text-headline-sm font-bold text-primary">
          Luminous Support
        </div>

        {/* Desktop Nav Links */}
        <div className="hidden md:flex gap-6">
          {["Platform", "Solutions", "Resources", "Pricing"].map((item) => (
            <a
              key={item}
              href="#"
              className="text-label-md font-semibold text-on-surface-variant hover:text-primary transition-colors tracking-wide"
            >
              {item}
            </a>
          ))}
        </div>

        {/* Desktop Auth Buttons */}
        <div className="hidden md:flex items-center gap-4">
          <a
            href="#"
            className="text-label-md font-semibold text-on-surface-variant hover:text-primary transition-colors"
          >
            Log In
          </a>
          <button className="bg-primary text-on-primary font-semibold text-label-md px-4 py-2 rounded-lg shadow-sm hover:bg-primary-container hover:text-on-primary-container transition-colors cursor-pointer">
            Get Started
          </button>
        </div>

        {/* Mobile Menu Toggle */}
        <button
          className="md:hidden text-on-surface-variant cursor-pointer"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          <span className="material-symbols-outlined">
            {mobileOpen ? "close" : "menu"}
          </span>
        </button>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="md:hidden bg-surface border-t border-outline-variant px-6 py-4 space-y-4 animate-in">
          {["Platform", "Solutions", "Resources", "Pricing"].map((item) => (
            <a
              key={item}
              href="#"
              className="block text-label-md font-semibold text-on-surface-variant hover:text-primary transition-colors"
            >
              {item}
            </a>
          ))}
          <hr className="border-outline-variant" />
          <a
            href="#"
            className="block text-label-md font-semibold text-on-surface-variant hover:text-primary transition-colors"
          >
            Log In
          </a>
          <button className="w-full bg-primary text-on-primary font-semibold text-label-md px-4 py-2 rounded-lg shadow-sm hover:bg-primary-container transition-colors cursor-pointer">
            Get Started
          </button>
        </div>
      )}
    </nav>
  );
}

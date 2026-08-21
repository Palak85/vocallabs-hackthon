import { Link } from "react-router-dom";

const footerLinks = [
  { label: "Platform", path: "/" },
  { label: "Solutions", path: "/solutions" },
  { label: "Resources", path: "/resources" },
  { label: "Pricing", path: "/pricing" },
  { label: "Dashboard", path: "/dashboard" },
];

export default function Footer() {
  return (
    <footer className="bg-surface-container-lowest border-t border-outline-variant">
      <div className="w-full py-12 px-6 flex flex-col md:flex-row justify-between items-center gap-6 max-w-7xl mx-auto">
        {/* Logo */}
        <Link
          to="/"
          className="flex items-center gap-2 text-headline-sm font-bold text-primary hover:opacity-90 transition-opacity"
        >
          <div className="w-7 h-7 rounded-lg bg-primary flex items-center justify-center text-on-primary">
            <span className="material-symbols-outlined text-[18px]">
              smart_toy
            </span>
          </div>
          <span>SuperviseAI Hub</span>
        </Link>

        {/* Links */}
        <div className="flex flex-wrap justify-center gap-6">
          {footerLinks.map((link) => (
            <Link
              key={link.label}
              to={link.path}
              className="text-body-sm font-medium text-on-surface-variant hover:text-primary hover:underline transition-colors"
            >
              {link.label}
            </Link>
          ))}
        </div>

        {/* Copyright */}
        <div className="text-body-sm text-on-surface-variant text-center md:text-right">
          © {new Date().getFullYear()} SuperviseAI Hub. AI-Driven, Human-Supervised.
        </div>
      </div>
    </footer>
  );
}

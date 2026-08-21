const footerLinks = ["Product", "Company", "Security", "Privacy Policy", "Terms of Service"];

export default function Footer() {
  return (
    <footer className="bg-surface-container-lowest border-t border-outline-variant opacity-90 hover:opacity-100 transition-opacity">
      <div className="w-full py-12 px-6 flex flex-col md:flex-row justify-between items-center gap-4 max-w-7xl mx-auto">
        {/* Logo */}
        <div className="text-headline-sm font-bold text-primary mb-4 md:mb-0">
          Luminous Support
        </div>

        {/* Links */}
        <div className="flex flex-wrap justify-center gap-6">
          {footerLinks.map((link) => (
            <a
              key={link}
              href="#"
              className="text-body-sm text-on-surface-variant hover:text-primary hover:underline transition-colors"
            >
              {link}
            </a>
          ))}
        </div>

        {/* Copyright */}
        <div className="text-body-sm text-on-surface-variant mt-4 md:mt-0 text-center md:text-right">
          © 2024 Luminous Support. AI-Driven, Human-Supervised.
        </div>
      </div>
    </footer>
  );
}

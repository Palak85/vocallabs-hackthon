import { useNavigate } from "react-router-dom";

export default function DashboardHeader() {
  const navigate = useNavigate();

  return (
    <header className="bg-dash-surface-container-lowest shadow-sm flex justify-between items-center px-4 w-full h-16 sticky top-0 z-50">
      <div className="flex items-center gap-6">
        {/* Logo */}
        <div className="flex items-center gap-2">
          <span
            className="material-symbols-outlined text-dash-primary text-2xl"
            style={{ fontVariationSettings: "'FILL' 1" }}
          >
            support_agent
          </span>
          <span
            className="text-headline-sm font-bold text-dash-primary cursor-pointer"
            onClick={() => navigate("/")}
          >
            SuperviseAI Hub
          </span>
        </div>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex gap-6 h-full items-center">
          {["Dashboard", "Analytics", "Team", "Settings"].map((item) => (
            <a
              key={item}
              href="#"
              className="text-dash-on-surface-variant font-medium hover:text-dash-primary transition-colors cursor-pointer active:opacity-80 text-label-md"
            >
              {item}
            </a>
          ))}
        </nav>
      </div>

      <div className="flex items-center gap-4">
        {/* Live Monitor Badge */}
        <div className="hidden md:flex items-center px-3 py-1.5 bg-dash-error-container text-dash-on-error-container rounded-full gap-2">
          <span className="w-2 h-2 rounded-full bg-error animate-pulse" />
          <span className="text-label-md font-semibold">Live Monitor Active</span>
        </div>

        {/* Live Monitor Button */}
        <button className="bg-dash-primary text-dash-on-primary px-4 py-2 rounded-lg text-label-md font-semibold hover:opacity-90 transition-colors shadow-sm hidden md:block cursor-pointer">
          Live Monitor
        </button>

        {/* Icon Buttons */}
        <div className="flex gap-2">
          <button
            aria-label="Notifications"
            className="p-2 text-dash-on-surface-variant hover:text-dash-primary transition-colors rounded-full hover:bg-dash-surface-container-high cursor-pointer"
          >
            <span className="material-symbols-outlined">notifications</span>
          </button>
          <button
            aria-label="History"
            className="p-2 text-dash-on-surface-variant hover:text-dash-primary transition-colors rounded-full hover:bg-dash-surface-container-high cursor-pointer"
          >
            <span className="material-symbols-outlined">history_toggle_off</span>
          </button>
        </div>

        {/* Profile */}
        <img
          alt="Agent Profile"
          className="w-8 h-8 rounded-full border-2 border-dash-surface object-cover"
          src="https://lh3.googleusercontent.com/aida-public/AB6AXuAfDVs5RYEc28HIPOh5JhP2CanZUvdOkWfb70VofsMFjXgsTW4Cz1eeaEI_AQfh16Wxy2gloQqFOUsU9dWTJoaiEhkRRs_h1LCZEW4M6z07vpwa8mHbmLL6kdbwNAdNh2BZwoDxstm5Ftn6mRMpnDH0iZZdQ0lVS6dmjAjEVUqIBqZljDVYsn0FwZM0ZFEkjfLUnSk2lNPFwNjm2OyPUtWNTfeTN9lUK_PS3t4r6okgN2XuscjNdv0N"
        />
      </div>
    </header>
  );
}

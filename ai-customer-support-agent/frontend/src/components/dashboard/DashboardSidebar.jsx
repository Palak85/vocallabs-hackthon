const navItems = [
  { icon: "forum", label: "Queues", active: true, filled: true },
  { icon: "insights", label: "Insights" },
  { icon: "robot_2", label: "Automations" },
  { icon: "library_books", label: "Library" },
  { icon: "terminal", label: "Logs" },
];

const bottomItems = [
  { icon: "help", label: "Help" },
  { icon: "check_circle", label: "Status" },
];

export default function DashboardSidebar() {
  return (
    <aside className="hidden lg:flex bg-dash-surface-container-low border-r border-dash-outline-variant h-full w-64 flex-col p-4 gap-2 shrink-0">
      {/* Node Info */}
      <div className="flex items-center gap-3 mb-6 p-2 bg-dash-surface-container-highest rounded-xl">
        <div className="w-10 h-10 rounded-full bg-dash-primary flex items-center justify-center text-dash-on-primary relative">
          <span
            className="material-symbols-outlined"
            style={{ fontVariationSettings: "'FILL' 1" }}
          >
            router
          </span>
          <span className="absolute -bottom-1 -right-1 w-3.5 h-3.5 bg-green-500 border-2 border-dash-surface-container-highest rounded-full" />
        </div>
        <div>
          <h2 className="text-label-md font-bold text-dash-on-surface">Node Alpha</h2>
          <p className="text-label-xs text-dash-on-surface-variant">98.4% Efficiency</p>
        </div>
      </div>

      {/* Main Navigation */}
      <nav className="flex-1 flex flex-col gap-1">
        {navItems.map((item) => (
          <a
            key={item.label}
            href="#"
            className={`flex items-center gap-3 px-3 py-2 rounded-lg text-label-md font-semibold transition-colors active:scale-95 ${
              item.active
                ? "bg-dash-secondary-container text-dash-on-secondary-container font-bold"
                : "text-dash-on-surface-variant hover:bg-dash-surface-variant"
            }`}
          >
            <span
              className="material-symbols-outlined text-lg"
              style={item.filled ? { fontVariationSettings: "'FILL' 1" } : {}}
            >
              {item.icon}
            </span>
            {item.label}
          </a>
        ))}
      </nav>

      {/* Escalate Button */}
      <button className="w-full bg-dash-error text-dash-on-error py-2 rounded-lg text-label-md font-semibold hover:opacity-90 transition-opacity mb-4 flex items-center justify-center gap-2 cursor-pointer">
        <span className="material-symbols-outlined text-sm">warning</span>
        Escalate Session
      </button>

      {/* Bottom Links */}
      <div className="mt-auto flex flex-col gap-1 border-t border-dash-outline-variant pt-4">
        {bottomItems.map((item) => (
          <a
            key={item.label}
            href="#"
            className="text-dash-on-surface-variant hover:bg-dash-surface-variant rounded-lg transition-colors flex items-center gap-3 px-3 py-2 text-label-md font-semibold"
          >
            <span className="material-symbols-outlined text-lg">{item.icon}</span>
            {item.label}
          </a>
        ))}
      </div>
    </aside>
  );
}

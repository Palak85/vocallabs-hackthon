export default function KPIStrip() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 p-4 shrink-0">
      {/* Active Sessions */}
      <div className="bg-dash-surface-container-lowest rounded-xl p-4 shadow-sm border border-dash-surface-variant flex flex-col gap-2">
        <div className="flex justify-between items-center text-dash-on-surface-variant">
          <span className="text-body-sm">Active Sessions</span>
          <span className="material-symbols-outlined text-lg">group</span>
        </div>
        <div className="text-2xl font-bold text-dash-on-surface">124</div>
        <div className="text-label-xs text-dash-primary">+12% from last hour</div>
      </div>

      {/* AI Auto-Resolved */}
      <div className="bg-dash-surface-container-lowest rounded-xl p-4 shadow-sm border border-dash-surface-variant flex flex-col gap-2">
        <div className="flex justify-between items-center text-dash-on-surface-variant">
          <span className="text-body-sm">AI Auto-Resolved</span>
          <span className="material-symbols-outlined text-lg">smart_toy</span>
        </div>
        <div className="text-2xl font-bold text-dash-on-surface">78%</div>
        <div className="w-full bg-dash-surface-variant rounded-full h-1.5 mt-1">
          <div className="bg-dash-primary h-1.5 rounded-full" style={{ width: "78%" }} />
        </div>
      </div>

      {/* Avg Frustration */}
      <div className="bg-dash-surface-container-lowest rounded-xl p-4 shadow-sm border border-dash-surface-variant flex flex-col gap-2">
        <div className="flex justify-between items-center text-dash-on-surface-variant">
          <span className="text-body-sm">Avg Frustration</span>
          <span className="material-symbols-outlined text-lg text-dash-tertiary">mood_bad</span>
        </div>
        <div className="text-2xl font-bold text-dash-on-surface">34%</div>
        {/* Mini Sparkline */}
        <div className="h-4 flex items-end gap-1 mt-1 opacity-70">
          <div className="w-full bg-dash-tertiary-container h-1/2 rounded-t-sm" />
          <div className="w-full bg-dash-tertiary-container h-3/4 rounded-t-sm" />
          <div className="w-full bg-dash-tertiary-container h-full rounded-t-sm" />
          <div className="w-full bg-dash-tertiary-container h-2/3 rounded-t-sm" />
          <div className="w-full bg-dash-tertiary-container h-1/3 rounded-t-sm" />
          <div className="w-full bg-dash-tertiary h-3/4 rounded-t-sm" />
        </div>
      </div>

      {/* Escalations Pending */}
      <div className="bg-dash-error-container rounded-xl p-4 shadow-sm border border-dash-error/20 flex flex-col gap-2 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-16 h-16 bg-dash-error/10 rounded-bl-full -mr-4 -mt-4" />
        <div className="flex justify-between items-center text-dash-on-error-container">
          <span className="text-body-sm font-medium">Escalations Pending</span>
          <span className="material-symbols-outlined text-lg animate-pulse">
            notification_important
          </span>
        </div>
        <div className="text-2xl font-bold text-dash-on-error-container">8</div>
        <div className="text-label-xs text-dash-error">Requires immediate attention</div>
      </div>
    </div>
  );
}

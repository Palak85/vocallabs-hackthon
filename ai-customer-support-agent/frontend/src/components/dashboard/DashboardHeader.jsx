import { useNavigate } from "react-router-dom";

export default function DashboardHeader() {
  const navigate = useNavigate();

  return (
    <header className="bg-white border-b border-slate-200 shadow-xs flex justify-between items-center px-6 w-full h-16 sticky top-0 z-50 shrink-0">
      <div className="flex items-center gap-6">
        {/* Brand */}
        <div
          onClick={() => navigate("/")}
          className="flex items-center gap-2 cursor-pointer hover:opacity-90 transition-opacity"
        >
          <div className="w-8 h-8 rounded-lg bg-[#006a6a] flex items-center justify-center text-white">
            <span className="material-symbols-outlined text-[20px]">admin_panel_settings</span>
          </div>
          <div>
            <span className="text-sm font-bold text-[#006a6a] block leading-tight">
              SUPERVISOR & MONITORING CONSOLE
            </span>
            <span className="text-[10px] text-slate-500 block">
              Human Escalation & Ingestion Hub
            </span>
          </div>
        </div>

        {/* Tenant Selector */}
        <div className="hidden sm:flex items-center gap-2 px-3 py-1 bg-slate-100 rounded-lg border border-slate-200 text-xs">
          <span className="text-slate-500 font-medium">Tenant:</span>
          <span className="font-bold text-slate-800 flex items-center gap-1">
            default
            <span className="material-symbols-outlined text-sm">arrow_drop_down</span>
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        {/* Switch to Customer Portal Button */}
        <button
          onClick={() => navigate("/customer")}
          className="bg-emerald-50 text-emerald-800 border border-emerald-300 px-3 py-1.5 rounded-lg text-xs font-semibold hover:bg-emerald-100 transition-all flex items-center gap-1.5 cursor-pointer shadow-2xs"
        >
          <span className="material-symbols-outlined text-sm">support_agent</span>
          Open Customer Portal
        </button>

        {/* Live Status Badge */}
        <div className="flex items-center px-3 py-1.5 bg-emerald-100 text-emerald-800 rounded-full gap-2 text-xs font-bold border border-emerald-300">
          <span className="w-2 h-2 rounded-full bg-emerald-600 animate-pulse" />
          <span>Real-time Stream Active</span>
        </div>
      </div>
    </header>
  );
}

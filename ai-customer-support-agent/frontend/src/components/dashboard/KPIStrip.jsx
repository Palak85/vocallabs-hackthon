import { useState, useEffect } from "react";
import { api } from "../../services/api";

export default function KPIStrip() {
  const [stats, setStats] = useState({
    totalConversations: 0,
    activeAiConversations: 0,
    activeHumanConversations: 0,
    escalationRecommendedCount: 0,
    escalatedCount: 0,
    averageFrustrationScore: 0,
  });

  const fetchStats = async () => {
    try {
      const data = await api.getMonitoringStats();
      if (data) setStats(data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 4000);
    return () => clearInterval(interval);
  }, []);

  const totalActive = stats.activeAiConversations + stats.activeHumanConversations;
  const totalEscalations = stats.escalationRecommendedCount + stats.escalatedCount;
  const avgFrustration = Math.round(stats.averageFrustrationScore || 0);

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 p-4 shrink-0">
      {/* Active Sessions */}
      <div className="bg-white rounded-xl p-4 shadow-xs border border-slate-200 flex flex-col gap-1.5">
        <div className="flex justify-between items-center text-slate-500">
          <span className="text-xs font-semibold uppercase tracking-wider">Active Sessions</span>
          <span className="material-symbols-outlined text-lg text-[#006a6a]">group</span>
        </div>
        <div className="text-2xl font-bold text-slate-900">{totalActive || stats.totalConversations}</div>
        <div className="text-[11px] text-emerald-600 font-medium flex items-center gap-1">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
          {stats.activeAiConversations} AI • {stats.activeHumanConversations} Human Active
        </div>
      </div>

      {/* AI Handled Rate */}
      <div className="bg-white rounded-xl p-4 shadow-xs border border-slate-200 flex flex-col gap-1.5">
        <div className="flex justify-between items-center text-slate-500">
          <span className="text-xs font-semibold uppercase tracking-wider">AI Handling Rate</span>
          <span className="material-symbols-outlined text-lg text-cyan-600">smart_toy</span>
        </div>
        <div className="text-2xl font-bold text-slate-900">
          {totalActive > 0 ? Math.round((stats.activeAiConversations / totalActive) * 100) : 100}%
        </div>
        <div className="w-full bg-slate-100 rounded-full h-1.5 mt-0.5">
          <div
            className="bg-[#006a6a] h-1.5 rounded-full transition-all duration-500"
            style={{
              width: `${totalActive > 0 ? (stats.activeAiConversations / totalActive) * 100 : 100}%`,
            }}
          />
        </div>
      </div>

      {/* Avg Frustration */}
      <div className="bg-white rounded-xl p-4 shadow-xs border border-slate-200 flex flex-col gap-1.5">
        <div className="flex justify-between items-center text-slate-500">
          <span className="text-xs font-semibold uppercase tracking-wider">Avg Frustration Score</span>
          <span className="material-symbols-outlined text-lg text-amber-600">mood_bad</span>
        </div>
        <div className="text-2xl font-bold text-slate-900">{avgFrustration}%</div>
        <div className="w-full bg-slate-100 rounded-full h-1.5 mt-0.5">
          <div
            className={`h-1.5 rounded-full transition-all duration-500 ${
              avgFrustration >= 70 ? "bg-red-500" : avgFrustration >= 40 ? "bg-amber-500" : "bg-emerald-500"
            }`}
            style={{ width: `${Math.max(5, avgFrustration)}%` }}
          />
        </div>
      </div>

      {/* Escalations Pending */}
      <div className="bg-red-50/80 rounded-xl p-4 shadow-xs border border-red-200 flex flex-col gap-1.5 relative overflow-hidden">
        <div className="flex justify-between items-center text-red-900">
          <span className="text-xs font-bold uppercase tracking-wider">Escalation Alerts</span>
          <span className="material-symbols-outlined text-lg text-red-600 animate-pulse">
            notification_important
          </span>
        </div>
        <div className="text-2xl font-bold text-red-950">{totalEscalations}</div>
        <div className="text-[11px] text-red-700 font-medium">
          {totalEscalations > 0 ? "Requires Supervisor Attention" : "All Sessions Operating Normally"}
        </div>
      </div>
    </div>
  );
}

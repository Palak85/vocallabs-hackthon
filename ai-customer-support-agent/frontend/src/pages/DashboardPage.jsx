import DashboardHeader from "../components/dashboard/DashboardHeader";
import KPIStrip from "../components/dashboard/KPIStrip";
import KnowledgeBasePanel from "../components/dashboard/KnowledgeBasePanel";
import LiveMonitorConsole from "../components/dashboard/LiveMonitorConsole";

export default function DashboardPage() {
  return (
    <div className="bg-[#f4f7f6] text-slate-900 min-h-screen flex flex-col antialiased select-none font-sans">
      <DashboardHeader />

      <div className="flex-1 flex flex-col overflow-hidden h-[calc(100vh-4rem)]">
        {/* Top Real-time Metrics KPI Strip */}
        <KPIStrip />

        {/* Dual Dashboard Workspace: Knowledge Base Ingestion + Live Monitor Takeover Console */}
        <main className="flex-1 flex flex-col lg:flex-row overflow-hidden p-4 pt-0 gap-4">
          {/* Column 1: Ingestion & Knowledge Base Management */}
          <div className="w-full lg:w-5/12 flex flex-col h-full overflow-hidden">
            <KnowledgeBasePanel />
          </div>

          {/* Column 2: Live Call Monitor & Human Takeover Console */}
          <div className="w-full lg:w-7/12 flex flex-col h-full overflow-hidden">
            <LiveMonitorConsole />
          </div>
        </main>
      </div>
    </div>
  );
}

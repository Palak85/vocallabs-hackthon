import DashboardHeader from "../components/dashboard/DashboardHeader";
import DashboardSidebar from "../components/dashboard/DashboardSidebar";
import KPIStrip from "../components/dashboard/KPIStrip";
import ActiveChat from "../components/dashboard/ActiveChat";
import TelemetryPanel from "../components/dashboard/TelemetryPanel";

export default function DashboardPage() {
  return (
    <div className="bg-dash-background text-dash-on-background min-h-screen flex flex-col antialiased">
      <DashboardHeader />

      <div className="flex flex-1 overflow-hidden h-[calc(100vh-4rem)]">
        <DashboardSidebar />

        {/* Main Workspace */}
        <main className="flex-1 flex flex-col overflow-hidden bg-dash-background">
          <KPIStrip />

          {/* Three Column Layout */}
          <div className="flex-1 flex overflow-hidden p-4 pt-0 gap-4">
            <ActiveChat />
            <TelemetryPanel />
          </div>
        </main>
      </div>
    </div>
  );
}

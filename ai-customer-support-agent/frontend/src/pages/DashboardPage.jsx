import { useState } from "react";
import DashboardHeader from "../components/dashboard/DashboardHeader";
import DashboardSidebar from "../components/dashboard/DashboardSidebar";
import KPIStrip from "../components/dashboard/KPIStrip";
import ActiveChat from "../components/dashboard/ActiveChat";
import TelemetryPanel from "../components/dashboard/TelemetryPanel";

export default function DashboardPage() {
  const [telemetry, setTelemetry] = useState({
    domain: "Ecommerce",
    intent: "Order Tracking",
    sentiment: "Neutral",
    emotion: "Neutral",
    frustration_score: 25,
    frustration_level: "low",
    urgency: "low",
    frustration_trend: "stable",
    confidence: 0.95,
    entities: []
  });

  const [isEscalated, setIsEscalated] = useState(false);
  const [logs, setLogs] = useState([
    { time: new Date().toLocaleTimeString(), text: "System Initialized: Multi-Domain AI Gateway Online (:8081)", color: "text-dash-primary-fixed-dim" },
    { time: new Date().toLocaleTimeString(), text: "NLP Inference Engine Connected (:8000)", color: "text-dash-secondary-fixed-dim" },
    { time: new Date().toLocaleTimeString(), text: "Continuous Monitor & Decision Engine Ready", color: "text-dash-primary-fixed-dim" }
  ]);

  const handleTurnComplete = (response) => {
    const timeStr = new Date().toLocaleTimeString();
    if (response) {
      if (response.escalated || response.status === "ESCALATED") {
        setIsEscalated(true);
        setLogs(prev => [
          ...prev,
          { time: timeStr, text: `MONITOR: High risk detected. HUMAN ESCALATION triggered.`, color: "text-dash-error-container text-red-400" }
        ]);
      } else {
        setIsEscalated(false);
      }

      if (response.nlp) {
        const n = response.nlp;
        const confidence = n.domainConfidence || n.intentConfidence || 0.92;
        setTelemetry({
          domain: n.domain ? n.domain.charAt(0).toUpperCase() + n.domain.slice(1) : "General",
          intent: n.intent ? n.intent.replace(/_/g, " ").replace(/\b\w/g, l => l.toUpperCase()) : "General Query",
          sentiment: n.sentiment ? n.sentiment.charAt(0).toUpperCase() + n.sentiment.slice(1) : "Neutral",
          emotion: n.emotion ? n.emotion.charAt(0).toUpperCase() + n.emotion.slice(1) : "Neutral",
          frustration_score: n.frustration_score != null ? n.frustration_score : (n.frustrationScore != null ? n.frustrationScore : 20),
          frustration_level: n.frustration_level || n.frustrationLevel || "low",
          urgency: n.urgency || "low",
          frustration_trend: n.frustration_trend || n.frustrationTrend || "stable",
          confidence: confidence,
          entities: n.entities || []
        });

        setLogs(prev => [
          ...prev,
          { time: timeStr, text: `NLP: Domain='${n.domain}' | Intent='${n.intent}' | Frustration=${n.frustration_score || n.frustrationScore || 0}`, color: "text-dash-secondary-fixed-dim" },
          { time: timeStr, text: `DECISION: Status='${response.status || "RESOLVED"}' | Escalated=${response.escalated}`, color: response.escalated ? "text-red-400" : "text-emerald-400" }
        ]);
      }
    }
  };

  return (
    <div className="bg-dash-background text-dash-on-background min-h-screen flex flex-col antialiased">
      <DashboardHeader />

      <div className="flex flex-1 overflow-hidden h-[calc(100vh-4rem)]">
        <DashboardSidebar />

        {/* Main Workspace */}
        <main className="flex-1 flex flex-col overflow-hidden bg-dash-background">
          <KPIStrip isEscalated={isEscalated} telemetry={telemetry} />

          {/* Three Column Layout */}
          <div className="flex-1 flex overflow-hidden p-4 pt-0 gap-4">
            <ActiveChat isEscalated={isEscalated} onTurnComplete={handleTurnComplete} />
            <TelemetryPanel telemetry={telemetry} isEscalated={isEscalated} logs={logs} />
          </div>
        </main>
      </div>
    </div>
  );
}

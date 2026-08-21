const logEntries = [
  { time: "10:42:01", text: "INIT Session #8922", color: "text-dash-secondary-fixed-dim" },
  { time: "10:42:05", text: "NLU: Intent='refund_request'", color: "text-dash-secondary-fixed-dim" },
  { time: "10:42:05", text: "WARN: Sentiment score dropped to -0.6", color: "text-dash-tertiary-fixed-dim" },
  { time: "10:42:12", text: "API_CALL: Stripe.getCharges(cus_98421)", color: "text-dash-secondary-fixed-dim" },
  { time: "10:42:14", text: "API_RES: 200 OK (2 records found)", color: "text-dash-primary-fixed-dim" },
  { time: "10:42:15", text: "LOGIC: Duplicate charge detected.", color: "text-dash-secondary-fixed-dim" },
  { time: "10:43:02", text: "GEN_RESPONSE: Confidence 0.94", color: "text-dash-secondary-fixed-dim" },
];

export default function TelemetryPanel() {
  return (
    <aside className="hidden xl:flex flex-col w-96 bg-dash-surface-container-lowest rounded-xl shadow-sm border border-dash-surface-variant overflow-hidden shrink-0">
      {/* Panel Header */}
      <div className="p-4 border-b border-dash-outline-variant bg-dash-surface-bright">
        <h3 className="text-label-md font-bold text-dash-on-surface flex items-center gap-2">
          <span className="material-symbols-outlined text-lg">memory</span>
          Real-Time Telemetry
        </h3>
      </div>

      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-6">
        {/* Meters */}
        <div className="flex flex-col gap-4">
          {/* AI Confidence */}
          <div>
            <div className="flex justify-between items-end mb-1">
              <span className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider">
                AI Confidence
              </span>
              <span className="text-label-md font-bold text-dash-primary">94%</span>
            </div>
            <div className="w-full bg-dash-surface-variant rounded-full h-2">
              <div
                className="bg-dash-primary h-2 rounded-full transition-all duration-500"
                style={{ width: "94%" }}
              />
            </div>
          </div>

          {/* Frustration Level */}
          <div>
            <div className="flex justify-between items-end mb-1">
              <span className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider">
                Frustration Level
              </span>
              <span className="text-label-md font-bold text-dash-tertiary">Medium</span>
            </div>
            <div className="w-full flex h-2 rounded-full overflow-hidden gap-0.5">
              <div className="bg-dash-surface-variant h-full flex-1" />
              <div className="bg-dash-surface-variant h-full flex-1" />
              <div className="bg-dash-tertiary-container h-full flex-1" />
              <div className="bg-dash-surface-variant h-full flex-1" />
              <div className="bg-dash-surface-variant h-full flex-1" />
            </div>
          </div>
        </div>

        <hr className="border-dash-outline-variant" />

        {/* Auto-Generated Dossier */}
        <div>
          <h4 className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider mb-3">
            Auto-Generated Dossier
          </h4>
          <div className="bg-dash-surface-container p-3 rounded-lg border border-dash-outline-variant flex flex-col gap-2">
            <div>
              <span className="text-label-xs text-dash-on-surface-variant block mb-0.5">
                Predicted Root Cause:
              </span>
              <span className="text-body-sm font-medium text-dash-on-surface">
                Payment Gateway Bug (Issue #442)
              </span>
            </div>
            <div>
              <span className="text-label-xs text-dash-on-surface-variant block mb-0.5">
                AI Action Path:
              </span>
              <span className="text-body-sm text-dash-on-surface flex items-center gap-1">
                Identify{" "}
                <span className="material-symbols-outlined text-[10px]">arrow_forward</span>{" "}
                Validate{" "}
                <span className="material-symbols-outlined text-[10px]">arrow_forward</span>{" "}
                Refund Auth
              </span>
            </div>
          </div>
        </div>

        {/* Execution Logs */}
        <div className="flex-1 flex flex-col">
          <h4 className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider mb-2">
            Execution Logs
          </h4>
          <div className="flex-1 bg-dash-inverse-surface text-dash-inverse-on-surface p-3 rounded-lg text-[11px] font-mono leading-relaxed overflow-y-auto border border-dash-surface-variant opacity-90 min-h-[192px]">
            {logEntries.map((entry, i) => (
              <div key={i}>
                <span className={entry.color}>[{entry.time}]</span> {entry.text}
              </div>
            ))}
          </div>
        </div>
      </div>
    </aside>
  );
}

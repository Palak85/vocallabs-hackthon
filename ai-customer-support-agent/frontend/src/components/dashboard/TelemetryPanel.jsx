export default function TelemetryPanel({ telemetry, isEscalated, logs = [] }) {
  const t = telemetry || {
    domain: "Ecommerce",
    intent: "Order Tracking",
    sentiment: "Neutral",
    emotion: "Neutral",
    frustration_score: 20,
    frustration_level: "low",
    urgency: "low",
    frustration_trend: "stable",
    confidence: 0.95,
    entities: []
  };

  const frustrationScore = t.frustration_score != null ? t.frustration_score : 20;
  const frustrationLevel = t.frustration_level || "low";
  const confidencePercent = Math.round((t.confidence || 0.92) * 100);

  return (
    <aside className="hidden xl:flex flex-col w-96 bg-dash-surface-container-lowest rounded-xl shadow-sm border border-dash-surface-variant overflow-hidden shrink-0">
      {/* Panel Header */}
      <div className="p-4 border-b border-dash-outline-variant bg-dash-surface-bright flex justify-between items-center">
        <h3 className="text-label-md font-bold text-dash-on-surface flex items-center gap-2">
          <span className="material-symbols-outlined text-lg">memory</span>
          Real-Time Telemetry
        </h3>
        {isEscalated && (
          <span className="px-2 py-0.5 text-[10px] uppercase font-bold bg-red-500/20 text-red-400 rounded-full border border-red-500/30 animate-pulse">
            Escalated
          </span>
        )}
      </div>

      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-5">
        {/* NLP Signal Badges */}
        <div className="grid grid-cols-2 gap-2">
          <div className="bg-dash-surface-container p-2.5 rounded-lg border border-dash-outline-variant">
            <span className="text-[10px] text-dash-on-surface-variant uppercase tracking-wider block">Domain</span>
            <span className="text-body-sm font-semibold text-dash-primary">{t.domain}</span>
          </div>
          <div className="bg-dash-surface-container p-2.5 rounded-lg border border-dash-outline-variant">
            <span className="text-[10px] text-dash-on-surface-variant uppercase tracking-wider block">Intent</span>
            <span className="text-body-sm font-semibold text-dash-on-surface truncate block" title={t.intent}>{t.intent}</span>
          </div>
          <div className="bg-dash-surface-container p-2.5 rounded-lg border border-dash-outline-variant">
            <span className="text-[10px] text-dash-on-surface-variant uppercase tracking-wider block">Sentiment</span>
            <span className={`text-body-sm font-semibold ${t.sentiment === "Negative" ? "text-red-400" : t.sentiment === "Positive" ? "text-emerald-400" : "text-dash-on-surface"}`}>{t.sentiment}</span>
          </div>
          <div className="bg-dash-surface-container p-2.5 rounded-lg border border-dash-outline-variant">
            <span className="text-[10px] text-dash-on-surface-variant uppercase tracking-wider block">Emotion</span>
            <span className="text-body-sm font-semibold text-amber-400">{t.emotion}</span>
          </div>
        </div>

        {/* Meters */}
        <div className="flex flex-col gap-3">
          {/* AI Confidence */}
          <div>
            <div className="flex justify-between items-end mb-1">
              <span className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider">
                NLP Confidence
              </span>
              <span className="text-label-md font-bold text-dash-primary">{confidencePercent}%</span>
            </div>
            <div className="w-full bg-dash-surface-variant rounded-full h-2">
              <div
                className="bg-dash-primary h-2 rounded-full transition-all duration-500"
                style={{ width: `${confidencePercent}%` }}
              />
            </div>
          </div>

          {/* Frustration Score */}
          <div>
            <div className="flex justify-between items-end mb-1">
              <span className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider">
                Frustration Score
              </span>
              <span className={`text-label-md font-bold ${frustrationScore >= 75 ? "text-red-400" : frustrationScore >= 40 ? "text-amber-400" : "text-emerald-400"}`}>
                {frustrationScore} / 100 ({frustrationLevel})
              </span>
            </div>
            <div className="w-full bg-dash-surface-variant rounded-full h-2 overflow-hidden">
              <div
                className={`h-2 rounded-full transition-all duration-500 ${frustrationScore >= 75 ? "bg-red-500" : frustrationScore >= 40 ? "bg-amber-500" : "bg-emerald-500"}`}
                style={{ width: `${Math.min(100, frustrationScore)}%` }}
              />
            </div>
          </div>

          {/* Urgency & Trend */}
          <div className="flex justify-between text-label-xs text-dash-on-surface-variant pt-1">
            <span>Urgency: <strong className="text-dash-on-surface uppercase">{t.urgency}</strong></span>
            <span>Trend: <strong className="text-dash-on-surface uppercase">{t.frustration_trend}</strong></span>
          </div>
        </div>

        {/* Entities Extracted */}
        {t.entities && t.entities.length > 0 && (
          <div>
            <h4 className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider mb-2">
              Extracted Entities (NER)
            </h4>
            <div className="flex flex-wrap gap-1.5">
              {t.entities.map((ent, idx) => (
                <span key={idx} className="px-2 py-1 bg-dash-surface-container text-[11px] rounded border border-dash-outline-variant font-mono text-cyan-300">
                  {ent.type}: {ent.value}
                </span>
              ))}
            </div>
          </div>
        )}

        <hr className="border-dash-outline-variant" />

        {/* Execution Logs */}
        <div className="flex-1 flex flex-col min-h-[160px]">
          <h4 className="text-label-xs text-dash-on-surface-variant uppercase tracking-wider mb-2">
            Execution Logs & Decision Signals
          </h4>
          <div className="flex-1 bg-dash-inverse-surface text-dash-inverse-on-surface p-3 rounded-lg text-[11px] font-mono leading-relaxed overflow-y-auto border border-dash-surface-variant opacity-90 max-h-[220px]">
            {logs.map((entry, i) => (
              <div key={i} className="mb-1">
                <span className={entry.color}>[{entry.time}]</span> {entry.text}
              </div>
            ))}
          </div>
        </div>
      </div>
    </aside>
  );
}

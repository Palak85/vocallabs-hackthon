import { useState, useEffect, useRef } from "react";
import { api } from "../../services/api";

export default function LiveMonitorConsole() {
  const [conversations, setConversations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [sessionDetail, setSessionDetail] = useState(null);
  const [agentReply, setAgentReply] = useState("");
  const [loading, setLoading] = useState(false);
  const [takingOver, setTakingOver] = useState(false);
  const [sendingMsg, setSendingMsg] = useState(false);

  const messagesEndRef = useRef(null);

  // Poll monitored conversations every 3 seconds
  const fetchMonitoredConversations = async () => {
    try {
      const list = await api.listMonitoredConversations();
      const validList = Array.isArray(list) ? list : [];
      setConversations(validList);

      if (validList.length > 0 && !selectedId) {
        setSelectedId(validList[0].id);
      }
    } catch (err) {
      console.error("Failed to load monitored conversations:", err);
    }
  };

  const fetchSessionDetail = async (id) => {
    if (!id) return;
    try {
      const detail = await api.getMonitoringDetail(id);
      setSessionDetail(detail);
    } catch (err) {
      console.error("Failed to load session detail:", err);
    }
  };

  useEffect(() => {
    fetchMonitoredConversations();
    const interval = setInterval(fetchMonitoredConversations, 3000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (selectedId) {
      fetchSessionDetail(selectedId);
      const detailInterval = setInterval(() => fetchSessionDetail(selectedId), 2500);
      return () => clearInterval(detailInterval);
    }
  }, [selectedId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [sessionDetail?.messages]);

  const handleTakeover = async () => {
    if (!selectedId) return;
    try {
      setTakingOver(true);
      await api.takeoverConversation(selectedId, "Support Specialist");
      await fetchSessionDetail(selectedId);
      await fetchMonitoredConversations();
    } catch (err) {
      alert("Takeover failed: " + err.message);
    } finally {
      setTakingOver(false);
    }
  };

  const handleHandback = async () => {
    if (!selectedId) return;
    try {
      setTakingOver(true);
      await api.handbackConversation(selectedId);
      await fetchSessionDetail(selectedId);
      await fetchMonitoredConversations();
    } catch (err) {
      alert("Handback failed: " + err.message);
    } finally {
      setTakingOver(false);
    }
  };

  const handleSendSupervisorMessage = async (e) => {
    e?.preventDefault();
    if (!agentReply.trim() || !selectedId) return;

    try {
      setSendingMsg(true);
      await api.sendAgentMessage(selectedId, agentReply.trim(), "Support Specialist");
      setAgentReply("");
      await fetchSessionDetail(selectedId);
    } catch (err) {
      alert("Failed to send message: " + err.message);
    } finally {
      setSendingMsg(false);
    }
  };

  const frustration = sessionDetail?.frustrationScore ?? 0;
  const isEscalated =
    sessionDetail?.escalationStatus === "ESCALATED" ||
    sessionDetail?.escalationStatus === "RECOMMENDED" ||
    frustration >= 70;
  const isHumanMode = sessionDetail?.mode === "HUMAN";

  return (
    <div className="flex-1 flex flex-col bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
      {/* Console Top Header */}
      <div className="p-4 border-b border-slate-200 bg-slate-50/80 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-emerald-600/10 text-emerald-700 flex items-center justify-center">
            <span className="material-symbols-outlined text-lg">monitoring</span>
          </div>
          <div>
            <h2 className="font-bold text-sm text-slate-800">Live Call Monitor & Human Takeover</h2>
            <p className="text-[11px] text-slate-500">Real-time Emotion, Frustration & Intervention</p>
          </div>
        </div>

        {/* Mode Indicator & Action Button */}
        <div className="flex items-center gap-2">
          {sessionDetail && (
            <>
              <span
                className={`px-2.5 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 ${
                  isHumanMode
                    ? "bg-amber-100 text-amber-900 border border-amber-300"
                    : "bg-emerald-100 text-emerald-800 border border-emerald-300"
                }`}
              >
                <span className={`w-2 h-2 rounded-full ${isHumanMode ? "bg-amber-500" : "bg-emerald-500 animate-pulse"}`} />
                {isHumanMode ? "👤 Human Specialist Active" : "🤖 AI Handled"}
              </span>

              {isHumanMode ? (
                <button
                  onClick={handleHandback}
                  disabled={takingOver}
                  className="px-3 py-1 bg-slate-800 text-white rounded-lg text-xs font-semibold hover:bg-slate-900 flex items-center gap-1 transition-all cursor-pointer"
                >
                  <span className="material-symbols-outlined text-sm">autorenew</span>
                  Hand Back to AI
                </button>
              ) : (
                <button
                  onClick={handleTakeover}
                  disabled={takingOver}
                  className={`px-3 py-1 rounded-lg text-xs font-bold flex items-center gap-1 transition-all cursor-pointer shadow-xs ${
                    isEscalated
                      ? "bg-red-600 hover:bg-red-700 text-white animate-pulse"
                      : "bg-[#006a6a] hover:bg-[#005555] text-white"
                  }`}
                >
                  <span className="material-symbols-outlined text-sm">front_hand</span>
                  Takeover Call
                </button>
              )}
            </>
          )}
        </div>
      </div>

      <div className="flex-1 flex overflow-hidden">
        {/* Left Side: Session Selector List */}
        <div className="w-72 border-r border-slate-200 bg-slate-50/50 flex flex-col overflow-hidden shrink-0">
          <div className="p-3 border-b border-slate-200 text-xs font-bold text-slate-700 flex justify-between items-center">
            <span>Active Sessions ({conversations.length})</span>
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          </div>

          <div className="flex-1 overflow-y-auto p-2 flex flex-col gap-1.5">
            {conversations.length === 0 ? (
              <div className="p-4 text-center text-xs text-slate-400">
                No active conversations yet. Start a chat in the Customer portal!
              </div>
            ) : (
              conversations.map((conv) => {
                const isSelected = selectedId === conv.id;
                const fScore = conv.frustrationScore ?? 0;
                const isHighFrustration = fScore >= 70;

                return (
                  <button
                    key={conv.id}
                    onClick={() => setSelectedId(conv.id)}
                    className={`p-3 rounded-xl text-left transition-all border cursor-pointer ${
                      isSelected
                        ? "bg-white border-[#006a6a] shadow-sm ring-1 ring-[#006a6a]/20"
                        : "bg-white/80 border-slate-200 hover:bg-white hover:border-slate-300"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-bold text-xs text-slate-800 truncate max-w-[130px]">
                        {conv.title || "Support Call"}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${
                          conv.mode === "HUMAN"
                            ? "bg-amber-100 text-amber-800"
                            : "bg-slate-100 text-slate-700"
                        }`}
                      >
                        {conv.mode || "AI"}
                      </span>
                    </div>

                    <p className="text-[11px] text-slate-500 truncate mb-2">
                      {conv.lastMessageSnippet || "Active call session..."}
                    </p>

                    {/* Metrics preview row */}
                    <div className="flex items-center justify-between text-[10px]">
                      <span
                        className={`font-semibold flex items-center gap-1 ${
                          isHighFrustration
                            ? "text-red-600 font-bold"
                            : fScore >= 40
                            ? "text-amber-600"
                            : "text-emerald-600"
                        }`}
                      >
                        <span className="material-symbols-outlined text-[12px]">
                          {isHighFrustration ? "local_fire_department" : "sentiment_satisfied"}
                        </span>
                        Frustration: {fScore}%
                      </span>

                      {conv.intent && (
                        <span className="bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded-md truncate max-w-[80px]">
                          {conv.intent}
                        </span>
                      )}
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Right Side: Live Session Detail & Takeover Stream */}
        {sessionDetail ? (
          <div className="flex-1 flex flex-col overflow-hidden bg-slate-50/30">
            {/* AI ESCALATION ALERT BANNER */}
            {isEscalated && (
              <div className="bg-red-50 border-b border-red-200 p-3 flex items-center justify-between text-red-900 shrink-0 animate-fadeIn">
                <div className="flex items-center gap-2 text-xs font-semibold">
                  <span className="material-symbols-outlined text-red-600 text-lg animate-bounce">
                    warning
                  </span>
                  <span>
                    <strong>⚠️ AI AGENT ESCALATION ALERT:</strong> Customer is getting frustrated (Score:{" "}
                    {frustration}/100, Intent: {sessionDetail.intent || "complaint"}). Recommend switching to live chat.
                  </span>
                </div>
                {!isHumanMode && (
                  <button
                    onClick={handleTakeover}
                    className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-bold cursor-pointer transition-all shrink-0"
                  >
                    Takeover Now
                  </button>
                )}
              </div>
            )}

            {/* Metrics Bar */}
            <div className="p-3 bg-white border-b border-slate-200 grid grid-cols-4 gap-3 text-xs shrink-0">
              {/* Frustration Meter */}
              <div>
                <div className="flex justify-between text-[11px] font-semibold text-slate-600 mb-1">
                  <span>Frustration Meter</span>
                  <span
                    className={
                      frustration >= 70
                        ? "text-red-600 font-bold"
                        : frustration >= 40
                        ? "text-amber-600"
                        : "text-emerald-600"
                    }
                  >
                    {frustration}/100 ({sessionDetail.frustrationLevel || "Low"})
                  </span>
                </div>
                <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-full transition-all duration-500 ${
                      frustration >= 70
                        ? "bg-red-500"
                        : frustration >= 40
                        ? "bg-amber-500"
                        : "bg-emerald-500"
                    }`}
                    style={{ width: `${Math.min(100, Math.max(5, frustration))}%` }}
                  />
                </div>
              </div>

              {/* Sentiment */}
              <div className="border-l border-slate-200 pl-3">
                <span className="text-[10px] text-slate-400 block font-semibold">Sentiment & Emotion</span>
                <span className="font-bold text-slate-800 capitalize">
                  {sessionDetail.sentiment || "Neutral"} ({sessionDetail.emotion || "Calm"})
                </span>
              </div>

              {/* Intent */}
              <div className="border-l border-slate-200 pl-3">
                <span className="text-[10px] text-slate-400 block font-semibold">Detected Intent</span>
                <span className="font-bold text-[#006a6a] truncate block">
                  {sessionDetail.intent || "General Ingestion Query"}
                </span>
              </div>

              {/* Assigned Agent */}
              <div className="border-l border-slate-200 pl-3">
                <span className="text-[10px] text-slate-400 block font-semibold">Active Handler</span>
                <span className="font-bold text-slate-800">
                  {sessionDetail.assignedAgent || "Gemini AI Agent"}
                </span>
              </div>
            </div>

            {/* Live Message Transcript */}
            <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
              {sessionDetail.messages && sessionDetail.messages.length > 0 ? (
                sessionDetail.messages.map((m, idx) => {
                  const isUser = m.source === "USER";
                  const isHuman = m.source === "HUMAN_AGENT";

                  return (
                    <div
                      key={idx}
                      className={`flex flex-col max-w-[80%] ${
                        isUser ? "self-start items-start" : "self-end items-end"
                      }`}
                    >
                      <span className="text-[10px] text-slate-400 mb-0.5 px-1 font-semibold">
                        {isUser
                          ? "Customer"
                          : isHuman
                          ? `👤 ${m.agentName || "Human Specialist"}`
                          : "🤖 AI Assistant"}
                      </span>
                      <div
                        className={`p-3 rounded-2xl text-xs leading-relaxed shadow-2xs whitespace-pre-wrap ${
                          isUser
                            ? "bg-slate-200/80 text-slate-900 rounded-tl-none"
                            : isHuman
                            ? "bg-amber-600 text-white rounded-tr-none font-medium"
                            : "bg-[#006a6a] text-white rounded-tr-none"
                        }`}
                      >
                        {m.text || m.content}
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-center my-auto text-xs text-slate-400">
                  No messages recorded for this session yet.
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Supervisor Reply Box */}
            <form
              onSubmit={handleSendSupervisorMessage}
              className="p-3 bg-white border-t border-slate-200 flex items-center gap-2 shrink-0"
            >
              <input
                type="text"
                placeholder={
                  isHumanMode
                    ? "Type message as Human Support Agent..."
                    : "Takeover call to reply as Human Agent..."
                }
                value={agentReply}
                onChange={(e) => setAgentReply(e.target.value)}
                disabled={sendingMsg}
                className="flex-1 bg-slate-50 border border-slate-300 rounded-xl px-4 py-2 text-xs text-slate-800 outline-none focus:border-[#006a6a] focus:bg-white transition-all"
              />
              <button
                type="submit"
                disabled={!agentReply.trim() || sendingMsg}
                className="px-4 py-2 bg-[#006a6a] text-white font-bold rounded-xl text-xs hover:bg-[#005555] disabled:opacity-50 transition-all flex items-center gap-1.5 cursor-pointer"
              >
                <span className="material-symbols-outlined text-sm">send</span>
                Send
              </button>
            </form>
          </div>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center p-8 text-slate-400 text-xs">
            <span className="material-symbols-outlined text-4xl mb-2 text-slate-300">chat_bubble_outline</span>
            Select a live session from the left panel to monitor transcripts and intervene.
          </div>
        )}
      </div>
    </div>
  );
}

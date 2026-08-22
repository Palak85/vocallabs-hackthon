import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../services/api";

const SUGGESTION_CHIPS = [
  "My UPI transaction failed and money was deducted",
  "How do I request a refund for a duplicate charge?",
  "What is the warranty policy for hardware?",
  "I want to track my recent order status",
];

export default function CustomerChat() {
  const navigate = useNavigate();
  const [activeMode, setActiveMode] = useState("chat"); // 'chat' | 'voice'
  const [messages, setMessages] = useState([
    {
      id: "init-1",
      role: "AI Assistant",
      text: "Hello! I'm your AI Assistant. How can I help you today?",
      time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    },
  ]);
  const [inputText, setInputText] = useState("");
  const [conversationId, setConversationId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);

  // Voice Call States
  const [isCalling, setIsCalling] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [speakerEnabled, setSpeakerEnabled] = useState(true);
  const [callDuration, setCallDuration] = useState(0);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isListening, setIsListening] = useState(false);

  // Call History Modal
  const [historyOpen, setHistoryOpen] = useState(false);
  const [pastConversations, setPastConversations] = useState([]);
  const [selectedHistory, setSelectedHistory] = useState(null);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const recognitionRef = useRef(null);
  const fileInputRef = useRef(null);
  const messagesEndRef = useRef(null);
  const timerRef = useRef(null);

  // Auto scroll chat
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading]);

  // Call duration timer
  useEffect(() => {
    if (isCalling) {
      timerRef.current = setInterval(() => {
        setCallDuration((prev) => prev + 1);
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
      setCallDuration(0);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isCalling]);

  // Setup Web Speech API for Voice & Chat
  useEffect(() => {
    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;

    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = "en-US";

      recognition.onresult = async (event) => {
        let transcriptText = "";
        for (let i = event.resultIndex; i < event.results.length; i++) {
          transcriptText += event.results[i][0].transcript;
        }

        if (event.results[event.results.length - 1].isFinal) {
          const finalPrompt = transcriptText.trim();
          if (finalPrompt) {
            setInputText("");
            await handleSend(finalPrompt, "You");
          }
        } else {
          setInputText(transcriptText);
        }
      };

      recognition.onerror = (err) => {
        console.error("STT Error:", err);
        setIsListening(false);
      };

      recognition.onend = () => {
        if (isCalling && !isMuted) {
          try {
            recognition.start();
          } catch (e) {}
        } else {
          setIsListening(false);
        }
      };

      recognitionRef.current = recognition;
    }

    return () => {
      if (typeof window !== "undefined" && window.speechSynthesis) {
        window.speechSynthesis.cancel();
      }
    };
  }, [isCalling, isMuted, conversationId]);

  // TTS Output
  const speakResponse = (text) => {
    if (!speakerEnabled || typeof window === "undefined" || !("speechSynthesis" in window)) {
      return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 1.0;
    utterance.pitch = 1.1;

    const voices = window.speechSynthesis.getVoices();
    const friendlyVoice = voices.find(
      (v) => v.lang.startsWith("en") && (v.name.includes("Google") || v.name.includes("Samantha") || v.name.includes("Natural"))
    );
    if (friendlyVoice) utterance.voice = friendlyVoice;

    utterance.onstart = () => setIsSpeaking(true);
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);

    window.speechSynthesis.speak(utterance);
  };

  // Start Call
  const handleStartCall = () => {
    setIsCalling(true);
    setIsMuted(false);
    if (recognitionRef.current) {
      try {
        recognitionRef.current.start();
        setIsListening(true);
      } catch (e) {}
    }
    // Greet user
    speakResponse("Call connected. How can I help you today?");
  };

  // End Call
  const handleEndCall = () => {
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch (e) {}
    }
    if (typeof window !== "undefined" && window.speechSynthesis) {
      window.speechSynthesis.cancel();
    }
    setIsCalling(false);
    setIsListening(false);
    setIsSpeaking(false);
  };

  const toggleMute = () => {
    if (isMuted) {
      setIsMuted(false);
      try {
        recognitionRef.current?.start();
        setIsListening(true);
      } catch (e) {}
    } else {
      setIsMuted(true);
      try {
        recognitionRef.current?.stop();
        setIsListening(false);
      } catch (e) {}
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) setSelectedFile(file);
    e.target.value = "";
  };

  // Main message dispatch connected to real backend
  const handleSend = async (textToSend = inputText, role = "You") => {
    const cleanText = (textToSend || "").trim();
    if (!cleanText && !selectedFile) return;

    const currentFile = selectedFile;
    const timeString = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

    let displayMsg = cleanText;
    if (currentFile) {
      const fileLabel = `📎 ${currentFile.name}`;
      displayMsg = displayMsg ? `${displayMsg}\n${fileLabel}` : fileLabel;
    }

    const userMessageObj = {
      id: Date.now().toString(),
      role: role,
      text: displayMsg,
      time: timeString,
    };

    setMessages((prev) => [...prev, userMessageObj]);
    setInputText("");
    setSelectedFile(null);
    setIsLoading(true);

    try {
      // 1. Upload document if present
      if (currentFile) {
        setMessages((prev) => [
          ...prev,
          {
            id: "sys-" + Date.now(),
            role: "System",
            text: `Uploading "${currentFile.name}" for real-time RAG ingestion...`,
          },
        ]);
        await api.uploadDocument(currentFile);
        setMessages((prev) => prev.filter((m) => !m.id.startsWith("sys-")));
      }

      // 2. Query LLM / RAG Backend
      if (cleanText) {
        const response = await api.sendMessage(cleanText, conversationId);

        if (response.conversationId) {
          setConversationId(response.conversationId);
        }

        const aiResponse = {
          id: response.messageId || "ai-" + Date.now(),
          role: response.source === "HUMAN_AGENT" ? "Live Specialist" : "AI Assistant",
          text: response.answer || "I have received your inquiry.",
          time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          intent: response.intent,
          emotion: response.emotion,
        };

        setMessages((prev) => [...prev, aiResponse]);

        if (activeMode === "voice" || speakerEnabled) {
          speakResponse(aiResponse.text);
        }
      }
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          id: "err-" + Date.now(),
          role: "System",
          text: `⚠️ Network Error: ${err.message || "Failed to reach backend."}`,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  // Open Call History Modal
  const openCallHistory = async () => {
    setHistoryOpen(true);
    setLoadingHistory(true);
    try {
      const convs = await api.listConversations();
      setPastConversations(convs || []);
      if (convs && convs.length > 0) {
        loadHistoryDetail(convs[0].id);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoadingHistory(false);
    }
  };

  const loadHistoryDetail = async (id) => {
    try {
      const detail = await api.getConversation(id);
      setSelectedHistory(detail);
    } catch (e) {
      console.error(e);
    }
  };

  const formatTimer = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  return (
    <div className="w-full h-screen bg-[#f8faf9] flex flex-col font-sans select-none overflow-hidden">
      {/* Top Header */}
      <header className="h-16 px-6 bg-white border-b border-[#e5ebe9] flex items-center justify-between shrink-0 shadow-sm z-20">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate("/")}
            className="flex items-center gap-2 text-xl font-bold text-[#006a6a] tracking-tight hover:opacity-90 cursor-pointer"
          >
            <div className="w-8 h-8 rounded-lg bg-[#006a6a] flex items-center justify-center text-white">
              <span className="material-symbols-outlined text-[20px]">smart_toy</span>
            </div>
            <span>SuperviseAI Hub</span>
          </button>
          <span className="w-2.5 h-2.5 rounded-full bg-[#c98e28] inline-block shadow-sm" />
          <span className="text-xs font-semibold text-slate-500 hidden sm:inline-block">
            Customer Support Portal
          </span>
        </div>

        {/* Center / Right controls */}
        <div className="flex items-center gap-3">
          {/* History Button */}
          <button
            onClick={openCallHistory}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#bac9c9] text-xs font-semibold text-slate-700 hover:bg-[#eef5f4] transition-colors cursor-pointer"
          >
            <span className="material-symbols-outlined text-[16px]">history</span>
            <span>Call History</span>
          </button>

          {/* Speaker / Mute Toggle */}
          <button
            onClick={() => setSpeakerEnabled(!speakerEnabled)}
            title={speakerEnabled ? "Mute Speaker" : "Unmute Speaker"}
            className={`p-2 rounded-full transition-colors cursor-pointer ${
              speakerEnabled ? "text-[#006a6a] hover:bg-slate-100" : "text-slate-400 hover:bg-slate-100"
            }`}
          >
            <span className="material-symbols-outlined text-2xl">
              {speakerEnabled ? "volume_up" : "volume_off"}
            </span>
          </button>

          {/* Close / Return */}
          <button
            onClick={() => navigate("/")}
            title="Return to Home"
            className="p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-full transition-colors cursor-pointer"
          >
            <span className="material-symbols-outlined text-2xl">close</span>
          </button>
        </div>
      </header>

      {/* Mode Switcher Banner */}
      <div className="px-6 py-2.5 bg-[#f0f4f3] border-b border-[#e2e8e7] shrink-0">
        <div className="max-w-4xl mx-auto bg-[#dbe3e1] p-1 rounded-full flex items-center shadow-inner">
          <button
            onClick={() => setActiveMode("chat")}
            className={`flex-1 py-1.5 rounded-full text-sm font-semibold transition-all cursor-pointer flex items-center justify-center gap-2 ${
              activeMode === "chat"
                ? "bg-white text-slate-800 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <span className="material-symbols-outlined text-[18px]">chat</span>
            Text Chat
          </button>
          <button
            onClick={() => setActiveMode("voice")}
            className={`flex-1 py-1.5 rounded-full text-sm font-semibold transition-all cursor-pointer flex items-center justify-center gap-2 ${
              activeMode === "voice"
                ? "bg-white text-[#006a6a] shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <span className="material-symbols-outlined text-[18px]">call</span>
            Voice Call Mode
          </button>
        </div>
      </div>

      {/* Main Dual-Side Workspace */}
      <div className="flex-1 flex overflow-hidden">
        {activeMode === "chat" ? (
          /* SIDE A: TEXT CHAT MODE */
          <div className="flex-1 flex flex-col justify-between max-w-4xl w-full mx-auto p-4 md:p-6 overflow-hidden">
            {/* Quick Suggestion Chips */}
            <div className="flex gap-2 overflow-x-auto pb-2 shrink-0 no-scrollbar">
              {SUGGESTION_CHIPS.map((chip, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSend(chip, "You")}
                  className="whitespace-nowrap px-3 py-1.5 bg-white rounded-full border border-slate-200 text-xs text-slate-700 hover:border-[#006a6a] hover:text-[#006a6a] transition-all shadow-2xs cursor-pointer shrink-0"
                >
                  ⚡ {chip}
                </button>
              ))}
            </div>

            {/* Message Stream */}
            <div className="flex-1 overflow-y-auto my-2 pr-1 flex flex-col gap-4">
              {messages.map((msg) => {
                if (msg.role === "System") {
                  return (
                    <div
                      key={msg.id}
                      className="self-center my-1 px-4 py-1.5 rounded-full bg-[#e8efee] text-xs text-slate-600 border border-[#bac9c9] flex items-center gap-2"
                    >
                      <span className="material-symbols-outlined text-[14px]">info</span>
                      {msg.text}
                    </div>
                  );
                }

                const isUser = msg.role === "You" || msg.sender === "user";
                const isSpecialist = msg.role === "Live Specialist";

                return (
                  <div
                    key={msg.id}
                    className={`flex gap-3 max-w-[85%] ${
                      isUser ? "self-end flex-row-reverse items-end" : "self-start items-start"
                    }`}
                  >
                    {/* Avatar */}
                    {!isUser && (
                      <div
                        className={`w-8 h-8 rounded-full border flex items-center justify-center shrink-0 mt-0.5 shadow-sm ${
                          isSpecialist
                            ? "bg-amber-100 border-amber-300 text-amber-800"
                            : "bg-[#dce5e4] border-[#c4d2d1] text-slate-600"
                        }`}
                      >
                        <span className="material-symbols-outlined text-[18px]">
                          {isSpecialist ? "support_agent" : "smart_toy"}
                        </span>
                      </div>
                    )}

                    {/* Bubble */}
                    <div className="flex flex-col gap-1">
                      <div
                        className={`px-4 py-3 rounded-2xl text-[14px] leading-relaxed shadow-sm whitespace-pre-wrap ${
                          isUser
                            ? "bg-[#006a6a] text-white rounded-tr-sm"
                            : isSpecialist
                            ? "bg-amber-50 text-amber-950 border border-amber-200 rounded-tl-sm"
                            : "bg-[#e2e8e7] text-slate-800 rounded-tl-sm"
                        }`}
                      >
                        {isSpecialist && (
                          <div className="text-[11px] font-bold text-amber-800 mb-1 flex items-center gap-1">
                            <span className="w-2 h-2 rounded-full bg-amber-500 animate-pulse" />
                            Live Human Specialist
                          </div>
                        )}
                        {msg.text}
                      </div>
                      <span className={`text-[10px] text-slate-400 ${isUser ? "text-right mr-1" : "ml-1"}`}>
                        {msg.time} • {msg.role || (isUser ? "You" : "AI")}
                      </span>
                    </div>
                  </div>
                );
              })}

              {isLoading && (
                <div className="flex gap-3 self-start items-center">
                  <div className="w-8 h-8 rounded-full bg-[#dce5e4] border border-[#c4d2d1] flex items-center justify-center shrink-0 text-slate-600">
                    <span className="material-symbols-outlined text-[18px] animate-spin">
                      progress_activity
                    </span>
                  </div>
                  <div className="px-4 py-2.5 rounded-2xl bg-[#e2e8e7] text-slate-600 text-sm flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#006a6a] animate-bounce" />
                    <span className="w-2 h-2 rounded-full bg-[#006a6a] animate-bounce [animation-delay:150ms]" />
                    <span className="w-2 h-2 rounded-full bg-[#006a6a] animate-bounce [animation-delay:300ms]" />
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Form */}
            <div className="pt-2 border-t border-[#e5ebe9] flex flex-col gap-2">
              {selectedFile && (
                <div className="flex items-center gap-2 px-3 py-1 bg-[#eef5f4] rounded-lg border border-[#bac9c9] w-fit">
                  <span className="material-symbols-outlined text-sm text-[#006a6a]">attach_file</span>
                  <span className="text-xs text-slate-700 truncate max-w-[250px]">{selectedFile.name}</span>
                  <button
                    onClick={() => setSelectedFile(null)}
                    className="text-slate-400 hover:text-red-500 cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-sm">close</span>
                  </button>
                </div>
              )}

              <div className="flex items-center gap-3">
                <input
                  ref={fileInputRef}
                  type="file"
                  className="hidden"
                  onChange={handleFileSelect}
                />
                <button
                  onClick={() => fileInputRef.current?.click()}
                  type="button"
                  title="Upload Document"
                  className="p-2 text-slate-600 hover:text-[#006a6a] hover:bg-slate-100 rounded-full transition-colors cursor-pointer shrink-0"
                >
                  <span className="material-symbols-outlined text-2xl">attach_file</span>
                </button>

                <div className="flex-1 bg-white border border-slate-300 rounded-full px-4 py-1.5 shadow-sm flex items-center gap-2 focus-within:border-[#006a6a] focus-within:ring-2 focus-within:ring-[#006a6a]/20 transition-all">
                  <input
                    type="text"
                    placeholder="Type your question here..."
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" && !e.shiftKey) {
                        e.preventDefault();
                        handleSend(inputText, "You");
                      }
                    }}
                    disabled={isLoading}
                    className="w-full bg-transparent text-slate-800 placeholder:text-slate-400 text-sm outline-none"
                  />
                  <button
                    onClick={() => {
                      if (recognitionRef.current) {
                        try {
                          recognitionRef.current.start();
                          setIsListening(true);
                        } catch (e) {}
                      }
                    }}
                    type="button"
                    title="Speak to type"
                    className="p-1.5 text-slate-500 hover:text-[#006a6a] cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-xl">
                      {isListening ? "mic" : "mic_none"}
                    </span>
                  </button>
                </div>

                <button
                  onClick={() => handleSend(inputText, "You")}
                  disabled={isLoading || (!inputText.trim() && !selectedFile)}
                  type="button"
                  title="Send Message"
                  className="w-11 h-11 bg-[#006a6a] text-white rounded-full flex items-center justify-center shadow-md hover:bg-[#005555] active:scale-95 disabled:opacity-50 transition-all cursor-pointer shrink-0"
                >
                  <span className="material-symbols-outlined text-xl -rotate-45 ml-0.5">send</span>
                </button>
              </div>
            </div>
          </div>
        ) : (
          /* SIDE B: VOICE CALL MODE */
          <div className="flex-1 grid md:grid-cols-2 gap-6 p-6 max-w-6xl w-full mx-auto overflow-hidden">
            {/* Left Box: Audio Call Console & Soundwave */}
            <div className="bg-white rounded-2xl border border-slate-200 shadow-md p-6 flex flex-col justify-between items-center text-center">
              {/* Call Status Badge */}
              <div className="flex items-center gap-2 px-4 py-1.5 rounded-full bg-slate-100 border border-slate-200">
                <span
                  className={`w-3 h-3 rounded-full ${
                    isCalling ? "bg-emerald-500 animate-ping" : "bg-slate-400"
                  }`}
                />
                <span className="text-xs font-bold text-slate-800">
                  {isCalling
                    ? `Call Connected: ${formatTimer(callDuration)}`
                    : "Call Disconnected"}
                </span>
              </div>

              {/* Central Pulsing Visualizer */}
              <div className="my-8 flex flex-col items-center">
                <div
                  className={`w-36 h-36 rounded-full flex items-center justify-center border-4 border-white shadow-2xl transition-all ${
                    isSpeaking
                      ? "bg-[#006a6a] ring-8 ring-[#47e5e6]/40 scale-105"
                      : isListening
                      ? "bg-emerald-600 ring-8 ring-emerald-300 scale-105"
                      : isCalling
                      ? "bg-[#006a6a]"
                      : "bg-slate-300 text-slate-500"
                  }`}
                >
                  <span className="material-symbols-outlined text-white text-6xl">
                    {isSpeaking ? "graphic_eq" : isCalling ? "mic" : "phone_disabled"}
                  </span>
                </div>

                {/* Animated Waveform Visualizer */}
                {isCalling && (
                  <div className="flex gap-1.5 items-end h-8 mt-6">
                    <span className={`w-2 bg-[#006a6a] rounded-full transition-all ${isSpeaking || isListening ? "h-8 animate-bounce" : "h-2"}`} />
                    <span className={`w-2 bg-[#47e5e6] rounded-full transition-all ${isSpeaking || isListening ? "h-6 animate-bounce [animation-delay:100ms]" : "h-3"}`} />
                    <span className={`w-2 bg-emerald-500 rounded-full transition-all ${isSpeaking || isListening ? "h-8 animate-bounce [animation-delay:200ms]" : "h-2"}`} />
                    <span className={`w-2 bg-[#006a6a] rounded-full transition-all ${isSpeaking || isListening ? "h-5 animate-bounce [animation-delay:300ms]" : "h-4"}`} />
                    <span className={`w-2 bg-emerald-400 rounded-full transition-all ${isSpeaking || isListening ? "h-7 animate-bounce [animation-delay:150ms]" : "h-2"}`} />
                  </div>
                )}
              </div>

              {/* Call Controls */}
              <div className="w-full flex items-center justify-center gap-4">
                {!isCalling ? (
                  <button
                    onClick={handleStartCall}
                    className="px-8 py-3.5 bg-emerald-600 text-white font-bold rounded-full shadow-lg hover:bg-emerald-700 transition-all flex items-center gap-2 cursor-pointer active:scale-95"
                  >
                    <span className="material-symbols-outlined text-2xl">call</span>
                    Start Voice Call
                  </button>
                ) : (
                  <>
                    <button
                      onClick={toggleMute}
                      className={`p-3.5 rounded-full shadow-md transition-all cursor-pointer ${
                        isMuted
                          ? "bg-amber-100 text-amber-800 border border-amber-300"
                          : "bg-slate-100 text-slate-700 hover:bg-slate-200"
                      }`}
                      title={isMuted ? "Unmute Mic" : "Mute Mic"}
                    >
                      <span className="material-symbols-outlined text-2xl">
                        {isMuted ? "mic_off" : "mic"}
                      </span>
                    </button>

                    <button
                      onClick={() => setSpeakerEnabled(!speakerEnabled)}
                      className={`p-3.5 rounded-full shadow-md transition-all cursor-pointer ${
                        !speakerEnabled
                          ? "bg-amber-100 text-amber-800 border border-amber-300"
                          : "bg-slate-100 text-slate-700 hover:bg-slate-200"
                      }`}
                      title={speakerEnabled ? "Mute Speaker" : "Enable Speaker"}
                    >
                      <span className="material-symbols-outlined text-2xl">
                        {speakerEnabled ? "volume_up" : "volume_off"}
                      </span>
                    </button>

                    <button
                      onClick={handleEndCall}
                      className="px-6 py-3.5 bg-red-600 text-white font-bold rounded-full shadow-lg hover:bg-red-700 transition-all flex items-center gap-2 cursor-pointer active:scale-95"
                      title="End Call"
                    >
                      <span className="material-symbols-outlined text-2xl">call_end</span>
                      End Call
                    </button>
                  </>
                )}
              </div>
            </div>

            {/* Right Box: Live Call Transcript Stream */}
            <div className="bg-white rounded-2xl border border-slate-200 shadow-md p-6 flex flex-col overflow-hidden">
              <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-3 flex items-center gap-2 border-b pb-2">
                <span className="material-symbols-outlined text-[#006a6a] text-lg">subtitles</span>
                Live Call Transcript Stream
              </h3>

              <div className="flex-1 overflow-y-auto flex flex-col gap-3 pr-1">
                {messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`p-3 rounded-xl text-xs leading-relaxed ${
                      msg.role === "You" || msg.sender === "user"
                        ? "bg-slate-100 border-l-4 border-[#006a6a] text-slate-800"
                        : msg.role === "Live Specialist"
                        ? "bg-amber-50 border-l-4 border-amber-500 text-amber-950 font-medium"
                        : "bg-emerald-50 border-l-4 border-emerald-500 text-slate-800"
                    }`}
                  >
                    <div className="font-bold text-[11px] mb-0.5 text-slate-600">
                      [{msg.role || "AI"}] • {msg.time}
                    </div>
                    {msg.text}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Call History Modal */}
      {historyOpen && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full h-[550px] shadow-2xl border border-slate-200 flex flex-col overflow-hidden">
            <div className="px-6 py-4 border-b flex justify-between items-center bg-slate-50">
              <h3 className="font-bold text-slate-800 flex items-center gap-2">
                <span className="material-symbols-outlined text-[#006a6a]">history</span>
                Previous Call & Chat Transcripts
              </h3>
              <button
                onClick={() => setHistoryOpen(false)}
                className="text-slate-400 hover:text-slate-700 cursor-pointer"
              >
                <span className="material-symbols-outlined text-2xl">close</span>
              </button>
            </div>

            <div className="flex-1 flex overflow-hidden">
              {/* Session list */}
              <div className="w-1/3 border-r overflow-y-auto p-2 flex flex-col gap-1 bg-slate-50/50">
                {loadingHistory ? (
                  <div className="p-4 text-xs text-slate-400 text-center">Loading...</div>
                ) : pastConversations.length === 0 ? (
                  <div className="p-4 text-xs text-slate-400 text-center">No past calls found.</div>
                ) : (
                  pastConversations.map((conv) => (
                    <button
                      key={conv.id}
                      onClick={() => loadHistoryDetail(conv.id)}
                      className={`text-left p-2.5 rounded-lg text-xs font-medium transition-colors cursor-pointer ${
                        selectedHistory?.id === conv.id
                          ? "bg-[#006a6a] text-white"
                          : "hover:bg-slate-100 text-slate-700"
                      }`}
                    >
                      <div className="font-bold truncate">{conv.title || "Support Call"}</div>
                      <div className="text-[10px] opacity-75 mt-0.5">
                        {conv.createdAt ? new Date(conv.createdAt).toLocaleDateString() : "Recent"}
                      </div>
                    </button>
                  ))
                )}
              </div>

              {/* Message transcript */}
              <div className="flex-1 p-4 overflow-y-auto flex flex-col gap-3">
                {selectedHistory?.messages && selectedHistory.messages.length > 0 ? (
                  selectedHistory.messages.map((m, i) => (
                    <div
                      key={i}
                      className={`p-3 rounded-xl text-xs ${
                        m.source === "USER"
                          ? "bg-slate-100 text-slate-800"
                          : "bg-[#eef5f4] text-[#004f50] border border-[#b8ecec]"
                      }`}
                    >
                      <span className="font-bold text-[10px] block mb-1">
                        [{m.source === "USER" ? "You" : m.source === "HUMAN_AGENT" ? "Live Agent" : "AI"}]
                      </span>
                      {m.text || m.content}
                    </div>
                  ))
                ) : (
                  <div className="text-center my-auto text-xs text-slate-400">
                    Select a conversation to view full transcript history.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

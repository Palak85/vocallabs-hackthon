import { useState, useEffect, useRef } from "react";

export default function Robot() {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isWaving, setIsWaving] = useState(false);
  const [isHovered, setIsHovered] = useState(false);
  const [speechText, setSpeechText] = useState("Hi! Welcome to Luminous AI Support! 👋");

  const speechPhrases = [
    "Hi! Welcome to Luminous AI Support! 👋",
    "I resolve 80% of customer issues in under 30 seconds! ⚡",
    "Need human backup? I seamlessly escalate to live specialists! 🤝",
    "Empower your team with zero-repeat support experiences! 🚀",
  ];
  const phraseIndexRef = useRef(0);

  useEffect(() => {
    return () => {
      if (typeof window !== "undefined" && window.speechSynthesis) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  const handleInteract = () => {
    const nextIndex = (phraseIndexRef.current + 1) % speechPhrases.length;
    phraseIndexRef.current = nextIndex;
    const currentPhrase = speechPhrases[nextIndex];
    setSpeechText(currentPhrase);
    setIsWaving(true);

    if (typeof window !== "undefined" && "speechSynthesis" in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(
        currentPhrase.replace(/[👋⚡🤝🚀]/g, "")
      );
      utterance.rate = 1.05;
      utterance.pitch = 1.15;

      const voices = window.speechSynthesis.getVoices();
      const friendlyVoice = voices.find(
        (v) =>
          v.lang.startsWith("en") &&
          (v.name.includes("Google") ||
            v.name.includes("Samantha") ||
            v.name.includes("Natural"))
      );
      if (friendlyVoice) utterance.voice = friendlyVoice;

      utterance.onstart = () => setIsSpeaking(true);
      utterance.onend = () => {
        setIsSpeaking(false);
        setTimeout(() => setIsWaving(false), 2000);
      };
      utterance.onerror = () => setIsSpeaking(false);

      window.speechSynthesis.speak(utterance);
    }
  };

  return (
    <div className="relative flex flex-col items-center justify-center w-full max-w-md select-none group">
      {/* Speech Bubble */}
      <div
        onClick={handleInteract}
        className="relative cursor-pointer mb-6 px-6 py-3 bg-white rounded-2xl border border-slate-200/90 shadow-md hover:shadow-lg transition-all duration-300 transform hover:-translate-y-0.5"
      >
        <p className="text-sm font-semibold text-slate-800 text-center tracking-tight">
          {speechText}
        </p>
        {/* Downward triangle arrow */}
        <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 w-4 h-4 bg-white border-r border-b border-slate-200/90 rotate-45" />
      </div>

      {/* Robot Character */}
      <div
        onClick={handleInteract}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        className="cursor-pointer relative flex flex-col items-center transition-transform duration-300 hover:scale-105"
      >
        {/* Antenna */}
        <div className="flex flex-col items-center">
          <span
            className={`w-3.5 h-3.5 rounded-full border-2 border-slate-800 transition-colors shadow-sm ${
              isSpeaking
                ? "bg-emerald-400 shadow-[0_0_12px_#34d399]"
                : "bg-cyan-400 shadow-[0_0_8px_#38bdf8]"
            }`}
          />
          <span className="w-1 h-4 bg-slate-700 -mt-0.5" />
        </div>

        {/* Head and Ears */}
        <div className="relative flex items-center justify-center">
          {/* Left Ear */}
          <div className="w-3 h-10 bg-slate-800 rounded-l-lg -mr-1 z-0" />

          {/* Head Screen */}
          <div className="relative w-36 h-24 bg-[#3b4b56] rounded-3xl border-4 border-slate-800 shadow-xl flex items-center justify-around px-4 z-10 overflow-hidden">
            {/* Screen Glare Highlight */}
            <div className="absolute top-0 left-0 w-full h-1/2 bg-white/10 rounded-t-3xl pointer-events-none" />

            {/* Left Eye */}
            <div
              className={`rounded-full transition-all duration-300 flex items-center justify-center ${
                isSpeaking
                  ? "w-7 h-4 bg-emerald-300 shadow-[0_0_14px_#34d399]"
                  : isHovered
                  ? "w-7 h-7 bg-cyan-300 shadow-[0_0_16px_#67e8f9]"
                  : "w-6 h-6 bg-cyan-300 shadow-[0_0_12px_#67e8f9]"
              }`}
            >
              <span className="w-2 h-2 bg-white rounded-full opacity-90" />
            </div>

            {/* Eye Bridge */}
            <div className="w-4 h-1 bg-cyan-300/80 rounded-full" />

            {/* Right Eye */}
            <div
              className={`rounded-full transition-all duration-300 flex items-center justify-center ${
                isSpeaking
                  ? "w-7 h-4 bg-emerald-300 shadow-[0_0_14px_#34d399]"
                  : isHovered
                  ? "w-7 h-7 bg-cyan-300 shadow-[0_0_16px_#67e8f9]"
                  : "w-6 h-6 bg-cyan-300 shadow-[0_0_12px_#67e8f9]"
              }`}
            >
              <span className="w-2 h-2 bg-white rounded-full opacity-90" />
            </div>
          </div>

          {/* Right Ear + Headset Mic Boom */}
          <div className="relative -ml-1 z-0">
            <div className="w-3 h-10 bg-slate-800 rounded-r-lg" />
            {/* Headset Mic */}
            <div className="absolute top-5 left-1.5 w-8 h-1.5 bg-slate-700 rounded-full rotate-45 origin-left flex items-center justify-end">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 -mr-1 shadow-[0_0_8px_#34d399]" />
            </div>
          </div>
        </div>

        {/* Neck */}
        <div className="w-10 h-2 bg-slate-700 -my-0.5 border-x-2 border-slate-800" />

        {/* Body */}
        <div className="relative w-36 h-28 bg-[#f1f5f9] rounded-3xl border-4 border-slate-800 shadow-lg flex flex-col items-center justify-center p-2">
          {/* Left Arm (Waving) */}
          <div
            className={`absolute -left-6 top-2 w-4 h-16 bg-[#67e8f9] rounded-full border-2 border-slate-800 shadow-md origin-top transition-transform duration-300 ${
              isWaving || isSpeaking
                ? "rotate-[-35deg] animate-pulse"
                : "-rotate-12"
            }`}
          >
            <div className="w-3 h-3 bg-slate-800 rounded-full mx-auto mt-0.5" />
          </div>

          {/* Right Arm */}
          <div className="absolute -right-6 top-2 w-4 h-16 bg-[#67e8f9] rounded-full border-2 border-slate-800 shadow-md origin-top rotate-12">
            <div className="w-3 h-3 bg-slate-800 rounded-full mx-auto mt-0.5" />
          </div>

          {/* Chest Display (VOCAL AI) */}
          <div className="w-20 h-14 bg-[#334155] rounded-2xl border-2 border-slate-700 flex flex-col items-center justify-center p-1.5 shadow-inner">
            <div className="text-[10px] font-mono font-bold text-slate-100 uppercase tracking-tight mb-1">
              VOCAL AI
            </div>
            {/* Equalizer Waveform */}
            <div className="flex gap-1 items-end h-4 justify-center">
              <span
                className={`w-1.5 bg-cyan-300 rounded-full transition-all ${
                  isSpeaking ? "h-4 animate-bounce" : "h-2"
                }`}
              />
              <span
                className={`w-1.5 bg-emerald-400 rounded-full transition-all ${
                  isSpeaking ? "h-3.5 animate-bounce [animation-delay:100ms]" : "h-3"
                }`}
              />
              <span
                className={`w-1.5 bg-cyan-300 rounded-full transition-all ${
                  isSpeaking ? "h-4 animate-bounce [animation-delay:200ms]" : "h-1.5"
                }`}
              />
              <span
                className={`w-1.5 bg-emerald-400 rounded-full transition-all ${
                  isSpeaking ? "h-2.5 animate-bounce [animation-delay:150ms]" : "h-2"
                }`}
              />
            </div>
          </div>
        </div>

        {/* Floating shadow beneath robot */}
        <div className="w-28 h-3 bg-slate-400/20 rounded-full blur-xs mt-3" />
      </div>

      {/* Click to hear voice label */}
      <div
        onClick={handleInteract}
        className="mt-4 flex items-center gap-1.5 text-xs text-slate-600 font-medium hover:text-[#006a6a] cursor-pointer transition-colors"
      >
        <span className="material-symbols-outlined text-sm">touch_app</span>
        <span>Click to hear voice & interact</span>
      </div>
    </div>
  );
}

import { useNavigate } from "react-router-dom";
import Robot from "./Robot";

export default function Hero() {
  const navigate = useNavigate();

  return (
    <section className="max-w-7xl mx-auto px-6 py-12 md:py-20 grid md:grid-cols-2 gap-12 items-center">
      {/* Left Column — Copy */}
      <div className="space-y-6">
        {/* Live Monitored Badge */}
        <div className="inline-flex items-center gap-2 px-3.5 py-1 bg-slate-200/90 rounded-full border border-slate-300/80 shadow-2xs">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-[11px] font-bold text-slate-600 uppercase tracking-wider">
            LIVE MONITORED AI
          </span>
        </div>

        {/* Heading */}
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight leading-[1.15]">
          Supervised AI for Customer Support That Never Misses a Beat.
        </h1>

        {/* Subheading */}
        <p className="text-base text-slate-600 max-w-lg leading-relaxed">
          Empower your team with AI that learns from your best agents.
          Seamlessly escalate to humans when it matters most, ensuring
          zero-repeat experiences.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-3 pt-2">
          <button
            onClick={() => navigate("/customer")}
            className="bg-[#006a6a] hover:bg-[#005555] text-white font-bold text-sm px-6 py-3 rounded-lg shadow-sm hover:shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer active:scale-95"
          >
            Start Your Free Trial
            <span className="material-symbols-outlined text-[18px]">
              arrow_forward
            </span>
          </button>
          <button
            onClick={() => navigate("/dashboard")}
            className="bg-[#e5ebea] hover:bg-[#d8e2e0] text-slate-800 font-bold text-sm px-5 py-3 rounded-lg border border-slate-300/80 transition-all flex items-center justify-center gap-2 cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-[18px]">
              play_circle
            </span>
            Watch Demo
          </button>
        </div>
      </div>

      {/* Right Column — Exact Themed Robot */}
      <div className="w-full flex justify-center items-center">
        <Robot />
      </div>
    </section>
  );
}

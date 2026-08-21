import { useNavigate } from "react-router-dom";

export default function Hero() {
  const navigate = useNavigate();

  return (
    <section className="max-w-7xl mx-auto px-6 py-16 md:py-24 grid md:grid-cols-2 gap-12 items-center">
      {/* Left Column — Copy */}
      <div className="space-y-8">
        {/* Badge */}
        <div className="inline-flex items-center gap-2 px-3 py-1 bg-surface-container-high rounded-full border border-surface-variant">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse-dot" />
          <span className="text-label-xs font-medium text-on-surface-variant uppercase tracking-wider">
            Live Monitored AI
          </span>
        </div>

        {/* Heading */}
        <h1 className="text-4xl md:text-5xl font-extrabold text-on-background tracking-tight leading-tight">
          Supervised AI for Customer Support That Never Misses a Beat.
        </h1>

        {/* Subheading */}
        <p className="text-lg text-on-surface-variant max-w-lg">
          Empower your team with AI that learns from your best agents.
          Seamlessly escalate to humans when it matters most, ensuring
          zero-repeat experiences.
        </p>

        {/* CTAs */}
        <div className="flex flex-col sm:flex-row gap-4">
          <button
            onClick={() => navigate("/dashboard")}
            className="bg-primary text-on-primary font-semibold text-label-md px-6 py-3 rounded-lg shadow-lg shadow-primary/20 hover:bg-primary-container hover:text-on-primary-container transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            Start Your Free Trial
            <span className="material-symbols-outlined text-[18px]">
              arrow_forward
            </span>
          </button>
          <button className="bg-surface-container text-on-surface font-semibold text-label-md px-6 py-3 rounded-lg border border-outline-variant hover:bg-surface-variant transition-all flex items-center justify-center gap-2 cursor-pointer">
            <span className="material-symbols-outlined text-[18px]">
              play_circle
            </span>
            Watch Demo
          </button>
        </div>
      </div>

      {/* Right Column — Dashboard Mockup */}
      <div className="relative w-full h-[500px] bg-surface-container-low rounded-xl border border-outline-variant shadow-2xl overflow-hidden flex justify-center items-center">
        <img
          src="https://lh3.googleusercontent.com/aida-public/AB6AXuBRz2LKB1zYLvfCRjudq9cS9Wri9DAzj6cdGuy7uOs7KidEG8HQLrBMZEkwS9sLcBjku1Qy5Q7rrIFkifIYAiAtkSKo8MzfcfYGmingQAT1Y_hHCexdfgrJxjFKVYE9kFN81aPz7j1mh22ejqWQ3f-vOB3lAiKjWQFpvUJ3HTDmbw1Up3dPQd4RZdlJZdgJS33MIoHMsbMTcGsDHWxsOGpwsWI8N0dFbojnD3xuBGkKqzDcBAsPB83S"
          alt="Luminous Support Dashboard Mockup"
          className="w-full h-full object-cover"
        />
      </div>
    </section>
  );
}

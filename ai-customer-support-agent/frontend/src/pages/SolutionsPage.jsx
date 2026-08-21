import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/common/Navbar";
import Footer from "../components/common/Footer";

const industries = [
  {
    id: "ecommerce",
    name: "E-Commerce & Retail",
    icon: "shopping_cart",
    badge: "Omnichannel & Peak Season Ready",
    title: "Instant Resolution for Orders, Returns & High-Value Carts",
    description:
      "Handle massive holiday volume spikes effortlessly. Automate 78% of order status, return processing, and sizing queries while routing VIP shoppers directly to top sales specialists.",
    stats: [
      { value: "78%", label: "First-contact deflection" },
      { value: "< 2.5s", label: "Average response time" },
      { value: "+34%", label: "Upsell conversion on assisted carts" },
    ],
    features: [
      "Real-time Shopify, Magento & WooCommerce order status syncing",
      "Automated instant return label generation & exchange workflows",
      "Intelligent VIP cart escalation when high cart value is detected",
      "Multilingual auto-translation across 95+ languages",
    ],
    quote: {
      text: "During Black Friday, SuperviseAI handled 65,000 inquiries in 48 hours. Our human agents only stepped in for high-value VIP escalations.",
      author: "Elena Rostova",
      role: "VP of Customer Experience, TrendVault Retail",
    },
  },
  {
    id: "fintech",
    name: "Fintech & Banking",
    icon: "account_balance",
    badge: "SOC2 & PCI-DSS Compliant",
    title: "Compliant & Verified Financial Support at Scale",
    description:
      "Deliver instant answers for transaction disputes, card freeze requests, and account verifications under strict regulatory guardrails and automated audit trails.",
    stats: [
      { value: "100%", label: "Audit-logged interactions" },
      { value: "85%", label: "Routine deflection rate" },
      { value: "0", label: "Compliance breaches" },
    ],
    features: [
      "Bank-grade PII & PCI data masking before LLM inference",
      "Automated card freeze & step-up biometric verification prompts",
      "Real-time sentiment and fraud risk anomaly triggers",
      "Seamless warm transfer to licensed banking specialists with full dossier",
    ],
    quote: {
      text: "The guardrails and human verification checkpoints gave our compliance officers total confidence to deploy AI in frontline banking operations.",
      author: "Marcus Sterling",
      role: "Head of Digital Operations, NovaPay Global",
    },
  },
  {
    id: "saas",
    name: "SaaS & Cloud Platforms",
    icon: "terminal",
    badge: "Developer & API Focused",
    title: "Deep Technical Diagnostics & Automated Escalation",
    description:
      "Resolve API errors, billing adjustments, and onboarding hurdles instantly. The AI reads logs, parses documentation, and synthesizes reproducible bug tickets directly into Jira or Linear.",
    stats: [
      { value: "4.9/5", label: "Developer CSAT score" },
      { value: "62%", label: "Reduction in Tier-2 ticket load" },
      { value: "4 min", label: "Average MTTR reduction" },
    ],
    features: [
      "Live documentation & API schema ingestion from GitHub & Notion",
      "Error log parsing with code snippet suggestions",
      "Automated reproduction steps synthesis & Jira/Linear ticket creation",
      "Live sandbox testing verification before advising developers",
    ],
    quote: {
      text: "Our engineers used to spend 30% of their week on support triage. SuperviseAI diagnose issues with precision and frees our team to build.",
      author: "Sarah Lin",
      role: "CTO & Co-founder, HyperQueue Cloud",
    },
  },
  {
    id: "healthcare",
    name: "Healthcare & Telehealth",
    icon: "health_and_safety",
    badge: "HIPAA-Conscious Architecture",
    title: "Empathetic Patient Triaging & Seamless Scheduling",
    description:
      "Provide 24/7 patient navigation, appointment rescheduling, and prescription status checks with zero wait times, backed by immediate clinical escalations when urgency is detected.",
    stats: [
      { value: "92%", label: "Patient satisfaction score" },
      { value: "< 1s", label: "Emergency triage trigger speed" },
      { value: "45%", label: "Reduction in no-show appointments" },
    ],
    features: [
      "HIPAA-compliant data encryption in transit and at rest",
      "Automated SMS/WhatsApp appointment reminders & one-tap rescheduling",
      "Clinical urgency keyword detection triggering immediate nurse handoff",
      "Insurance eligibility checks and provider directory lookup",
    ],
    quote: {
      text: "Patients get immediate reassurance and answers at midnight, while our clinic staff always retains full supervisory control over care discussions.",
      author: "Dr. Julian Vance",
      role: "Chief Medical Officer, CareLink Health",
    },
  },
];

const workflows = [
  {
    step: "01",
    title: "Intelligent Deflection & Confidence Scoring",
    description:
      "Every incoming request is analyzed by specialized LLMs trained on your knowledge base. Queries with confidence >90% are resolved immediately.",
    icon: "psychology",
    tag: "Sub-Second Resolution",
  },
  {
    step: "02",
    title: "Real-Time Agent Whisper & Copilot",
    description:
      "For moderate-confidence queries, AI drafts precision responses and retrieves customer history for your agents in real time, cutting reply time by 70%.",
    icon: "hearing",
    tag: "Agent Superpowers",
  },
  {
    step: "03",
    title: "Zero-Context-Loss Warm Handoff",
    description:
      "When human empathy or complex judgement is needed, the conversation transitions smoothly with a synthesized summary and sentiment tags.",
    icon: "swap_horizontal_circle",
    tag: "Seamless Transition",
  },
  {
    step: "04",
    title: "Self-Improving Knowledge Flywheel",
    description:
      "Human agent edits and resolutions automatically train and refine the knowledge base, ensuring edge cases are resolved permanently.",
    icon: "autorenew",
    tag: "Continuous Learning",
  },
];

export default function SolutionsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState(industries[0].id);

  // ROI Calculator State
  const [monthlyTickets, setMonthlyTickets] = useState(15000);
  const [teamSize, setTeamSize] = useState(12);

  // Calculations
  const deflectionRate = 0.72; // 72%
  const resolvedByAI = Math.round(monthlyTickets * deflectionRate);
  const hoursSavedPerMonth = Math.round(resolvedByAI * 0.18); // ~11 mins per ticket
  const annualDollarSavings = Math.round(hoursSavedPerMonth * 28 * 12); // $28/hr loaded cost

  const currentIndustry = industries.find((i) => i.id === activeTab) || industries[0];

  return (
    <div className="bg-background text-on-background min-h-screen">
      <Navbar />

      <main className="pt-24 pb-20">
        {/* ── 1. Hero Section ── */}
        <section className="max-w-7xl mx-auto px-6 py-12 md:py-20 text-center">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-high rounded-full border border-surface-variant mb-6 shadow-sm">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse-dot" />
            <span className="text-label-xs font-semibold text-primary uppercase tracking-wider">
              Enterprise & Scale-Ready AI Solutions
            </span>
          </div>

          <h1 className="text-4xl md:text-6xl font-extrabold text-on-background tracking-tight max-w-4xl mx-auto leading-tight">
            Tailored AI Supervision for Every Industry & Workflow.
          </h1>

          <p className="mt-6 text-lg md:text-xl text-on-surface-variant max-w-2xl mx-auto">
            From high-volume e-commerce to strictly regulated banking and 24/7 SaaS support—deploy supervised AI agents that amplify your team instead of replacing quality.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center items-center">
            <button
              onClick={() => navigate("/dashboard")}
              className="w-full sm:w-auto bg-primary text-on-primary font-semibold text-label-md px-7 py-3.5 rounded-lg shadow-lg shadow-primary/20 hover:bg-primary-container hover:text-on-primary-container transition-all flex items-center justify-center gap-2 cursor-pointer"
            >
              Explore Live Solutions
              <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
            </button>
            <button
              onClick={() => navigate("/pricing")}
              className="w-full sm:w-auto bg-surface-container text-on-surface font-semibold text-label-md px-7 py-3.5 rounded-lg border border-outline-variant hover:bg-surface-variant transition-all flex items-center justify-center gap-2 cursor-pointer"
            >
              <span className="material-symbols-outlined text-[18px]">payments</span>
              View Plans & ROI
            </button>
          </div>
        </section>

        {/* ── 2. Industry Selector Tabs ── */}
        <section className="max-w-7xl mx-auto px-6 py-12">
          <div className="text-center mb-8">
            <h2 className="text-2xl md:text-3xl font-bold text-on-background">
              Engineered for Your Industry's Specific Challenges
            </h2>
            <p className="mt-2 text-on-surface-variant text-body-md">
              Select your vertical to see domain-adapted workflows, compliance guardrails, and real metrics.
            </p>
          </div>

          {/* Tab Navigation */}
          <div className="flex flex-wrap justify-center gap-3 mb-10">
            {industries.map((ind) => (
              <button
                key={ind.id}
                onClick={() => setActiveTab(ind.id)}
                className={`flex items-center gap-2 px-5 py-3 rounded-xl font-semibold text-body-sm transition-all cursor-pointer ${
                  activeTab === ind.id
                    ? "bg-primary text-on-primary shadow-md shadow-primary/20"
                    : "bg-surface-container-lowest text-on-surface-variant border border-outline-variant hover:bg-surface-container hover:text-primary"
                }`}
              >
                <span className="material-symbols-outlined text-[20px]">{ind.icon}</span>
                {ind.name}
              </button>
            ))}
          </div>

          {/* Active Industry Panel */}
          <div className="bg-surface-container-lowest rounded-3xl border border-outline-variant p-8 md:p-12 shadow-sm">
            <div className="grid lg:grid-cols-12 gap-10 items-center">
              {/* Left Column: Details */}
              <div className="lg:col-span-7 space-y-6">
                <div className="inline-flex items-center gap-2 px-3 py-1 bg-secondary-container text-on-secondary-container rounded-full text-label-xs font-semibold">
                  <span className="material-symbols-outlined text-[15px]">verified</span>
                  {currentIndustry.badge}
                </div>

                <h3 className="text-2xl md:text-3xl font-bold text-on-background leading-snug">
                  {currentIndustry.title}
                </h3>

                <p className="text-on-surface-variant text-body-md leading-relaxed">
                  {currentIndustry.description}
                </p>

                {/* Feature Checklist */}
                <div className="space-y-3 pt-2">
                  {currentIndustry.features.map((feat, idx) => (
                    <div key={idx} className="flex items-start gap-3">
                      <div className="w-5 h-5 rounded-full bg-primary-fixed flex items-center justify-center text-primary mt-0.5 shrink-0">
                        <span className="material-symbols-outlined text-[14px]">check</span>
                      </div>
                      <span className="text-body-sm text-on-surface font-medium">{feat}</span>
                    </div>
                  ))}
                </div>

                {/* Quote Box */}
                <div className="p-5 bg-surface-container rounded-xl border-l-4 border-primary mt-6">
                  <p className="italic text-body-sm text-on-surface mb-3">
                    "{currentIndustry.quote.text}"
                  </p>
                  <div className="text-label-md font-bold text-primary">
                    {currentIndustry.quote.author}
                  </div>
                  <div className="text-label-xs text-on-surface-variant">
                    {currentIndustry.quote.role}
                  </div>
                </div>
              </div>

              {/* Right Column: Key Stats & Visual Card */}
              <div className="lg:col-span-5 bg-surface-container-low rounded-2xl p-6 md:p-8 border border-outline-variant flex flex-col justify-between space-y-8">
                <div>
                  <div className="text-label-xs font-bold text-on-surface-variant uppercase tracking-wider mb-6 flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-[18px]">
                      query_stats
                    </span>
                    Verified Benchmark Outcomes
                  </div>

                  <div className="space-y-5">
                    {currentIndustry.stats.map((st, i) => (
                      <div
                        key={i}
                        className="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant/60 shadow-xs flex items-center justify-between"
                      >
                        <span className="text-body-sm font-medium text-on-surface-variant">
                          {st.label}
                        </span>
                        <span className="text-2xl font-black text-primary">{st.value}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="pt-4 border-t border-outline-variant/70 flex flex-col sm:flex-row gap-3">
                  <button
                    onClick={() => navigate("/dashboard")}
                    className="w-full bg-primary text-on-primary font-semibold text-label-md py-3 rounded-lg hover:bg-primary-container hover:text-on-primary-container transition-colors flex items-center justify-center gap-2 cursor-pointer"
                  >
                    Launch Industry Demo
                    <span className="material-symbols-outlined text-[16px]">play_arrow</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ── 3. The 4-Step Supervised Architecture ── */}
        <section className="max-w-7xl mx-auto px-6 py-16">
          <div className="text-center mb-14">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-surface-container-high rounded-full border border-surface-variant mb-3 text-label-xs font-semibold text-primary uppercase tracking-wider">
              Architecture & Security
            </div>
            <h2 className="text-3xl font-bold text-on-background">
              The Hybrid Supervision Lifecycle
            </h2>
            <p className="mt-3 text-on-surface-variant text-body-md max-w-2xl mx-auto">
              How our system balances speed and strict accuracy across every user touchpoint.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {workflows.map((wf) => (
              <div
                key={wf.step}
                className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant shadow-sm hover:shadow-md transition-all relative flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-3xl font-black text-primary/30">{wf.step}</span>
                    <div className="w-10 h-10 rounded-xl bg-primary-fixed flex items-center justify-center text-primary">
                      <span className="material-symbols-outlined text-[20px]">{wf.icon}</span>
                    </div>
                  </div>
                  <div className="inline-block text-[11px] font-bold text-primary bg-primary-fixed/40 px-2.5 py-0.5 rounded-md mb-2">
                    {wf.tag}
                  </div>
                  <h4 className="text-headline-sm font-semibold text-on-background mb-2">
                    {wf.title}
                  </h4>
                  <p className="text-body-sm text-on-surface-variant">{wf.description}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* ── 4. Interactive ROI & Savings Calculator ── */}
        <section className="max-w-7xl mx-auto px-6 py-16">
          <div className="bg-gradient-to-br from-surface-container-lowest to-surface-container-low p-8 md:p-12 rounded-3xl border border-outline-variant shadow-lg">
            <div className="grid lg:grid-cols-12 gap-12 items-center">
              {/* Left Column: Sliders */}
              <div className="lg:col-span-6 space-y-8">
                <div>
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-primary-fixed text-primary rounded-full text-label-xs font-semibold mb-3">
                    <span className="material-symbols-outlined text-[15px]">calculate</span>
                    Interactive ROI Estimator
                  </div>
                  <h3 className="text-2xl md:text-3xl font-bold text-on-background">
                    Calculate Your Support Operations Savings
                  </h3>
                  <p className="mt-2 text-body-sm text-on-surface-variant">
                    Adjust your monthly volume and team size to forecast first-year efficiency gains and cost reductions.
                  </p>
                </div>

                {/* Slider 1: Monthly Tickets */}
                <div className="space-y-3 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/60">
                  <div className="flex justify-between items-center">
                    <label className="text-label-md font-semibold text-on-background">
                      Monthly Inquiries & Tickets
                    </label>
                    <span className="text-headline-sm font-bold text-primary">
                      {monthlyTickets.toLocaleString()}
                    </span>
                  </div>
                  <input
                    type="range"
                    min="2000"
                    max="100000"
                    step="1000"
                    value={monthlyTickets}
                    onChange={(e) => setMonthlyTickets(Number(e.target.value))}
                    className="w-full h-2 bg-surface-container-high rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                  <div className="flex justify-between text-label-xs text-on-surface-variant">
                    <span>2k tickets</span>
                    <span>50k tickets</span>
                    <span>100k+ tickets</span>
                  </div>
                </div>

                {/* Slider 2: Support Team Size */}
                <div className="space-y-3 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/60">
                  <div className="flex justify-between items-center">
                    <label className="text-label-md font-semibold text-on-background">
                      Current Support Agents
                    </label>
                    <span className="text-headline-sm font-bold text-primary">
                      {teamSize} Agents
                    </span>
                  </div>
                  <input
                    type="range"
                    min="2"
                    max="100"
                    step="1"
                    value={teamSize}
                    onChange={(e) => setTeamSize(Number(e.target.value))}
                    className="w-full h-2 bg-surface-container-high rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                  <div className="flex justify-between text-label-xs text-on-surface-variant">
                    <span>2 agents</span>
                    <span>50 agents</span>
                    <span>100+ agents</span>
                  </div>
                </div>
              </div>

              {/* Right Column: Calculated ROI Matrix */}
              <div className="lg:col-span-6 bg-primary text-on-primary rounded-2xl p-8 md:p-10 shadow-xl space-y-6">
                <div className="text-label-xs font-bold uppercase tracking-wider text-primary-fixed flex items-center gap-2">
                  <span className="material-symbols-outlined text-[18px]">trending_up</span>
                  Estimated Annual Value Generated
                </div>

                <div>
                  <div className="text-4xl md:text-5xl font-black text-on-primary tracking-tight">
                    ${annualDollarSavings.toLocaleString()}
                  </div>
                  <div className="text-body-sm text-primary-fixed mt-1">
                    Projected annual operational savings
                  </div>
                </div>

                <hr className="border-on-primary/20" />

                <div className="grid grid-cols-2 gap-6">
                  <div>
                    <div className="text-2xl font-bold text-on-primary">
                      {resolvedByAI.toLocaleString()}
                    </div>
                    <div className="text-label-xs text-primary-fixed">
                      Monthly tickets auto-deflected
                    </div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-on-primary">
                      {hoursSavedPerMonth.toLocaleString()} hrs
                    </div>
                    <div className="text-label-xs text-primary-fixed">
                      Agent hours saved per month
                    </div>
                  </div>
                </div>

                <div className="pt-2">
                  <button
                    onClick={() => navigate("/pricing")}
                    className="w-full bg-surface-container-lowest text-primary font-bold text-label-md py-3.5 rounded-xl hover:bg-primary-fixed transition-colors shadow-md flex items-center justify-center gap-2 cursor-pointer"
                  >
                    Unlock These Savings Today
                    <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ── 5. Enterprise Security & Integrations ── */}
        <section className="max-w-7xl mx-auto px-6 py-12">
          <div className="bg-surface-container rounded-2xl border border-outline-variant p-8 md:p-10 flex flex-col md:flex-row items-center justify-between gap-8">
            <div className="space-y-2 max-w-2xl">
              <div className="flex items-center gap-2 text-primary font-bold text-label-md">
                <span className="material-symbols-outlined text-[20px]">shield</span>
                Enterprise Trust, Privacy & Custom Deployments
              </div>
              <h3 className="text-xl md:text-2xl font-bold text-on-background">
                Connects directly to your Zendesk, Salesforce, Intercom & custom stack.
              </h3>
              <p className="text-body-sm text-on-surface-variant">
                Available as a secure cloud multi-tenant or dedicated single-tenant VPC deployment with custom SLA guarantees.
              </p>
            </div>
            <div className="flex gap-4 shrink-0">
              <button
                onClick={() => navigate("/dashboard")}
                className="bg-primary text-on-primary font-semibold text-label-md px-6 py-3 rounded-lg hover:bg-primary-container hover:text-on-primary-container transition-colors cursor-pointer"
              >
                Schedule Architecture Review
              </button>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}

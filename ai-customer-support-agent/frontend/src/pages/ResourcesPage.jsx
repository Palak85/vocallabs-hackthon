import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/common/Navbar";
import Footer from "../components/common/Footer";

const categories = [
  "All",
  "Guides & Whitepapers",
  "Case Studies",
  "Developer Docs",
  "Best Practices",
];

const allResources = [
  {
    id: 1,
    category: "Guides & Whitepapers",
    title: "The 2025 Supervised AI Support Benchmark Report",
    description:
      "A comprehensive analysis of over 4.2 million customer interactions across 250 enterprise support teams transitioning from dumb bots to supervised AI.",
    readTime: "12 min read",
    type: "Whitepaper",
    icon: "description",
    date: "August 2024",
    highlight: true,
  },
  {
    id: 2,
    category: "Best Practices",
    title: "The Human-in-the-Loop Escalation Playbook",
    description:
      "Proven frameworks for setting dynamic confidence thresholds, crafting silent agent whisper prompts, and eliminating customer friction during handoffs.",
    readTime: "8 min read",
    type: "Playbook",
    icon: "menu_book",
    date: "July 2024",
  },
  {
    id: 3,
    category: "Developer Docs",
    title: "Real-Time Voice & Webhook Integration Blueprint",
    description:
      "Step-by-step developer tutorial for streaming telephony audio into SuperviseAI's low-latency speech pipelines and CRM endpoints.",
    readTime: "15 min read",
    type: "Technical Guide",
    icon: "code",
    date: "August 2024",
  },
  {
    id: 4,
    category: "Case Studies",
    title: "How TrendVault Handled 350% Volume Growth with Zero New Hires",
    description:
      "Learn how a leading retail brand automated 78% of order queries while boosting their Net Promoter Score by 24 points.",
    readTime: "6 min read",
    type: "Case Study",
    icon: "trending_up",
    date: "June 2024",
  },
  {
    id: 5,
    category: "Developer Docs",
    title: "Architecting LLM Guardrails for High-Risk Financial Support",
    description:
      "Techniques for strict deterministic rule enforcement, automated PII sanitization, and real-time hallucination prevention.",
    readTime: "10 min read",
    type: "Whitepaper",
    icon: "security",
    date: "May 2024",
  },
  {
    id: 6,
    category: "Best Practices",
    title: "Continuous Knowledge Ingestion: Beyond Static FAQ Bots",
    description:
      "How to set up automated pipelines that synchronize your Notion, Zendesk Help Center, and Jira issue logs into a live vector index.",
    readTime: "7 min read",
    type: "Guide",
    icon: "sync_alt",
    date: "July 2024",
  },
];

const videoMasterclasses = [
  {
    title: "Live Agent Cockpit: 10-Minute Walkthrough",
    duration: "9:45 min",
    speaker: "Maya Lin, VP Product",
    topic: "Supervision Tools & Real-Time Handoff",
    views: "3.4k views",
  },
  {
    title: "Configuring Confidence Thresholds & Guardrails",
    duration: "14:20 min",
    speaker: "David Kim, Head of AI Research",
    topic: "Model Governance",
    views: "2.8k views",
  },
  {
    title: "Integrating Zendesk & Salesforce in Under 5 Minutes",
    duration: "6:15 min",
    speaker: "Alex Rivera, Solutions Architect",
    topic: "Integrations & APIs",
    views: "5.1k views",
  },
];

const faqs = [
  {
    q: "How does human supervision prevent AI hallucinations?",
    a: "SuperviseAI utilizes multi-tier verification: strict retrieval-augmented guardrails prevent the model from generating unsourced claims, and any confidence score below your preset threshold automatically triggers a silent agent whisper or direct human handoff.",
  },
  {
    q: "Can we connect our existing knowledge bases and CRM tools?",
    a: "Yes! We support one-click sync connectors for Zendesk, Salesforce Service Cloud, Intercom, Freshdesk, Notion, Confluence, and custom REST API/GraphQL endpoints.",
  },
  {
    q: "Is our customer data used to train public foundation models?",
    a: "Never. All customer data and transcripts remain strictly isolated within your private dedicated workspace. We comply with SOC2 Type II, HIPAA, and GDPR standards with zero data retention for model vendors.",
  },
  {
    q: "How quickly can we roll out SuperviseAI to our existing support team?",
    a: "Most teams are up and running in under 24 hours. You can connect your knowledge base, test sample queries in sandbox mode, and begin with silent supervisor mode without disrupting active customer workflows.",
  },
  {
    q: "Does SuperviseAI support voice telephony calls as well as text chat?",
    a: "Yes, our voice pipelines offer ultra-low latency (<350ms) speech-to-speech with natural interruptions, real-time telephony webhooks (Twilio, Amazon Connect), and smooth transfers to PBX phone trees.",
  },
];

export default function ResourcesPage() {
  const navigate = useNavigate();
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchQuery, setSearchQuery] = useState("");
  const [openFaqIndex, setOpenFaqIndex] = useState(0);
  const [emailSubscribed, setEmailSubscribed] = useState(false);
  const [emailInput, setEmailInput] = useState("");

  const filteredResources = allResources.filter((item) => {
    const matchesCategory =
      selectedCategory === "All" || item.category === selectedCategory;
    const matchesSearch =
      item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.category.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const handleSubscribe = (e) => {
    e.preventDefault();
    if (emailInput.trim()) {
      setEmailSubscribed(true);
    }
  };

  return (
    <div className="bg-background text-on-background min-h-screen">
      <Navbar />

      <main className="pt-24 pb-20">
        {/* ── 1. Hero Section & Search ── */}
        <section className="max-w-7xl mx-auto px-6 py-12 md:py-16 text-center">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-high rounded-full border border-surface-variant mb-6 shadow-sm">
            <span className="material-symbols-outlined text-[16px] text-primary">
              auto_stories
            </span>
            <span className="text-label-xs font-semibold text-primary uppercase tracking-wider">
              Knowledge Hub & Developer Guides
            </span>
          </div>

          <h1 className="text-4xl md:text-5xl font-extrabold text-on-background tracking-tight max-w-3xl mx-auto leading-tight">
            Master Supervised AI Customer Operations.
          </h1>

          <p className="mt-4 text-lg text-on-surface-variant max-w-2xl mx-auto">
            Deep dive into operational blueprints, developer documentation, benchmark research, and best practices for human-AI support teams.
          </p>

          {/* Search Bar */}
          <div className="mt-8 max-w-xl mx-auto relative">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">
              search
            </span>
            <input
              type="text"
              placeholder="Search guides, docs, case studies & playbooks..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-3.5 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-xs text-body-md focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all"
            />
          </div>

          {/* Category Filter Pills */}
          <div className="mt-6 flex flex-wrap justify-center gap-2">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-4 py-2 rounded-lg text-body-sm font-semibold transition-all cursor-pointer ${
                  selectedCategory === cat
                    ? "bg-primary text-on-primary shadow-xs"
                    : "bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface border border-outline-variant/60"
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </section>

        {/* ── 2. Featured Resource Card ── */}
        <section className="max-w-7xl mx-auto px-6 py-6">
          <div className="bg-gradient-to-r from-primary to-secondary text-on-primary rounded-3xl p-8 md:p-12 shadow-xl relative overflow-hidden">
            <div className="relative z-10 max-w-2xl space-y-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 bg-surface-container-lowest/20 backdrop-blur-md rounded-full text-label-xs font-semibold text-primary-fixed">
                <span className="material-symbols-outlined text-[14px]">star</span>
                Featured Flagship Research
              </div>
              <h2 className="text-2xl md:text-4xl font-extrabold leading-tight">
                The 2025 Supervised AI Support Benchmark Report
              </h2>
              <p className="text-on-primary/80 text-body-md">
                Discover how top organizations achieve a 75%+ resolution rate while cutting human burnout and maintaining 4.8+ CSAT scores.
              </p>
              <div className="pt-2 flex flex-wrap gap-4 items-center">
                <button
                  onClick={() => navigate("/dashboard")}
                  className="bg-surface-container-lowest text-primary font-bold text-label-md px-6 py-3 rounded-lg shadow-md hover:bg-primary-fixed transition-colors flex items-center gap-2 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[18px]">download</span>
                  Download Free Report (PDF)
                </button>
                <span className="text-label-xs text-on-primary/70">
                  Updated for Q3 2024 • 38 Pages
                </span>
              </div>
            </div>
          </div>
        </section>

        {/* ── 3. Filtered Resource Grid ── */}
        <section className="max-w-7xl mx-auto px-6 py-12">
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-2xl font-bold text-on-background">
              Articles & Guides ({filteredResources.length})
            </h2>
            {searchQuery && (
              <button
                onClick={() => setSearchQuery("")}
                className="text-label-xs text-primary font-semibold hover:underline cursor-pointer"
              >
                Clear Search
              </button>
            )}
          </div>

          {filteredResources.length === 0 ? (
            <div className="text-center py-16 bg-surface-container-lowest rounded-2xl border border-outline-variant">
              <span className="material-symbols-outlined text-4xl text-on-surface-variant mb-2">
                search_off
              </span>
              <p className="text-body-md text-on-surface-variant">
                No resources found matching "{searchQuery}". Try a different keyword.
              </p>
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredResources.map((res) => (
                <div
                  key={res.id}
                  className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-6 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
                >
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-label-xs font-semibold px-2.5 py-1 bg-surface-container-high text-primary rounded-md">
                        {res.type}
                      </span>
                      <span className="text-label-xs text-on-surface-variant">
                        {res.readTime}
                      </span>
                    </div>

                    <h3 className="text-headline-sm font-semibold text-on-background leading-snug">
                      {res.title}
                    </h3>

                    <p className="text-body-sm text-on-surface-variant line-clamp-3">
                      {res.description}
                    </p>
                  </div>

                  <div className="pt-6 border-t border-outline-variant/60 mt-6 flex items-center justify-between">
                    <span className="text-label-xs text-on-surface-variant font-medium">
                      {res.date}
                    </span>
                    <button
                      onClick={() => navigate("/dashboard")}
                      className="text-primary font-semibold text-label-md hover:underline flex items-center gap-1 cursor-pointer"
                    >
                      Read Now
                      <span className="material-symbols-outlined text-[16px]">
                        arrow_forward
                      </span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ── 4. Video Masterclasses & Hands-On Demos ── */}
        <section className="max-w-7xl mx-auto px-6 py-12">
          <div className="text-center mb-10">
            <h2 className="text-2xl md:text-3xl font-bold text-on-background">
              Video Masterclasses & Tutorials
            </h2>
            <p className="mt-2 text-on-surface-variant text-body-md">
              Watch step-by-step implementation walkthroughs by our solutions architects.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            {videoMasterclasses.map((vid, i) => (
              <div
                key={i}
                className="bg-surface-container-lowest rounded-2xl border border-outline-variant overflow-hidden shadow-xs hover:shadow-md transition-shadow group cursor-pointer"
                onClick={() => navigate("/dashboard")}
              >
                {/* Thumbnail / Video header */}
                <div className="bg-surface-container-high h-44 relative flex items-center justify-center">
                  <div className="w-14 h-14 rounded-full bg-primary text-on-primary flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                    <span className="material-symbols-outlined text-[28px]">
                      play_arrow
                    </span>
                  </div>
                  <span className="absolute bottom-3 right-3 bg-inverse-surface/80 text-inverse-on-surface text-label-xs font-semibold px-2 py-1 rounded">
                    {vid.duration}
                  </span>
                </div>

                <div className="p-6 space-y-2">
                  <span className="text-label-xs text-primary font-semibold">
                    {vid.topic}
                  </span>
                  <h3 className="text-headline-sm font-semibold text-on-background group-hover:text-primary transition-colors">
                    {vid.title}
                  </h3>
                  <div className="text-label-xs text-on-surface-variant pt-2 flex justify-between">
                    <span>{vid.speaker}</span>
                    <span>{vid.views}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* ── 5. Interactive FAQ Accordion ── */}
        <section className="max-w-4xl mx-auto px-6 py-16">
          <div className="text-center mb-12">
            <h2 className="text-2xl md:text-3xl font-bold text-on-background">
              Frequently Asked Questions
            </h2>
            <p className="mt-2 text-on-surface-variant text-body-md">
              Everything you need to know about our supervised customer AI system.
            </p>
          </div>

          <div className="space-y-4">
            {faqs.map((faq, index) => {
              const isOpen = openFaqIndex === index;
              return (
                <div
                  key={index}
                  className="bg-surface-container-lowest rounded-2xl border border-outline-variant overflow-hidden transition-all shadow-xs"
                >
                  <button
                    onClick={() => setOpenFaqIndex(isOpen ? -1 : index)}
                    className="w-full text-left p-6 flex justify-between items-center gap-4 cursor-pointer"
                  >
                    <span className="text-headline-sm font-semibold text-on-background">
                      {faq.q}
                    </span>
                    <span className="material-symbols-outlined text-primary text-[24px] shrink-0">
                      {isOpen ? "expand_less" : "expand_more"}
                    </span>
                  </button>

                  {isOpen && (
                    <div className="px-6 pb-6 pt-0 text-body-md text-on-surface-variant leading-relaxed border-t border-outline-variant/40 pt-4">
                      {faq.a}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        {/* ── 6. Newsletter / Updates Subscription ── */}
        <section className="max-w-7xl mx-auto px-6 py-8">
          <div className="bg-surface-container rounded-3xl border border-outline-variant p-8 md:p-12 text-center max-w-3xl mx-auto">
            <span className="material-symbols-outlined text-3xl text-primary mb-2">
              mark_email_read
            </span>
            <h3 className="text-2xl font-bold text-on-background">
              Get Weekly Supervised AI Insights
            </h3>
            <p className="text-body-sm text-on-surface-variant mt-2 max-w-md mx-auto">
              Join 12,000+ support leaders getting our weekly breakdown on LLM prompt recipes, escalation patterns, and benchmark data.
            </p>

            {emailSubscribed ? (
              <div className="mt-6 inline-flex items-center gap-2 px-5 py-3 bg-secondary-container text-on-secondary-container rounded-xl font-semibold text-body-sm">
                <span className="material-symbols-outlined text-[18px]">check_circle</span>
                Thank you! You're subscribed to SuperviseAI Weekly.
              </div>
            ) : (
              <form onSubmit={handleSubscribe} className="mt-6 flex flex-col sm:flex-row gap-3 justify-center max-w-md mx-auto">
                <input
                  type="email"
                  required
                  placeholder="Enter your work email"
                  value={emailInput}
                  onChange={(e) => setEmailInput(e.target.value)}
                  className="px-4 py-3 bg-surface-container-lowest border border-outline-variant rounded-xl text-body-sm focus:outline-none focus:border-primary grow"
                />
                <button
                  type="submit"
                  className="bg-primary text-on-primary font-semibold text-label-md px-6 py-3 rounded-xl hover:bg-primary-container hover:text-on-primary-container transition-colors cursor-pointer shrink-0"
                >
                  Subscribe
                </button>
              </form>
            )}
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}

import React, { useState, Fragment } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/common/Navbar";
import Footer from "../components/common/Footer";

export default function PricingPage() {
  const navigate = useNavigate();
  const [annualBilling, setAnnualBilling] = useState(true);
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [quoteSubmitted, setQuoteSubmitted] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    company: "",
    volume: "10,000 - 50,000 / mo",
    message: "",
  });

  const plans = [
    {
      id: "starter",
      name: "Starter",
      badge: "Fast-Moving Startups",
      description: "Essential supervised AI capabilities for growing teams looking to automate routine customer queries.",
      monthlyPrice: 49,
      annualPrice: 39,
      popular: false,
      ctaText: "Start 14-Day Free Trial",
      features: [
        "Up to 3,000 conversations / month",
        "3 Human Supervisor seats included",
        "Web Chat Widget & Email channels",
        "Real-Time Human Escalation trigger",
        "Standard Notion & Zendesk sync",
        "Business hours email support (12h SLA)",
        "Standard 99.5% uptime SLA",
      ],
      notIncluded: [
        "Voice & telephony streams",
        "Silent agent whisper copilot",
        "Custom single-tenant VPC deployment",
      ],
    },
    {
      id: "pro",
      name: "Pro & Scale",
      badge: "Most Popular",
      description: "Full-featured omnichannel AI copilot & supervisor for high-volume, scaling customer support organizations.",
      monthlyPrice: 149,
      annualPrice: 119,
      popular: true,
      ctaText: "Get Started Free",
      features: [
        "Up to 20,000 conversations / month",
        "10 Human Supervisor seats included",
        "Omnichannel: Web, Email, SMS, WhatsApp & Voice",
        "Live Agent Whisper & Auto-Drafting Copilot",
        "Zero-Context-Loss Warm Handoff Cockpit",
        "Zendesk, Salesforce, Freshdesk & HubSpot sync",
        "Continuous Knowledge Base fine-tuning",
        "Priority 24/7 support (2h SLA)",
        "99.9% uptime SLA guarantee",
      ],
      notIncluded: ["Custom single-tenant VPC deployment"],
    },
    {
      id: "enterprise",
      name: "Enterprise",
      badge: "Custom Guardrails & Scale",
      description: "Dedicated single-tenant infrastructure, custom LLM fine-tuning, and strict compliance for global enterprises.",
      monthlyPrice: 499,
      annualPrice: 399,
      popular: false,
      ctaText: "Contact Enterprise Sales",
      isCustom: true,
      features: [
        "Unlimited conversations & volume scale",
        "Unlimited Supervisor seats",
        "Dedicated LLM fine-tuning on custom historical tickets",
        "Single-tenant VPC or On-Prem deployment options",
        "SOC2 Type II, HIPAA & PCI-DSS compliance guarantees",
        "Biometric step-up & bank-grade PII data sanitization",
        "Dedicated Solutions Architect & Technical Account Manager",
        "99.99% custom uptime SLA with financial backing",
      ],
      notIncluded: [],
    },
  ];

  const comparisonCategories = [
    {
      category: "Core AI & Deflection",
      rows: [
        { feature: "Monthly Included Conversations", starter: "3,000", pro: "20,000", enterprise: "Unlimited" },
        { feature: "Confidence-Based Auto Resolution", starter: true, pro: true, enterprise: true },
        { feature: "Continuous Knowledge Base Sync", starter: "Daily", pro: "Real-time", enterprise: "Real-time + Custom ETL" },
        { feature: "Custom Model Fine-Tuning", starter: false, pro: false, enterprise: true },
      ],
    },
    {
      category: "Human Supervision & Copilot",
      rows: [
        { feature: "Supervisor Agent Seats", starter: "3 seats", pro: "10 seats", enterprise: "Custom / Unlimited" },
        { feature: "Live Monitoring & Takeover Cockpit", starter: true, pro: true, enterprise: true },
        { feature: "Silent Agent Whisper Suggestions", starter: false, pro: true, enterprise: true },
        { feature: "Automated Ticket Synthesis (Jira/Linear)", starter: false, pro: true, enterprise: true },
      ],
    },
    {
      category: "Omnichannel & Voice",
      rows: [
        { feature: "Web Chat & Email Support", starter: true, pro: true, enterprise: true },
        { feature: "SMS & WhatsApp Connectors", starter: false, pro: true, enterprise: true },
        { feature: "Real-Time Voice Telephony (<350ms)", starter: false, pro: true, enterprise: true },
      ],
    },
    {
      category: "Security, Compliance & Support",
      rows: [
        { feature: "PII & Masking Guardrails", starter: "Standard", pro: "Advanced", enterprise: "Bank-Grade / Custom" },
        { feature: "SOC2 Type II & HIPAA Compliance", starter: false, pro: true, enterprise: true },
        { feature: "Deployment Options", starter: "Cloud Multi-Tenant", pro: "Cloud Multi-Tenant", enterprise: "Dedicated VPC / On-Prem" },
        { feature: "Uptime SLA", starter: "99.5%", pro: "99.9%", enterprise: "99.99% Dedicated" },
      ],
    },
  ];

  const handleFormSubmit = (e) => {
    e.preventDefault();
    setQuoteSubmitted(true);
  };

  return (
    <div className="bg-background text-on-background min-h-screen">
      <Navbar />

      <main className="pt-24 pb-20">
        {/* ── 1. Hero & Billing Switcher ── */}
        <section className="max-w-7xl mx-auto px-6 py-12 md:py-16 text-center">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-high rounded-full border border-surface-variant mb-6 shadow-sm">
            <span className="material-symbols-outlined text-[16px] text-primary">
              sell
            </span>
            <span className="text-label-xs font-semibold text-primary uppercase tracking-wider">
              Transparent, Scalable Pricing
            </span>
          </div>

          <h1 className="text-4xl md:text-5xl font-extrabold text-on-background tracking-tight max-w-3xl mx-auto leading-tight">
            Supercharge Support. Pay Only For What You Need.
          </h1>

          <p className="mt-4 text-lg text-on-surface-variant max-w-2xl mx-auto">
            Choose the right plan to give your team real-time AI supervision, instant escalation guardrails, and seamless multichannel routing.
          </p>

          {/* Billing Switcher Toggle */}
          <div className="mt-8 inline-flex items-center gap-3 bg-surface-container-lowest p-1.5 rounded-2xl border border-outline-variant shadow-xs">
            <button
              onClick={() => setAnnualBilling(false)}
              className={`px-5 py-2 rounded-xl text-body-sm font-semibold transition-all cursor-pointer ${
                !annualBilling
                  ? "bg-primary text-on-primary shadow-xs"
                  : "text-on-surface-variant hover:text-on-background"
              }`}
            >
              Monthly Billing
            </button>
            <button
              onClick={() => setAnnualBilling(true)}
              className={`px-5 py-2 rounded-xl text-body-sm font-semibold transition-all flex items-center gap-2 cursor-pointer ${
                annualBilling
                  ? "bg-primary text-on-primary shadow-xs"
                  : "text-on-surface-variant hover:text-on-background"
              }`}
            >
              <span>Annual Billing</span>
              <span className="bg-secondary-container text-on-secondary-container text-label-xs font-bold px-2 py-0.5 rounded-full">
                Save 20%
              </span>
            </button>
          </div>
        </section>

        {/* ── 2. Pricing Tier Cards ── */}
        <section className="max-w-7xl mx-auto px-6 py-6">
          <div className="grid lg:grid-cols-3 gap-8 items-stretch">
            {plans.map((plan) => {
              const price = annualBilling ? plan.annualPrice : plan.monthlyPrice;

              return (
                <div
                  key={plan.id}
                  className={`relative rounded-3xl p-8 transition-all flex flex-col justify-between ${
                    plan.popular
                      ? "bg-surface-container-lowest border-2 border-primary shadow-xl scale-100 lg:-translate-y-2 ring-4 ring-primary/10"
                      : "bg-surface-container-lowest border border-outline-variant shadow-sm hover:shadow-md"
                  }`}
                >
                  {plan.popular && (
                    <div className="absolute -top-4 left-1/2 -translate-x-1/2 bg-primary text-on-primary text-label-xs font-bold px-4 py-1.5 rounded-full uppercase tracking-wider shadow-md">
                      {plan.badge}
                    </div>
                  )}

                  <div>
                    {!plan.popular && (
                      <span className="text-label-xs font-bold text-primary uppercase tracking-wider">
                        {plan.badge}
                      </span>
                    )}

                    <h3 className="text-2xl font-extrabold text-on-background mt-2">
                      {plan.name}
                    </h3>

                    <p className="text-body-sm text-on-surface-variant mt-2 mb-6">
                      {plan.description}
                    </p>

                    {/* Price display */}
                    <div className="mb-6">
                      <div className="flex items-baseline gap-1">
                        <span className="text-4xl md:text-5xl font-black text-on-background">
                          ${price}
                        </span>
                        <span className="text-on-surface-variant text-body-sm font-medium">
                          / month
                        </span>
                      </div>
                      <span className="text-label-xs text-on-surface-variant">
                        {annualBilling ? "Billed annually" : "Billed monthly"}
                      </span>
                    </div>

                    {/* CTA Button */}
                    <button
                      onClick={() => {
                        if (plan.isCustom) {
                          setShowQuoteModal(true);
                        } else {
                          navigate("/dashboard");
                        }
                      }}
                      className={`w-full py-3.5 px-6 rounded-xl font-bold text-label-md transition-all flex items-center justify-center gap-2 cursor-pointer mb-8 ${
                        plan.popular
                          ? "bg-primary text-on-primary hover:bg-primary-container hover:text-on-primary-container shadow-md shadow-primary/20"
                          : "bg-surface-container text-on-surface hover:bg-surface-variant border border-outline-variant"
                      }`}
                    >
                      {plan.ctaText}
                      <span className="material-symbols-outlined text-[18px]">
                        arrow_forward
                      </span>
                    </button>

                    {/* Features Checklist */}
                    <div className="space-y-3">
                      <div className="text-label-xs font-bold text-on-surface-variant uppercase tracking-wider">
                        Included in this plan:
                      </div>
                      {plan.features.map((feat, idx) => (
                        <div key={idx} className="flex items-start gap-3">
                          <span className="material-symbols-outlined text-primary text-[18px] shrink-0 mt-0.5">
                            check_circle
                          </span>
                          <span className="text-body-sm text-on-surface font-medium">
                            {feat}
                          </span>
                        </div>
                      ))}

                      {plan.notIncluded.map((notFeat, idx) => (
                        <div
                          key={idx}
                          className="flex items-start gap-3 opacity-40"
                        >
                          <span className="material-symbols-outlined text-on-surface-variant text-[18px] shrink-0 mt-0.5">
                            cancel
                          </span>
                          <span className="text-body-sm text-on-surface-variant line-through">
                            {notFeat}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* ── 3. Feature Comparison Table ── */}
        <section className="max-w-7xl mx-auto px-6 py-16">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-on-background">
              Compare Plan Capabilities
            </h2>
            <p className="mt-2 text-on-surface-variant text-body-md">
              A detailed breakdown of all features, limits, and service level commitments.
            </p>
          </div>

          <div className="bg-surface-container-lowest rounded-3xl border border-outline-variant overflow-hidden shadow-xs">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-surface-container-high border-b border-outline-variant">
                    <th className="py-4 px-6 text-label-md font-bold text-on-background w-2/5">
                      Features
                    </th>
                    <th className="py-4 px-6 text-label-md font-bold text-on-background text-center w-1/5">
                      Starter
                    </th>
                    <th className="py-4 px-6 text-label-md font-bold text-primary text-center w-1/5">
                      Pro & Scale
                    </th>
                    <th className="py-4 px-6 text-label-md font-bold text-on-background text-center w-1/5">
                      Enterprise
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/60">
                  {comparisonCategories.map((cat, catIdx) => (
                    <React.Fragment key={catIdx}>
                      <tr className="bg-surface-container/50">
                        <td
                          colSpan={4}
                          className="py-3 px-6 text-label-xs font-bold uppercase tracking-wider text-primary"
                        >
                          {cat.category}
                        </td>
                      </tr>
                      {cat.rows.map((row, rowIdx) => (
                        <tr
                          key={rowIdx}
                          className="hover:bg-surface-container-low transition-colors"
                        >
                          <td className="py-4 px-6 text-body-sm font-medium text-on-background">
                            {row.feature}
                          </td>
                          <td className="py-4 px-6 text-center text-body-sm text-on-surface-variant">
                            {typeof row.starter === "boolean" ? (
                              row.starter ? (
                                <span className="material-symbols-outlined text-primary text-[20px]">
                                  check
                                </span>
                              ) : (
                                <span className="material-symbols-outlined text-on-surface-variant/40 text-[20px]">
                                  remove
                                </span>
                              )
                            ) : (
                              row.starter
                            )}
                          </td>
                          <td className="py-4 px-6 text-center text-body-sm font-semibold text-primary">
                            {typeof row.pro === "boolean" ? (
                              row.pro ? (
                                <span className="material-symbols-outlined text-primary text-[20px]">
                                  check
                                </span>
                              ) : (
                                <span className="material-symbols-outlined text-on-surface-variant/40 text-[20px]">
                                  remove
                                </span>
                              )
                            ) : (
                              row.pro
                            )}
                          </td>
                          <td className="py-4 px-6 text-center text-body-sm font-semibold text-on-background">
                            {typeof row.enterprise === "boolean" ? (
                              row.enterprise ? (
                                <span className="material-symbols-outlined text-primary text-[20px]">
                                  check
                                </span>
                              ) : (
                                <span className="material-symbols-outlined text-on-surface-variant/40 text-[20px]">
                                  remove
                                </span>
                              )
                            ) : (
                              row.enterprise
                            )}
                          </td>
                        </tr>
                      ))}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        {/* ── 4. Trust Badges & Guarantee ── */}
        <section className="max-w-7xl mx-auto px-6 py-8">
          <div className="grid md:grid-cols-3 gap-6 text-center">
            <div className="p-6 bg-surface-container-lowest rounded-2xl border border-outline-variant">
              <span className="material-symbols-outlined text-3xl text-primary mb-2">
                schedule
              </span>
              <h4 className="font-bold text-on-background mb-1">14-Day Free Trial</h4>
              <p className="text-label-xs text-on-surface-variant">
                Full access to Pro features. No credit card required to start.
              </p>
            </div>
            <div className="p-6 bg-surface-container-lowest rounded-2xl border border-outline-variant">
              <span className="material-symbols-outlined text-3xl text-primary mb-2">
                lock_clock
              </span>
              <h4 className="font-bold text-on-background mb-1">Cancel Anytime</h4>
              <p className="text-label-xs text-on-surface-variant">
                No locked-in contracts. Upgrade, downgrade or cancel with 1 click.
              </p>
            </div>
            <div className="p-6 bg-surface-container-lowest rounded-2xl border border-outline-variant">
              <span className="material-symbols-outlined text-3xl text-primary mb-2">
                verified_user
              </span>
              <h4 className="font-bold text-on-background mb-1">Enterprise Compliance</h4>
              <p className="text-label-xs text-on-surface-variant">
                SOC2 Type II, HIPAA & GDPR-certified data pipelines.
              </p>
            </div>
          </div>
        </section>

        {/* ── 5. Enterprise Custom Quote Modal ── */}
        {showQuoteModal && (
          <div className="fixed inset-0 bg-inverse-surface/50 backdrop-blur-xs z-50 flex items-center justify-center p-4">
            <div className="bg-surface-container-lowest rounded-3xl border border-outline-variant max-w-lg w-full p-8 shadow-2xl relative">
              <button
                onClick={() => {
                  setShowQuoteModal(false);
                  setQuoteSubmitted(false);
                }}
                className="absolute top-6 right-6 text-on-surface-variant hover:text-on-background cursor-pointer"
              >
                <span className="material-symbols-outlined">close</span>
              </button>

              {quoteSubmitted ? (
                <div className="text-center py-8 space-y-4">
                  <span className="material-symbols-outlined text-5xl text-primary">
                    check_circle
                  </span>
                  <h3 className="text-2xl font-bold text-on-background">
                    Request Received!
                  </h3>
                  <p className="text-body-sm text-on-surface-variant">
                    Our Enterprise Solutions Architect will review your requirements and reach out within 2 business hours.
                  </p>
                  <button
                    onClick={() => {
                      setShowQuoteModal(false);
                      setQuoteSubmitted(false);
                    }}
                    className="mt-4 bg-primary text-on-primary px-6 py-2.5 rounded-xl font-semibold text-label-md cursor-pointer"
                  >
                    Done
                  </button>
                </div>
              ) : (
                <form onSubmit={handleFormSubmit} className="space-y-4">
                  <div className="flex items-center gap-2 text-primary font-bold text-label-md">
                    <span className="material-symbols-outlined">corporate_fare</span>
                    Custom Enterprise Quote
                  </div>
                  <h3 className="text-2xl font-bold text-on-background">
                    Tailor SuperviseAI For Your Organization
                  </h3>
                  <p className="text-body-sm text-on-surface-variant">
                    Get custom SLAs, VPC deployment options, and volume pricing discounts.
                  </p>

                  <div className="space-y-3 pt-2">
                    <div>
                      <label className="text-label-xs font-semibold text-on-surface-variant block mb-1">
                        Full Name
                      </label>
                      <input
                        type="text"
                        required
                        placeholder="Alex Morgan"
                        value={formData.name}
                        onChange={(e) =>
                          setFormData({ ...formData, name: e.target.value })
                        }
                        className="w-full px-4 py-2.5 bg-surface-container-low border border-outline-variant rounded-xl text-body-sm focus:outline-none focus:border-primary"
                      />
                    </div>
                    <div>
                      <label className="text-label-xs font-semibold text-on-surface-variant block mb-1">
                        Work Email
                      </label>
                      <input
                        type="email"
                        required
                        placeholder="alex@company.com"
                        value={formData.email}
                        onChange={(e) =>
                          setFormData({ ...formData, email: e.target.value })
                        }
                        className="w-full px-4 py-2.5 bg-surface-container-low border border-outline-variant rounded-xl text-body-sm focus:outline-none focus:border-primary"
                      />
                    </div>
                    <div>
                      <label className="text-label-xs font-semibold text-on-surface-variant block mb-1">
                        Company Name
                      </label>
                      <input
                        type="text"
                        required
                        placeholder="Acme Corp"
                        value={formData.company}
                        onChange={(e) =>
                          setFormData({ ...formData, company: e.target.value })
                        }
                        className="w-full px-4 py-2.5 bg-surface-container-low border border-outline-variant rounded-xl text-body-sm focus:outline-none focus:border-primary"
                      />
                    </div>
                    <div>
                      <label className="text-label-xs font-semibold text-on-surface-variant block mb-1">
                        Monthly Support Volume
                      </label>
                      <select
                        value={formData.volume}
                        onChange={(e) =>
                          setFormData({ ...formData, volume: e.target.value })
                        }
                        className="w-full px-4 py-2.5 bg-surface-container-low border border-outline-variant rounded-xl text-body-sm focus:outline-none focus:border-primary"
                      >
                        <option>10,000 - 50,000 / mo</option>
                        <option>50,000 - 250,000 / mo</option>
                        <option>250,000+ / mo</option>
                      </select>
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="w-full mt-4 bg-primary text-on-primary font-bold text-label-md py-3 rounded-xl hover:bg-primary-container hover:text-on-primary-container transition-colors cursor-pointer"
                  >
                    Submit Enterprise Request
                  </button>
                </form>
              )}
            </div>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}

const features = [
  {
    icon: "visibility",
    title: "Supervised Intelligence",
    description:
      "AI that's monitored by your team in real-time. Catch edge cases before they escalate and train the model naturally.",
    iconBg: "bg-primary-fixed",
    iconColor: "text-primary",
  },
  {
    icon: "swap_calls",
    title: "Instant Escalation",
    description:
      "Smooth transitions from bot to human without losing context. The agent steps right in where the AI left off.",
    iconBg: "bg-secondary-container",
    iconColor: "text-on-secondary-container",
  },
  {
    icon: "record_voice_over",
    title: "Voice & Text Native",
    description:
      "Support customers wherever they are, with the same high quality across chat widgets, SMS, and voice calls.",
    iconBg: "bg-tertiary-fixed",
    iconColor: "text-tertiary",
  },
];

export default function Features() {
  return (
    <section className="max-w-7xl mx-auto px-6 py-16">
      {/* Section Heading */}
      <div className="text-center mb-12">
        <h2 className="text-3xl font-bold text-on-background">
          The Luminous Difference
        </h2>
        <p className="mt-4 text-on-surface-variant text-body-md max-w-2xl mx-auto">
          Built for teams that value quality over pure deflection. Our hybrid
          approach ensures you never compromise on customer satisfaction.
        </p>
      </div>

      {/* Feature Cards */}
      <div className="grid md:grid-cols-3 gap-6">
        {features.map((f) => (
          <div
            key={f.title}
            className="bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant shadow-sm hover:shadow-md transition-shadow"
          >
            <div
              className={`w-12 h-12 ${f.iconBg} rounded-lg flex items-center justify-center mb-6 ${f.iconColor}`}
            >
              <span className="material-symbols-outlined">{f.icon}</span>
            </div>
            <h3 className="text-headline-sm font-semibold text-on-background mb-3">
              {f.title}
            </h3>
            <p className="text-body-sm text-on-surface-variant">
              {f.description}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
}

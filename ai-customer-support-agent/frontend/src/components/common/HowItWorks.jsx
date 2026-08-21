const steps = [
  {
    number: 1,
    title: "Automated Resolution",
    description:
      "AI handles routine queries instantly, pulling from your knowledge base with high accuracy.",
  },
  {
    number: 2,
    title: "Human Supervision",
    description:
      "Supervisors monitor complex interactions via the dashboard, ensuring tone and accuracy.",
  },
  {
    number: 3,
    title: "Seamless Handoff",
    description:
      "Smooth transition to live agents with full context for zero-repeat, empathetic experiences.",
  },
];

export default function HowItWorks() {
  return (
    <section className="bg-surface-container-low py-20 border-y border-outline-variant">
      <div className="max-w-7xl mx-auto px-6">
        {/* Section Heading */}
        <div className="text-center mb-16">
          <h2 className="text-3xl font-bold text-on-background">
            How It Works
          </h2>
          <p className="mt-4 text-on-surface-variant text-body-md max-w-2xl mx-auto">
            A seamless choreography between artificial intelligence and human
            empathy.
          </p>
        </div>

        {/* Steps */}
        <div className="grid md:grid-cols-3 gap-8 relative">
          {/* Connecting line (desktop only) */}
          <div className="hidden md:block absolute top-1/2 left-0 w-full h-[2px] bg-outline-variant/30 -z-10 -translate-y-1/2" />

          {steps.map((s) => (
            <div
              key={s.number}
              className="bg-surface p-6 rounded-xl shadow-sm border border-outline-variant relative z-10 flex flex-col items-center text-center"
            >
              <div className="w-10 h-10 bg-primary text-on-primary rounded-full flex items-center justify-center font-bold mb-4 ring-4 ring-surface-container-low">
                {s.number}
              </div>
              <h4 className="text-headline-sm font-semibold text-on-background mb-2">
                {s.title}
              </h4>
              <p className="text-body-sm text-on-surface-variant">
                {s.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default function Testimonial() {
  return (
    <section className="max-w-4xl mx-auto px-6 py-20 text-center">
      {/* Quote Icon */}
      <span className="material-symbols-outlined text-4xl text-primary/40 mb-6 block">
        format_quote
      </span>

      {/* Quote */}
      <blockquote className="text-2xl font-medium text-on-background leading-relaxed mb-8">
        &ldquo;Implementing Luminous Support completely transformed our CS
        operations. We kept the personal touch our brand is known for, while
        resolution times dropped by 40%.&rdquo;
      </blockquote>

      {/* Author */}
      <div className="flex items-center justify-center gap-4">
        <div className="w-12 h-12 rounded-full overflow-hidden border-2 border-primary-fixed">
          <img
            className="w-full h-full object-cover"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuDUFFJjjW8xMAuL83yfkmtoHeHzpMXfrq45kdykpEHF21TYi1SS6Dcv3TMxSLveZPSVQLfuercr5aoRJ8xumkMmb65eRUiukmbsATtvq-X-ve0vvdgV8lDLxuQnu4SeJgJeQ_IhqI50OLzTNQCEJBi8hxR5ozrP4HZa3lomcWQKGx5rv6BkEEkbVvqNFN41o1iN2waOGYV8i38WCdfg4DAREy-FgEwq4yY4InUJXZ5e7_dRaHVRt6yC"
            alt="Sarah Jenkins, Customer Success Director at Acme Corp"
          />
        </div>
        <div className="text-left">
          <div className="text-label-md font-semibold text-on-background">
            Sarah Jenkins
          </div>
          <div className="text-body-sm text-on-surface-variant">
            Customer Success Director, Acme Corp
          </div>
        </div>
      </div>
    </section>
  );
}

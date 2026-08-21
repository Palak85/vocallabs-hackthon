import Navbar from "../components/common/Navbar";
import Hero from "../components/common/Hero";
import Features from "../components/common/Features";
import HowItWorks from "../components/common/HowItWorks";
import Testimonial from "../components/common/Testimonial";
import Footer from "../components/common/Footer";

export default function LandingPage() {
  return (
    <div className="bg-background text-on-background min-h-screen">
      <Navbar />

      <main className="pt-24 pb-16">
        <Hero />
        <Features />
        <HowItWorks />
        <Testimonial />
      </main>

      <Footer />
    </div>
  );
}

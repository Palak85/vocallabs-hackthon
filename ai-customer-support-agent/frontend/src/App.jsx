import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import { useEffect } from "react";
import LandingPage from "./pages/LandingPage";
import SolutionsPage from "./pages/SolutionsPage";
import ResourcesPage from "./pages/ResourcesPage";
import PricingPage from "./pages/PricingPage";
import DashboardPage from "./pages/DashboardPage";
import CustomerPage from "./pages/CustomerPage";

function ScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  return null;
}

function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/solutions" element={<SolutionsPage />} />
        <Route path="/resources" element={<ResourcesPage />} />
        <Route path="/pricing" element={<PricingPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/customer" element={<CustomerPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

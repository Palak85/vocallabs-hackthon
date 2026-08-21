# Frontend Documentation: Luminous Support Agent UI

Comprehensive technical and architectural documentation for the **Luminous Support** frontend application (`ai-customer-support-agent/frontend`).

---

## 📑 Table of Contents

1. [Overview & Architecture](#1-overview--architecture)
2. [Technology Stack](#2-technology-stack)
3. [Directory Structure](#3-directory-structure)
4. [Design System & Theme Tokens](#4-design-system--theme-tokens)
5. [Component Analysis & Catalog](#5-component-analysis--catalog)
6. [State Management & Interactivity](#6-state-management--interactivity)
7. [Build Configuration & Linting](#7-build-configuration--linting)
8. [Getting Started & Local Development](#8-getting-started--local-development)
9. [Integration Roadmap (Connecting with Backend & RAG)](#9-integration-roadmap)

---

## 1. Overview & Architecture

The frontend for **Luminous Support** is a modern Single Page Application (SPA) designed to showcase and interface with an **AI-Powered Customer Support Agent with Intelligent Human Escalation**.

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                          index.html                         │
│   (HTML5, Google Fonts 'Inter', Material Symbols Outlined)  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                          main.jsx                           │
│                 (React 18 ReactDOM.createRoot)              │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                           App.jsx                           │
│                      (Root App Wrapper)                     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    pages/LandingPage.jsx                    │
│                      (Page Assembler)                       │
├─────────────────────────────────────────────────────────────┤
│  ├── components/common/Navbar.jsx      (Navigation & Menu)  │
│  ├── components/common/Hero.jsx        (Hero & Live Status) │
│  ├── components/common/Features.jsx    (Feature Highlights) │
│  ├── components/common/HowItWorks.jsx  (Workflow Steps)     │
│  ├── components/common/Testimonial.jsx (Social Proof)       │
│  └── components/common/Footer.jsx      (Footer Links)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

| Technology | Version | Purpose / Description |
| :--- | :--- | :--- |
| **React** | `^18.3.1` | Core UI library utilizing functional components and hooks (`useState`). |
| **ReactDOM** | `^18.3.1` | DOM renderer for React. |
| **Vite** | `^5.4.3` | Next-generation frontend tooling and ultra-fast HMR dev server. |
| **Tailwind CSS** | `^3.4.10` | Utility-first CSS framework configured with custom theme tokens. |
| **PostCSS & Autoprefixer** | `^8.4.41` / `^10.4.20` | CSS transformation pipeline and vendor prefixing. |
| **Oxlint** | Custom config | High-performance Rust-based JavaScript/JSX linter. |
| **Google Fonts** | Inter | Modern sans-serif typography for high readability. |
| **Material Symbols** | Variable font | Consistent Google icon suite (`visibility`, `swap_calls`, `menu`, etc.). |

---

## 3. Directory Structure

```
ai-customer-support-agent/frontend/
├── public/
│   ├── favicon.svg             # Browser tab icon
│   └── icons.svg               # SVG sprite definitions
├── src/
│   ├── assets/
│   │   ├── hero.png            # Hero visual assets
│   │   ├── react.svg           # React default asset
│   │   └── vite.svg            # Vite default asset
│   ├── components/
│   │   └── common/
│   │       ├── Features.jsx    # Feature cards with icons & descriptions
│   │       ├── Footer.jsx      # Bottom footer with quick links & copyright
│   │       ├── Hero.jsx        # Value proposition, live badge & mockups
│   │       ├── HowItWorks.jsx  # 3-step animated workflow process
│   │       ├── Navbar.jsx      # Sticky blur header with mobile toggle
│   │       └── Testimonial.jsx # Customer quote & social validation
│   ├── pages/
│   │   └── LandingPage.jsx     # Master landing page layout
│   ├── App.jsx                 # Top-level React component
│   ├── index.css               # Global Tailwind directives & custom keyframes
│   └── main.jsx                # Application DOM entry point
├── .gitignore                  # Git ignore rules for node_modules & dist
├── .oxlintrc.json              # Oxlint linting configuration
├── index.html                  # HTML entry point and external font CDN links
├── package.json                # Project dependencies and script declarations
├── postcss.config.js           # PostCSS configuration for Tailwind
├── tailwind.config.js          # Extended color palette and typography scales
└── vite.config.js              # Vite React plugin setup
```

---

## 4. Design System & Theme Tokens

The application employs a custom **Material 3 / Teal-Cyan Inspired Design System** specified in [tailwind.config.js](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/tailwind.config.js):

### Color System
- **Primary**: `#006a6a` (Deep Teal) / `on-primary`: `#ffffff`
- **Primary Container**: `#47e5e6` (Vibrant Cyan) / `on-primary-container`: `#006364`
- **Primary Fixed / Dim**: `#60f8f9` / `#38dbdc`
- **Secondary**: `#346666` (Slate Teal) / `secondary-container`: `#b8ecec`
- **Tertiary**: `#7a5900` (Warm Amber) / `tertiary-fixed`: `#ffdea2`
- **Surface Scale**:
  - `surface-container-lowest`: `#ffffff`
  - `surface-container-low`: `#eef5f4`
  - `surface-container`: `#e8efee`
  - `surface-container-high`: `#e3eae9`
  - `surface-container-highest`: `#dde4e3`
  - `surface`: `#f4fbfa`
  - `background`: `#f4fbfa`
  - `on-background` / `on-surface`: `#161d1d`

### Custom Typography & Sizing
- **Font Family**: `"Inter", sans-serif`
- **Typography Tokens**:
  - `headline-sm`: `18px`, line-height `24px`, tracking `-0.01em`, font-weight `600`
  - `body-md`: `14px`, line-height `20px`, font-weight `400`
  - `body-sm`: `13px`, line-height `18px`, font-weight `400`
  - `label-md`: `12px`, line-height `16px`, tracking `0.02em`, font-weight `600`
  - `label-xs`: `11px`, line-height `14px`, font-weight `500`

### Custom Keyframes & Utilities
- `.animate-pulse-dot`: 2-second ease-in-out infinite pulsing animation used for real-time live monitoring indicators.
- `::selection`: Highlight styling in `#47e5e6` cyan with `#006364` text.
- `html`: `scroll-behavior: smooth` for smooth in-page anchor navigation.

---

## 5. Component Analysis & Catalog

### 1. `Navbar.jsx`
- **Location**: [src/components/common/Navbar.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/Navbar.jsx)
- **Role**: Top-level sticky navigation bar.
- **Features**:
  - Glassmorphic backdrop blur (`backdrop-blur-md bg-surface/80`).
  - Brand header (`Luminous Support`).
  - Desktop nav links (`Platform`, `Solutions`, `Resources`, `Pricing`).
  - Call-to-actions (`Log In`, `Get Started`).
  - Responsive mobile burger menu with animated collapsible dropdown and state control via `useState(mobileOpen)`.

### 2. `Hero.jsx`
- **Location**: [src/components/common/Hero.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/Hero.jsx)
- **Role**: Above-the-fold engagement section.
- **Features**:
  - **Live Indicator Badge**: Green pulsing dot with `Live Monitored AI` label.
  - **Headline**: High-impact value proposition (*Supervised AI for Customer Support That Never Misses a Beat*).
  - **Primary Action Buttons**: *Start Your Free Trial* and *Watch Demo*.
  - **Visual Asset**: 2-column responsive layout with dashboard preview container.

### 3. `Features.jsx`
- **Location**: [src/components/common/Features.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/Features.jsx)
- **Role**: Highlights key architectural capabilities.
- **Pillars**:
  1. **Supervised Intelligence**: Real-time team oversight and continuous edge-case training.
  2. **Instant Escalation**: Frictionless handoff from AI to live human agents without losing context.
  3. **Voice & Text Native**: Omnichannel support across chat, SMS, and voice calls.

### 4. `HowItWorks.jsx`
- **Location**: [src/components/common/HowItWorks.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/HowItWorks.jsx)
- **Role**: Visual 3-step workflow diagram explaining the agent loop.
- **Steps**:
  1. `Automated Resolution`: RAG-backed query resolution.
  2. `Human Supervision`: Live monitoring of sentiment and tone via supervisor dashboard.
  3. `Seamless Handoff`: Context-preserved escalation for zero-repeat interactions.

### 5. `Testimonial.jsx`
- **Location**: [src/components/common/Testimonial.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/Testimonial.jsx)
- **Role**: Social proof and metric validation.
- **Content**: Highlights 40% reduction in resolution time while preserving brand quality.

### 6. `Footer.jsx`
- **Location**: [src/components/common/Footer.jsx](file:///d:/VOCALLABS%20AI%20Hackathon/ai-customer-support-agent/frontend/src/components/common/Footer.jsx)
- **Role**: Site footer containing company links, legal pages, and copyright notice.

---

## 6. State Management & Interactivity

- **Local State**: Currently leverages React `useState` for local UI behaviors (e.g. mobile navigation toggle).
- **Styling State**: Responsive classes (`hidden md:flex`, `grid md:grid-cols-3`, `hover:shadow-md`, `transition-all`) provide interactive micro-interactions.

---

## 7. Build Configuration & Linting

### Vite (`vite.config.js`)
Configured with `@vitejs/plugin-react` for Fast Refresh during development:
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
```

### Oxlint (`.oxlintrc.json`)
Configured with standard JavaScript & React validation rules to enforce clean code and prevent common bugs.

---

## 8. Getting Started & Local Development

### Prerequisites
- **Node.js**: v18.0.0 or higher
- **npm**: v9.0.0 or higher

### Installation & Run Commands
```bash
# Navigate to the frontend directory
cd ai-customer-support-agent/frontend

# Install dependencies (if not already installed)
npm install

# Start the Vite development server
npm run dev

# Build for production
npm run build

# Preview the production build locally
npm run preview
```

---

## 9. Integration Roadmap

To connect this presentation layer with the AI customer support backend (`/backend`), the following UI modules can be added:

1. **Customer Chat Widget & Voice Visualizer**:
   - Floating interactive widget with WebSockets for real-time streaming AI responses.
   - Web Audio API integration for voice mode input/output.
2. **Supervisor Live Monitoring Console**:
   - Real-time sentiment & frustration gauge (Green/Yellow/Red).
   - Live transcript viewer with automatic escalation alerts.
   - One-click "Take Over Conversation" intervention button.
3. **Agent Workspace**:
   - Customer profile & historical interaction memory sidebar.
   - Suggested RAG responses & quick actions.

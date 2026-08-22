# AI-Powered Customer Support Agent with Intelligent Human Escalation

## ⚡ 1-Step Quickstart (Docker Compose)

Clone the repository and spin up all microservices (PostgreSQL pgvector, Python NLP Microservice, Spring Boot Java Backend, and React Frontend) with a single command:

```bash
# 1. Clone repo
git clone https://github.com/Palak85/vocallabs-hackthon.git
cd vocallabs-hackthon

# 2. Configure Gemini API Key
cp .env.example .env
# Edit .env and paste your GEMINI_API_KEY from https://aistudio.google.com/

# 3. Build & Run all microservices
docker compose up --build
```

### 🌐 Accessing the Services
- **Customer Support Portal (Chat & Voice)**: [http://localhost:5173/customer](http://localhost:5173/customer)
- **Live Supervisor & Monitoring Console**: [http://localhost:5173/dashboard](http://localhost:5173/dashboard)
- **Knowledge Base & Document Ingestion**: [http://localhost:5173/dashboard](http://localhost:5173/dashboard)
- **Spring Boot Backend APIs & Swagger**: [http://localhost:8080](http://localhost:8080)
- **Python NLP Microservice**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **PostgreSQL pgvector Database**: `localhost:5432` (`ai_chat`)

---

## 📌 Overview

This project is an **AI-powered customer support system** designed to autonomously resolve customer problems while continuously monitoring the customer's **sentiment, frustration level, conversation context, and resolution status**.

Unlike traditional chatbots that continue responding even when a customer is dissatisfied, our system introduces a **continuous AI monitoring layer** powered by NLP, sentiment analysis, and RAG.

The system allows the AI agent to independently handle routine customer problems when the customer is satisfied and the issue is resolvable. However, when the system detects **increasing frustration, anger, repeated failed resolution attempts, dissatisfaction, high urgency, or an explicit request for human support**, it automatically escalates the conversation to a human representative.

---

## 🎯 Problem Statement

Traditional customer-support systems either depend heavily on human agents or use automated chatbots that follow predefined workflows.

A major limitation of autonomous AI support is that an AI agent may continue interacting with a customer even when:

- The customer's frustration is increasing.
- The issue is not being resolved.
- The customer has repeatedly explained the same problem.
- The customer is becoming angry or dissatisfied.
- The customer explicitly requests human assistance.
- The problem requires human judgment or intervention.

This can result in:

- Poor customer experience
- Repeated conversations
- Increased customer frustration
- Loss of customer trust
- Inefficient support operations

### Proposed Solution

We propose an **AI-powered customer-support agent with continuous conversation monitoring**.

The system combines:

- Natural Language Processing
- Intent Detection
- Sentiment Analysis
- Frustration Detection
- Retrieval-Augmented Generation (RAG)
- Customer Conversation Memory
- API/Tool Calling
- AI Decision Making
- Human-in-the-Loop Escalation

The AI agent autonomously resolves problems whenever possible. A separate monitoring layer continuously evaluates the conversation and determines whether the AI should continue or transfer the conversation to a human representative.

---

# 🚀 Core Concept

The system follows a simple principle:

> **Let AI handle what AI can solve. Bring humans in when AI should no longer continue.**

```text
                    CUSTOMER
                       │
                       ▼
               Voice / Chat Input
                       │
                       ▼
              Speech-to-Text (Voice)
                       │
                       ▼
              ┌──────────────────┐
              │    AI AGENT      │
              │                  │
              │ Intent Detection │
              │ NLP Processing   │
              │ Reasoning        │
              │ Tool Calling     │
              └────────┬─────────┘
                       │
                       ▼
              Customer Conversation
                       │
                       ▼
          ┌──────────────────────────┐
          │   CONTINUOUS MONITOR     │
          │                          │
          │ Sentiment                │
          │ Frustration              │
          │ Intent                   │
          │ Context                  │
          │ Resolution Status        │
          │ Previous Interactions    │
          └────────────┬─────────────┘
                       │
                       ▼
              RAG + Decision Engine
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
       AI Can Resolve?      AI Cannot Resolve
       Customer Satisfied?  Customer Frustrated?
             │                   │
             ▼                   ▼
        Continue AI         HUMAN ESCALATION
             │                   │
             ▼                   ▼
         API/Tool             Human Agent
          Action
             │
             ▼
        Verify Result
             │
             ▼
          RESOLVED
```

---

# 🧠 How the System Works

## 1. Customer Interaction

The customer communicates with the system through:

- Text chat
- Voice conversation

For voice interaction, the system first converts speech into text using a Speech-to-Text service.

---

## 2. NLP & Intent Detection

The NLP layer identifies what the customer actually wants.

Example:

> "My payment was deducted but my order wasn't placed."

The system identifies:

```text
Intent:
PAYMENT_FAILURE

Entities:
Payment
Order

Problem:
Money deducted but order not created
```

Possible intents include:

```text
ORDER_TRACKING
ORDER_CANCELLATION
PAYMENT_FAILURE
REFUND_REQUEST
DAMAGED_PRODUCT
WRONG_PRODUCT
DELIVERY_DELAY
ACCOUNT_PROBLEM
HUMAN_SUPPORT_REQUEST
```

---

# 😊 Sentiment & Frustration Analysis

The system continuously evaluates the customer's emotional state.

Possible states:

```text
😊 Satisfied
😐 Neutral
😟 Concerned
😠 Frustrated
😡 Angry
```

However, the system does **not** immediately escalate because of one negative sentence.

Instead, it monitors the **trend of the conversation**.

Example:

```text
Message 1 → Neutral
Message 2 → Concerned
Message 3 → Frustrated
Message 4 → Highly Frustrated
```

The increasing frustration becomes an important escalation signal.

---

# 📚 RAG-Based Continuous Monitoring

The RAG layer acts as an **AI supervisor**.

It continuously evaluates:

- Current conversation
- Previous conversation history
- Customer profile
- Relevant knowledge-base information
- Resolution attempts
- API results
- Sentiment history
- Frustration trend

The monitoring system determines whether the AI agent is still capable of resolving the customer's problem.

### Important distinction

RAG is not used only for answering questions.

In this project, RAG also helps the monitoring layer **understand the complete context of the conversation and evaluate the AI agent's progress**.

---

# 🤖 Autonomous AI Resolution

If the customer is satisfied and the issue can be solved automatically, the AI agent continues without involving a human.

Example:

```text
Customer:
"Where is my order?"

        ↓

Intent Detection

        ↓

ORDER_TRACKING

        ↓

Order Tracking API

        ↓

Order Status:
Out for Delivery

        ↓

AI Response

"Your order is currently out for delivery
and should arrive shortly."
```

No human intervention is required.

---

# 🔌 API / Tool Integration

The AI agent can interact with external services through APIs.

Possible tools include:

```text
Order Status API
Cancellation API
Refund API
Payment Status API
Delivery Tracking API
Customer Profile API
Ticket Creation API
Human Escalation API
```

The agent decides which tool is required based on the customer's intent.

---

# 🚨 Intelligent Human Escalation

The most important component of the system is the **Human Escalation Engine**.

The system considers multiple signals rather than relying only on sentiment.

### Escalation signals

```text
Customer Frustration
        +
Repeated Failed Attempts
        +
High Urgency
        +
Unresolved Problem
        +
Negative Sentiment Trend
        +
Explicit Human Request
        +
AI Confidence
        ↓
Escalation Decision
```

For example:

```text
Frustration Score     = 87/100
Resolution Attempts   = 3
Issue Resolved        = NO
Customer Requested HR = YES

        ↓

     ESCALATE
```

---

# 🔄 Example Conversation

### Scenario

**Customer:**

> "My order hasn't arrived."

**AI Agent:**

> "I'm sorry about the delay. Let me check your order status."

The system calls the Order API.

```text
Order Status:
Delayed by 25 minutes
```

The AI responds:

> "Your order is currently delayed by approximately 25 minutes."

Customer:

> "I've already waited for more than an hour."

Monitoring layer:

```text
Sentiment → Negative
Frustration → Increasing
Resolution → Not completed
```

AI attempts another resolution.

Customer:

> "This is the third time I'm contacting support. Nobody is helping me."

Monitoring layer:

```text
Frustration      = 91/100
Repeated Issue   = TRUE
Previous Contact = TRUE
Resolution       = FAILED
Customer Trust   = LOW
```

### Decision

```text
              AI MONITOR
                   │
                   ▼
          Risk exceeds threshold
                   │
                   ▼
          Human Escalation
                   │
                   ▼
          Human Support Agent
```

The customer is transferred without having to explain the entire problem again.

The human agent receives:

```text
Customer Issue:
Delayed Order

Conversation Summary:
Customer reported delivery delay.

Actions Taken:
1. Order status checked
2. Delivery status verified
3. Customer informed

Customer State:
Frustrated

Frustration:
91/100

Reason for Escalation:
Repeated unresolved issue
and high customer frustration
```

---

# ⭐ Key Features

### 1. Autonomous Customer Support

AI handles routine customer problems without human intervention.

### 2. Real-Time Sentiment Analysis

Continuously analyzes the customer's emotional state.

### 3. Frustration Trend Detection

Detects whether customer frustration is increasing or decreasing.

### 4. RAG-Based Conversation Monitoring

A dedicated monitoring layer evaluates the entire conversation context.

### 5. Intelligent API Calling

The AI agent can interact with external systems to actually perform actions.

### 6. Resolution Verification

The system checks whether the action actually solved the customer's problem.

### 7. Intelligent Human Escalation

Human agents are involved only when necessary.

### 8. Conversation Memory

Previous interactions can be used to avoid making customers repeatedly explain the same issue.

### 9. Human Agent Handoff Summary

The human agent receives the complete context, sentiment state, actions already performed, and escalation reason.

---

# 🧩 Proposed Architecture

```text
Frontend
   │
   ├── Chat Interface
   └── Voice Interface
           │
           ▼
      FastAPI Backend
           │
           ▼
      AI Agent Layer
           │
    ┌──────┼────────┐
    ▼      ▼        ▼
  NLP    LLM      Tools
    │      │        │
    │      │        ├── Order API
    │      │        ├── Refund API
    │      │        ├── Payment API
    │      │        └── Delivery API
    │      │
    └──────┼────────┐
           ▼        │
     RAG Knowledge  │
        Base        │
           │        │
           └────┬───┘
                ▼
       Continuous Monitor
                │
       ┌────────┴────────┐
       ▼                 ▼
   AI Continue       Escalate
       │                 │
       ▼                 ▼
    Resolve          Human Agent
```

---

# 🛠️ Technology Stack

## Frontend

- React.js
- Tailwind CSS
- WebSocket for real-time communication

## Backend

- Python
- FastAPI

## AI / NLP

- LLM
- Hugging Face Transformers
- Sentiment Analysis
- Intent Classification
- Named Entity Recognition

## Agent Framework

- LangGraph / LangChain

## RAG

- Vector Database
- Embeddings
- Knowledge Base
- Conversation Memory

## Database

- PostgreSQL

## Voice

- Speech-to-Text
- Text-to-Speech

## Deployment

- Docker
- Git
- GitHub

---

# 📊 Monitoring Dashboard

The support dashboard can display:

```text
┌─────────────────────────────────────────┐
│        AI SUPPORT MONITOR               │
├─────────────────────────────────────────┤
│ Active Conversations: 124               │
│ AI Resolved:          97                │
│ Human Escalations:    27                │
│                                         │
│ Average Frustration:  34%               │
│ Resolution Rate:      78%               │
│                                         │
│ 🚨 High Risk Conversations: 8           │
└─────────────────────────────────────────┘
```

For an active customer:

```text
Customer: #10284

Intent:
Delivery Problem

Sentiment:
😠 Frustrated

Frustration:
87%

Resolution Attempts:
3

AI Confidence:
42%

Status:
🚨 HUMAN ESCALATION
```

---

# 🎯 Project Objective

The primary objective is to create a **trust-aware and customer-aware AI support system** that balances automation with human intervention.

The goal is not to replace human support completely.

Instead:

> **AI handles what it can solve efficiently, while intelligent monitoring determines when a human should take over.**

---

# 💡 Innovation

The proposed system differentiates itself from conventional chatbots through a **continuous supervisory layer**.

Traditional chatbot:

```text
Customer → Chatbot → Response
```

Our system:

```text
Customer
    ↓
AI Agent
    ↓
Action
    ↓
Continuous Monitoring
    ↓
Is customer satisfied?
    ↓
Is the problem actually solved?
    ↓
Can AI continue safely?
    │
    ├── YES → Continue AI
    │
    └── NO → Human Escalation
```

This creates a **Human-in-the-Loop Agentic Customer Support System**.

---

# 🏆 Expected Impact

The system aims to:

- Reduce unnecessary human-support workload
- Reduce repetitive customer interactions
- Improve first-contact resolution
- Detect customer frustration earlier
- Prevent customers from being trapped in ineffective chatbot loops
- Provide faster escalation when human intervention is necessary
- Improve customer satisfaction
- Reduce average support resolution time

---

# 🔮 Future Scope

Future versions can include:

- Multilingual and Hinglish support
- Voice emotion/prosody analysis
- Predictive customer churn detection
- Automatic compensation recommendations
- Multi-agent customer-support architecture
- Advanced customer behavior prediction
- Proactive customer intervention
- Cross-channel conversation memory
- Advanced supervisor agents
- Automatic quality scoring of AI conversations

---

# 🎤 Hackathon Pitch

> **"Our project is not another customer-support chatbot. It is an AI support system with a continuous supervisory layer. The AI agent autonomously understands customer problems, retrieves knowledge, calls APIs, and attempts to resolve issues. At the same time, a monitoring agent continuously evaluates the conversation, customer sentiment, frustration trend, resolution progress, and AI confidence. When the system determines that continuing with AI may lead to a poor customer experience, it automatically transfers the conversation to a human with the complete context and escalation reason. Our goal is simple: let AI handle what it can solve, and let humans step in exactly when they are needed."**

---

## 📌 Core USP

**"Don't just automate customer support — automatically know when NOT to automate it."**

# AI Customer Support Agent — Dual Dashboard Frontend Guide

This guide describes how to build the two primary frontend dashboards:
1. **Normal User / Consumer Dashboard**: A clean, consumer-facing two-sided interface featuring a **Text Chat Mode** and a **Voice Call Mode** with live speech recognition (STT), voice synthesis (TTS), live call transcripts, and history. (No admin/ingestion controls).
2. **Monitoring & Supervisor Dashboard**: A comprehensive supervisor and admin interface where:
   - **Knowledge Base Ingestion & Document Management** is conducted (Upload PDFs, DOCX, TXT, MD; track chunking & indexing; delete docs).
   - **Live Calls & Chats are Monitored** in real-time with customer emotional metrics and frustration scores.
   - **AI-Driven Escalation Alerts** prompt supervisors when a user is frustrated (*"User is getting frustrated. Recommend switching to live chat"*).
   - **Human Takeover & Direct Messaging Console** allows admins to explicitly take over calls/chats and hand them back to AI.

---

## 1. Dashboard 1: Normal User / Consumer Dashboard

The consumer dashboard is dedicated entirely to the end user's query experience with a toggle between **Text Chat Mode** and **Voice Call Mode**.

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  CUSTOMER SUPPORT PORTAL | [ 💬 Text Mode ] [ 📞 Voice Call Mode ] | [ Call History 🕒] │
├────────────────────────────────────────────────────────┬───────────────────────────────┤
│  SIDE A: TEXT CHAT MODE                                │  SIDE B: VOICE CALL MODE      │
│                                                        │                               │
│  Chat Transcript Feed:                                 │  [ 🟢 Call Connected: 01:45 ] │
│  ┌──────────────────────────────────────────────────┐  │                               │
│  │ User: My UPI transaction failed...               │  │  Live Audio Visualizer:       │
│  │ Assistant: I understand your frustration...      │  │  ( 〰️〰️ Live Sound Wave 〰️〰️ )   │
│  └──────────────────────────────────────────────────┘  │                               │
│                                                        │  Live Call Transcript Stream: │
│  Input Area:                                           │  • [You (01:20)]: I was       │
│  [ Type your question here...          ] [ Send 📤 ]   │    charged twice.             │
│                                                        │  • [AI (01:22)]: Let me check.│
│                                                        │                               │
│                                                        │  Call Controls:               │
│                                                        │  [ 🎙️ Mute ] [ 🔊 Speaker ]   │
│                                                        │  [ 🔴 End Call ]              │
└────────────────────────────────────────────────────────┴───────────────────────────────┘
```

### 1.1. Side A: Text Chat Mode
* **Query Input**: Focused text field with prompt suggestion chips.
* **Token Streaming**: Consumes `POST /api/v1/chat/stream`, rendering text token-by-token.
* **Live Badges**: Optionally displays detected intent and entity tags.

### 1.2. Side B: Voice Call Mode
* **Voice-First Experience**:
  1. User clicks **"Start Call"** to speak into their microphone.
  2. Browser Web Speech API (`SpeechRecognition`) converts speech to text in real-time.
  3. The recognized speech appears instantly in the **Live Call Transcript** and is dispatched to `/api/v1/chat/stream`.
  4. As assistant tokens stream in, browser Speech Synthesis (`SpeechSynthesisUtterance`) speaks the response aloud naturally.
  5. The transcript highlights speaker labels (`[You]`, `[AI Assistant]`, `[Live Specialist]`).
* **Interactive Soundwave Visualizer**: Animated canvas/SVG waves reacting to speech volume.
* **Call Controls**: Mute/Unmute, Speaker Mute, End Call.
* **Call History Modal**: Displays previous call transcripts, timestamps, and durations.

---

## 2. Dashboard 2: Monitoring & Supervisor Dashboard

The monitoring dashboard is the control center for administrators and support supervisors. It houses **Knowledge Base Ingestion** as well as **Live Call Monitoring & Human Takeover**.

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  SUPERVISOR & MONITORING CONSOLE | Tenant: [ default ▼ ] | Stats: 15 Active | 2 Escalated │
├────────────────────────────────────────┬───────────────────────────────────────────────┤
│  KNOWLEDGE BASE MANAGEMENT (INGESTION) │  LIVE CALL MONITOR & HUMAN TAKEOVER CONSOLE   │
│                                        │                                               │
│  Upload New Document:                  │  Active Call: conv_001 | Customer: cust_987   │
│  ┌──────────────────────────────────┐  │  Current Mode: [ 🤖 AI Handled ]              │
│  │ 📁 Drag & drop PDF/DOCX/TXT/MD   │  │  [ 🛑 Switch to Human / Takeover Call ]      │
│  │ Category: [ banking ▼ ]          │  │                                               │
│  │ [ Upload to Vector DB 📤 ]       │  │  ┌─────────────────────────────────────────┐  │
│  └──────────────────────────────────┘  │  │ ⚠️ AI AGENT ESCALATION ALERT:            │  │
│                                        │  │ "User is getting frustrated (Score: 72).│  │
│  Ingested Knowledge Base:              │  │  Recommend switching to live chat."     │  │
│  • upi_faq.pdf (12 chunks) [Ready]     │  └─────────────────────────────────────────┘  │
│  • refund_policy.pdf (8 chunks) [Ready]│                                               │
│  • warranty.docx [ ⏳ Processing... ]  │  Live Transcript & Metrics:                   │
│                                        │  Frustration: 72/100 (High) 🔥 | Intent: UPI  │
│                                        │  • [User]: Money deducted from account!       │
│                                        │  • [AI]: Reversal occurs within 24-48 hrs.   │
│                                        │                                               │
│                                        │  Supervisor Live Reply Box:                   │
│                                        │  [ Type message as Human Agent... ] [ Send 📨]│
│                                        │                                               │
│                                        │  [ 🔄 Hand Back to AI ]                       │
└────────────────────────────────────────┴───────────────────────────────────────────────┘
```

### 2.1. Knowledge Base Ingestion Panel (Admin Only)
* **Document Upload**:
  * Dropzone accepting `.pdf`, `.docx`, `.txt`, `.md` (Max 20MB).
  * Category tagging selector (`banking`, `ecommerce`, `technical`, `general`).
  * Triggers `POST /api/v1/documents`.
* **Real-time Indexing Status**:
  * Status pills: `PROCESSING` (with spinner), `COMPLETED` (green with chunk count), `FAILED` (red with error and retry button).
* **Document Deletion**: Trash icon triggering `DELETE /api/v1/documents/{id}` to purge chunks from `pgvector`.

### 2.2. Live Call Monitoring & Frustration Tracking
* **Real-time Session Cards**:
  * Color-coded Frustration Meter: `Low (0-39)`, `Medium (40-69)`, `High (70-100)`.
  * Sentiment and Emotion Badges (`Frustrated`, `Anxious`, `Neutral`).
  * Extracted Entity Chips (`[Transaction ID: TXN12345]`, `[Order ID: ORD9988]`).

### 2.3. AI-Driven Escalation Alerts
* When frustration reaches **`>= 70`** or urgent negative sentiment is detected:
  * The supervisor console flashes an alert banner:
    > **⚠️ AI Agent Prompt**: *"Customer is getting frustrated (Score: 72/100, Intent: transaction_failed). Recommend switching to live chat."*
  * A prominent **"Takeover Call"** button is highlighted.

### 2.4. Human Takeover Workflow
1. **Takeover Action**: Admin clicks **"Takeover Call"** (`POST /api/v1/monitoring/conversations/{id}/takeover`).
2. **AI Paused**: The AI stops generating responses; customer receives notice that a specialist has joined.
3. **Supervisor Direct Messaging**: Supervisor replies directly using `POST /api/v1/monitoring/conversations/{id}/message`. Messages are spoken aloud in the customer's Voice Call mode as `[Live Specialist]`.
4. **Hand Back to AI**: Admin clicks **"Hand Back to AI"** (`POST /api/v1/monitoring/conversations/{id}/handback`) once the issue is resolved.

---

## 3. Frontend Integration Code (React + TypeScript)

### 3.1. Voice Call Hook (Customer Dashboard)

```typescript
import { useState, useRef } from 'react';

export function useVoiceCall(conversationId?: string) {
  const [isCalling, setIsCalling] = useState<boolean>(false);
  const [transcript, setTranscript] = useState<Array<{ role: string; text: string }>>([]);
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const recognitionRef = useRef<any>(null);

  const startCall = () => {
    setIsCalling(true);
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-US';
    recognition.continuous = true;
    recognition.interimResults = false;

    recognition.onresult = async (event: any) => {
      const lastIndex = event.results.length - 1;
      const spokenText = event.results[lastIndex][0].transcript.trim();
      if (!spokenText) return;

      setTranscript((prev) => [...prev, { role: 'You', text: spokenText }]);
      await sendVoiceQuery(spokenText);
    };

    recognition.start();
    recognitionRef.current = recognition;
  };

  const sendVoiceQuery = async (query: string) => {
    const response = await fetch('http://localhost:8080/api/v1/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'X-Tenant-Id': 'default',
      },
      body: JSON.stringify({
        conversation_id: conversationId,
        text: query,
        customer_id: 'cust_voice_user',
      }),
    });

    const reader = response.body?.getReader();
    const decoder = new TextDecoder('utf-8');
    let aiAnswer = '';
    let buffer = '';

    if (!reader) return;

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n\n');
      buffer = lines.pop() || '';

      for (const block of lines) {
        const eventMatch = block.match(/event:\s*(.+)/);
        const dataMatch = block.match(/data:\s*(.+)/);

        if (eventMatch && dataMatch) {
          const event = eventMatch[1].trim();
          const data = dataMatch[1].trim();

          if (event === 'token') {
            aiAnswer += data;
          } else if (event === 'human_agent_active') {
            aiAnswer = data;
          }
        }
      }
    }

    setTranscript((prev) => [...prev, { role: 'AI Assistant', text: aiAnswer }]);
    speakResponse(aiAnswer);
  };

  const speakResponse = (text: string) => {
    if (!('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.onstart = () => setIsSpeaking(true);
    utterance.onend = () => setIsSpeaking(false);
    window.speechSynthesis.speak(utterance);
  };

  const endCall = () => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
    }
    window.speechSynthesis.cancel();
    setIsCalling(false);
    setIsSpeaking(false);
  };

  return { isCalling, isSpeaking, transcript, startCall, endCall };
}
```

---

### 3.2. Knowledge Base Ingestion Hook (Monitoring Dashboard)

```typescript
import { useState, useEffect } from 'react';

export function useKnowledgeBase() {
  const [documents, setDocuments] = useState<any[]>([]);
  const [isUploading, setIsUploading] = useState<boolean>(false);

  const fetchDocuments = async () => {
    const res = await fetch('http://localhost:8080/api/v1/documents', {
      headers: { 'X-Tenant-Id': 'default' },
    });
    if (res.ok) setDocuments(await res.json());
  };

  useEffect(() => {
    fetchDocuments();
    const interval = setInterval(fetchDocuments, 4000); // Poll ingestion status
    return () => clearInterval(interval);
  }, []);

  const uploadDocument = async (file: File, category?: string) => {
    setIsUploading(true);
    const formData = new FormData();
    formData.append('file', file);
    if (category) formData.append('category', category);

    await fetch('http://localhost:8080/api/v1/documents', {
      method: 'POST',
      headers: { 'X-Tenant-Id': 'default' },
      body: formData,
    });
    setIsUploading(false);
    fetchDocuments();
  };

  const deleteDocument = async (id: string) => {
    await fetch(`http://localhost:8080/api/v1/documents/${id}`, {
      method: 'DELETE',
      headers: { 'X-Tenant-Id': 'default' },
    });
    fetchDocuments();
  };

  return { documents, isUploading, uploadDocument, deleteDocument };
}
```

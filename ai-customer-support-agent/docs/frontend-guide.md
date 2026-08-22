# AI Customer Support Agent — Frontend UI/UX Guide

This guide outlines how the React frontend UI should be structured, how visual components should behave across different lifecycle states, and what the user should see at each step of the user journey.

---

## 1. UI Component Architecture & Layout

The recommended UI layout consists of three primary zones:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  HEADER: App Title | Tenant Selector [X-Tenant-Id: default] | KB Status (3 Docs)       │
├───────────────────────┬────────────────────────────────────────┬───────────────────────┤
│  SIDEBAR              │  CENTRAL CHAT & NLP HUD                │  SOURCES / CITATIONS  │
│                       │                                        │  DRAWER (Expandable)  │
│  [+ New Chat]         │  ┌──────────────────────────────────┐  │                       │
│                       │  │ LIVE NLP ANALYSIS HUD            │  │  • UPI FAQs.pdf       │
│  Conversations:       │  │ [Banking] [Txn Failed] [High: 72]│  │    Page 3 (Score 0.89)│
│  • UPI Transaction    │  │ Entities: [TXN12345]             │  │                       │
│  • Refund Policy      │  └──────────────────────────────────┘  │  • Refund_Rules.docx  │
│                       │                                        │    Page 1 (Score 0.78)│
│  Knowledge Base:      │  Message Feed:                         │                       │
│  • [Upload Document]  │  [User Bubble]: My UPI txn failed...   │                       │
│  • faq.pdf (Ready)    │  [Assistant Bubble]: Streaming text... │                       │
│                       │                                        │                       │
│                       ├────────────────────────────────────────┤                       │
│                       │  INPUT & AUDIO CONTROLS                │                       │
│                       │  [ Input text... ] [🎙️ Mic] [🔊 TTS]   │                       │
│                       │  [ 〰️〰️ Live Audio Waveform (STT) 〰️〰️ ] │                       │
└───────────────────────┴────────────────────────────────────────┴───────────────────────┘
```

---

## 2. Comprehensive State-by-State UI Guide

### State 1: IDLE / READY STATE

#### What the user sees:
* **Chat Area**: Centered welcome message with quick prompt chips:
  * *"My UPI transaction failed and money was debited"*
  * *"How do I request a refund?"*
  * *"What are your customer support hours?"*
* **Input Box**: Focused text field with placeholder *"Type your question or click the microphone to speak..."*.
* **Action Buttons**:
  * **Microphone Icon (STT)**: Solid blue, ready for single-click voice input.
  * **TTS Toggle**: Enabled/Disabled state toggle for auto-speaking responses.
  * **Send Button**: Disabled when text is empty; enabled when text is typed.
* **Sidebar**: List of previous chat sessions and active knowledge base documents.

---

### State 2: LISTENING STATE (Voice STT Active)

#### Trigger:
* User clicks the **Microphone Button** (or hotkey `Spacebar`).

#### What the user sees:
* **Microphone Button**: Turns vibrant red with an animated pulsing ring.
* **Audio Visualizer**: An animated audio frequency waveform (bars or glowing wave) displays under the input box reflecting user voice volume.
* **Live Transcription**: Spoken words appear in real-time inside the input text field.
* **Status Badge**: *"Listening... Click to send or press Esc to cancel"*.
* **Control Actions**:
  * **Stop / Submit Button**: Immediately commits the recognized speech and starts the chat flow.
  * **Cancel Button**: Discards speech and returns to State 1.

---

### State 3: DOCUMENT UPLOAD & INGESTION STATE

#### Trigger:
* User drags and drops a file (PDF, DOCX, TXT, MD) into the Knowledge Base Drawer or clicks "Upload Document".

#### What the user sees:
* **Upload Progress**:
  * Step 1: File upload bar (`0% -> 100%`).
  * Step 2: Badge changes to `PROCESSING` with a circular spinner.
  * Step 3: Once pgvector chunking & embedding finishes, badge turns green `COMPLETED` showing chunk count (`e.g. 12 chunks indexed`).
* **Error Handling**: If upload or parsing fails, a red `FAILED` badge appears with a tooltip error message and a *"Retry"* button.

---

### State 4: ANALYZING & RETRIEVING STATE (Enriched Query Processing)

#### Trigger:
* User submits a question (via text or STT).

#### What the user sees:
1. **User Message Bubble**: Renders immediately on the right side with timestamps.
2. **Real-Time NLP HUD** (Appears instantly at the top of the chat window on `event: nlp`):
   * **Domain Badge**: E.g., `Domain: Banking` (Blue pill)
   * **Intent Badge**: E.g., `Intent: Transaction Failed` (Purple pill)
   * **Emotion & Sentiment**: E.g., `Sentiment: Negative` | `Emotion: Frustrated` (Amber pill)
   * **Frustration Gauge**: Visual progress meter with score `72/100 (High)`
   * **Entity Chips**: Clickable tags showing extracted values like `[Transaction ID: TXN12345]`, `[Amount: ₹500]`
3. **Retrieval Indicator**: A subtle shimmer or skeleton pulse below the user message: *"Searching knowledge base documents..."*.

---

### State 5: STREAMING RESPONSE STATE (SSE Token Generation)

#### Trigger:
* First `event: token` received from `/api/v1/chat/stream`.

#### What the user sees:
* **Assistant Message Bubble**: Appears on the left side with an AI avatar.
* **Typewriter Effect**: Words stream in character-by-character or token-by-token with a subtle blinking cursor `|`.
* **Smooth Auto-Scroll**: The viewport smoothly auto-scrolls down as new lines are generated.
* **Stop Button**: An optional *"Stop Generating"* button allows the user to abort the stream.

---

### State 6: RESPONSE COMPLETE & CITATIONS RENDERED

#### Trigger:
* `event: sources` and `event: done` received.

#### What the user sees:
* **Blinking Cursor**: Disappears.
* **Action Toolbar on Assistant Bubble**:
  * **Copy to Clipboard**: Copies formatted markdown.
  * **Replay Voice (TTS)**: Reads the answer aloud.
  * **Thumbs Up / Thumbs Down**: Feedback rating.
* **Expandable Citations Accordion (`Sources` Drawer)**:
  * Displays source document cards with:
    * Document Title (e.g. `UPI Banking FAQs.pdf`)
    * Page Number (e.g. `Page 3`)
    * Similarity Match Badge (e.g. `89% match`)
    * Quoted Context Snippet

---

### State 7: SPEAKING / AUDIO PLAYBACK STATE (Frontend TTS)

#### Trigger:
* User clicks "Read Aloud" or auto-TTS was enabled.

#### What the user sees:
* **Audio Wave Indicator**: Small animated soundbars next to the Assistant bubble.
* **Controls**: **Pause**, **Resume**, **Stop Playback** buttons.
* **Optional Karaoke Highlighting**: Highlights the current sentence being spoken.

---

### State 8: REFUSAL / NO CONTEXT / ERROR STATE

#### Trigger:
* No matching documents found (refusal) or server error occurs.

#### What the user sees:
* **Refusal Response**:
  * Gentle, polite message: *"I could not find information regarding this in our available knowledge base documents."*
  * Quick Actions:
    * *"Upload relevant document"* button (opens KB drawer).
    * *"Contact human support"* button.
* **Network Error / 500**:
  * Toast notification: *"Failed to connect to backend. Please verify your connection and try again."*
  * *"Retry"* action directly on the message bubble.

---

## 3. Frontend Integration Code Snippets (React + TypeScript)

### 3.1. SSE Stream Client (`/api/v1/chat/stream`)

```typescript
import { useState } from 'react';

interface NlpData {
  language: { label: string; confidence: number };
  domain: { label: string; confidence: number };
  intent: { label: string; confidence: number };
  sentiment: { label: string; confidence: number };
  emotion: { label: string; confidence: number };
  frustration: { score: number; level: string };
  urgency: { level: string; confidence: number };
  entities: Array<{ type: string; value: string; confidence: number }>;
}

export function useChatStream() {
  const [tokens, setTokens] = useState<string>('');
  const [nlp, setNlp] = useState<NlpData | null>(null);
  const [sources, setSources] = useState<any[]>([]);
  const [isStreaming, setIsStreaming] = useState<boolean>(false);

  const sendMessage = async (question: string, conversationId?: string) => {
    setIsStreaming(true);
    setTokens('');
    setSources([]);

    const response = await fetch('http://localhost:8080/api/v1/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'X-Tenant-Id': 'default',
      },
      body: JSON.stringify({
        conversation_id: conversationId,
        text: question,
        customer_id: 'cust_react_user',
      }),
    });

    const reader = response.body?.getReader();
    const decoder = new TextDecoder('utf-8');
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
          const eventType = eventMatch[1].trim();
          const rawData = dataMatch[1].trim();

          if (eventType === 'nlp') {
            const parsed = JSON.parse(rawData);
            setNlp(parsed.nlp);
          } else if (eventType === 'token') {
            setTokens((prev) => prev + rawData);
          } else if (eventType === 'sources') {
            const parsedSources = JSON.parse(rawData);
            setSources(parsedSources);
          } else if (eventType === 'done') {
            setIsStreaming(false);
          }
        }
      }
    }
    setIsStreaming(false);
  };

  return { sendMessage, tokens, nlp, sources, isStreaming };
}
```

---

### 3.2. Speech-to-Text (STT) Client

```typescript
export function startSpeechRecognition(onResult: (text: string) => void, onEnd: () => void) {
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (!SpeechRecognition) {
    alert('Speech recognition is not supported in this browser.');
    return null;
  }

  const recognition = new SpeechRecognition();
  recognition.lang = 'en-US';
  recognition.continuous = false;
  recognition.interimResults = true;

  recognition.onresult = (event: any) => {
    let transcript = '';
    for (let i = event.resultIndex; i < event.results.length; i++) {
      transcript += event.results[i][0].transcript;
    }
    onResult(transcript);
  };

  recognition.onend = onEnd;
  recognition.start();
  return recognition;
}
```

---

### 3.3. Text-to-Speech (TTS) Client

```typescript
export function speakText(text: string, onEnd?: () => void) {
  if (!('speechSynthesis' in window)) return;

  window.speechSynthesis.cancel(); // Stop any previous speech
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.rate = 1.0;
  utterance.pitch = 1.0;

  if (onEnd) {
    utterance.onend = onEnd;
  }

  window.speechSynthesis.speak(utterance);
}
```

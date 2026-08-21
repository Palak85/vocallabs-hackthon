import { useState, useEffect, useRef } from "react";

const initialMessages = [
  {
    id: 1,
    sender: "customer",
    text: "Hi, I've been charged twice this month for my pro subscription. I need a refund immediately.",
    time: "10:42 AM",
  },
  {
    id: 2,
    sender: "ai",
    text: "Hello Maria. I apologize for the inconvenience. Let me check your billing history right away to see what happened.",
    time: "10:42 AM",
  },
  {
    id: 3,
    sender: "system",
    text: "Executing API: GET /billing/v2/history?user=98421",
  },
  {
    id: 4,
    sender: "ai",
    text: "I see the duplicate charge on Oct 24th for $49.99. This appears to be a system error during our payment gateway update. I can process a refund for the duplicate charge right now.",
    time: "10:43 AM",
  },
  {
    id: 5,
    sender: "customer",
    text: "Yes, please do. When will it show up in my account?",
    time: "10:44 AM",
  },
];

export default function ActiveChat() {
  const [messages, setMessages] = useState(initialMessages);
  const [inputText, setInputText] = useState("");
  const [isListening, setIsListening] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(true);
  const recognitionRef = useRef(null);

  useEffect(() => {
    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setSpeechSupported(false);
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = "en-US";

    recognition.onresult = (event) => {
      let transcript = "";
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      setInputText(transcript);
    };

    recognition.onerror = (event) => {
      console.error("Speech recognition error:", event.error);
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = recognition;
  }, []);

  const toggleListening = () => {
    if (!speechSupported) {
      alert("Speech recognition is not supported in this browser. Please try Chrome or Edge.");
      return;
    }

    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
    } else {
      try {
        recognitionRef.current?.start();
        setIsListening(true);
      } catch (err) {
        console.error("Failed to start speech recognition:", err);
      }
    }
  };

  const handleSendMessage = () => {
    if (!inputText.trim()) return;

    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
    }

    const now = new Date();
    const timeString = now.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });

    const newMessage = {
      id: Date.now(),
      sender: "supervisor",
      text: inputText,
      time: timeString,
    };

    setMessages((prev) => [...prev, newMessage]);
    setInputText("");
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <section className="flex-1 flex flex-col bg-dash-surface-container-lowest rounded-xl shadow-sm border border-dash-surface-variant overflow-hidden min-w-[300px]">
      {/* Frustration Warning Banner */}
      <div className="bg-dash-tertiary-container text-dash-on-tertiary-container px-4 py-2 flex items-center gap-2 text-label-md font-semibold border-b border-dash-tertiary/20">
        <span className="material-symbols-outlined text-sm">warning</span>
        User frustration spike detected. Monitoring suggested.
      </div>

      {/* Chat Header */}
      <div className="p-4 border-b border-dash-outline-variant flex justify-between items-center bg-dash-surface-bright shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-dash-surface-container flex items-center justify-center text-dash-on-surface-variant font-bold text-headline-sm">
            MC
          </div>
          <div>
            <h3 className="font-bold text-body-md text-dash-on-surface">Maria Chen</h3>
            <p className="text-label-xs text-dash-on-surface-variant flex items-center gap-1">
              <span className="w-2 h-2 rounded-full bg-green-500" />
              Online • ID: #98421
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <span className="px-2 py-1 bg-dash-surface-container-high rounded text-label-xs text-dash-on-surface-variant border border-dash-outline-variant">
            Billing
          </span>
          <span className="px-2 py-1 bg-dash-error-container text-dash-on-error-container rounded text-label-xs border border-dash-error/20">
            High Frustration
          </span>
        </div>
      </div>

      {/* Chat Messages */}
      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-section-gap bg-dash-surface-container-lowest">
        {messages.map((msg) => {
          if (msg.sender === "system") {
            return (
              <div
                key={msg.id}
                className="self-center my-2 flex items-center gap-2 bg-dash-surface-container px-3 py-1.5 rounded-full border border-dash-outline-variant text-label-xs text-dash-on-surface-variant"
              >
                <span className="material-symbols-outlined text-[14px] animate-spin">
                  progress_activity
                </span>
                {msg.text}
              </div>
            );
          }

          const isCustomer = msg.sender === "customer";
          const isSupervisor = msg.sender === "supervisor";
          const isAi = msg.sender === "ai";

          let bubbleStyles = "bg-dash-primary text-dash-on-primary rounded-tr-none";
          if (isCustomer) {
            bubbleStyles = "bg-dash-surface-container-high text-dash-on-surface rounded-tl-none border border-dash-surface-variant";
          } else if (isSupervisor) {
            bubbleStyles = "bg-dash-secondary text-dash-on-secondary rounded-tr-none";
          }

          return (
            <div
              key={msg.id}
              className={`flex flex-col gap-bubble-gap max-w-[85%] ${
                isCustomer ? "self-start" : "self-end items-end"
              }`}
            >
              <div className={`p-3 rounded-2xl text-body-sm shadow-sm ${bubbleStyles}`}>
                {msg.text}
              </div>
              <span
                className={`text-label-xs text-dash-on-surface-variant ${
                  isCustomer ? "ml-2" : "mr-2"
                }`}
              >
                {msg.time}
                {isAi && " • AI Agent"}
                {isSupervisor && " • Human Supervisor"}
              </span>
            </div>
          );
        })}
      </div>

      {/* Chat Input Area */}
      <div className="p-4 border-t border-dash-outline-variant bg-dash-surface-bright shrink-0">
        <div className="flex gap-2 mb-3">
          <button className="flex-1 bg-dash-surface-container text-dash-on-surface-variant border border-dash-outline-variant py-2 rounded-lg text-label-md font-semibold hover:bg-dash-surface-variant transition-colors flex items-center justify-center gap-2 cursor-pointer">
            <span className="material-symbols-outlined text-sm">visibility</span>
            Private Suggestion
          </button>
          <button className="flex-1 bg-dash-primary text-dash-on-primary py-2 rounded-lg text-label-md font-semibold hover:opacity-90 transition-colors shadow-sm flex items-center justify-center gap-2 cursor-pointer">
            <span className="material-symbols-outlined text-sm">front_hand</span>
            Take Over
          </button>
        </div>

        <div className="relative flex items-center">
          <input
            className={`w-full bg-dash-surface-container-lowest border border-dash-outline-variant rounded-xl pl-4 pr-24 py-3 text-body-sm focus:ring-2 focus:ring-dash-primary focus:border-transparent outline-none transition-all placeholder:text-dash-outline ${
              isListening ? "ring-2 ring-emerald-500 border-emerald-500" : ""
            }`}
            placeholder={
              isListening ? "Listening... Speak now..." : "Type a message to take over..."
            }
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <div className="absolute right-2 flex items-center gap-1">
            {/* Mic / Speech-to-Text Button */}
            <button
              onClick={toggleListening}
              type="button"
              title={isListening ? "Stop Listening" : "Start Voice Input"}
              className={`p-2 rounded-full transition-colors flex items-center justify-center cursor-pointer ${
                isListening
                  ? "bg-emerald-500 text-white animate-pulse"
                  : "text-dash-on-surface-variant hover:bg-dash-surface-variant hover:text-dash-primary"
              }`}
            >
              <span
                className="material-symbols-outlined text-xl"
                style={isListening ? { fontVariationSettings: "'FILL' 1" } : {}}
              >
                {isListening ? "mic" : "mic_none"}
              </span>
            </button>

            {/* Send Button */}
            <button
              onClick={handleSendMessage}
              type="button"
              title="Send Message"
              className="p-2 text-dash-primary hover:bg-dash-surface-variant rounded-full transition-colors flex items-center justify-center cursor-pointer"
            >
              <span
                className="material-symbols-outlined"
                style={{ fontVariationSettings: "'FILL' 1" }}
              >
                send
              </span>
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}


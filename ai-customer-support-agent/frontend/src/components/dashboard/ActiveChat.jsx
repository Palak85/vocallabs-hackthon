import { useState, useEffect, useRef } from "react";
import { api } from "../../services/api";

const initialMessages = [
  {
    id: 1,
    sender: "ai",
    text: "Hello! Welcome to AI Multi-Domain Customer Support. How can I assist you with your order, banking, education, insurance, telecom, travel, or healthcare query today?",
    time: "10:00 AM",
  }
];

export default function ActiveChat({ isEscalated, onTurnComplete }) {
  const [messages, setMessages] = useState(initialMessages);
  const [inputText, setInputText] = useState("");
  const [isListening, setIsListening] = useState(false);
  const [speechSupported, setSpeechSupported] = useState(true);
  const [selectedFile, setSelectedFile] = useState(null);
  const [conversationId, setConversationId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const recognitionRef = useRef(null);
  const fileInputRef = useRef(null);

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

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
    }
    e.target.value = "";
  };

  const handleSendMessage = async () => {
    if (!inputText.trim() && !selectedFile) return;

    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
    }

    const now = new Date();
    const timeString = now.toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });

    let messageText = inputText;
    const currentFile = selectedFile;
    if (currentFile) {
      const fileLabel = `📎 ${currentFile.name}`;
      messageText = messageText ? `${messageText}\n${fileLabel}` : fileLabel;
    }

    const newMessage = {
      id: Date.now(),
      sender: "customer",
      text: messageText,
      time: timeString,
    };

    setMessages((prev) => [...prev, newMessage]);
    const textToSend = inputText.trim();
    setInputText("");
    setSelectedFile(null);
    setIsLoading(true);

    try {
      if (currentFile) {
        setMessages((prev) => [
          ...prev,
          {
            id: Date.now() + 1,
            sender: "system",
            text: `Uploading document: ${currentFile.name}...`,
          },
        ]);
        
        await api.uploadDocument(currentFile);
        setMessages((prev) => prev.filter(m => !m.text.includes('Uploading document:')));
      }

      if (textToSend) {
        setMessages((prev) => [
          ...prev,
          {
            id: 'loading-indicator',
            sender: "system",
            text: "AI Agent analyzing signals & formulating response...",
          }
        ]);

        const response = await api.sendMessage(textToSend, conversationId);
        
        if (response.conversationId) {
          setConversationId(response.conversationId);
        }

        setMessages((prev) => prev.filter(m => m.id !== 'loading-indicator'));

        const aiMessageText = response.response || response.answer || "Your request is being processed.";
        const aiMessage = {
          id: response.messageId || Date.now() + 2,
          sender: response.escalated ? "supervisor" : "ai",
          text: aiMessageText,
          time: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          escalated: response.escalated,
          status: response.status
        };

        setMessages((prev) => [...prev, aiMessage]);

        if (onTurnComplete) {
          onTurnComplete(response);
        }
      }
    } catch (error) {
      setMessages((prev) => prev.filter(m => m.id !== 'loading-indicator'));
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 3,
          sender: "system",
          text: `Service response: ${error.message}`,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <section className="flex-1 flex flex-col bg-dash-surface-container-lowest rounded-xl shadow-sm border border-dash-surface-variant overflow-hidden min-w-[300px]">
      {/* Frustration / Escalation Warning Banner */}
      {isEscalated ? (
        <div className="bg-red-500/20 text-red-300 px-4 py-2 flex items-center gap-2 text-label-md font-semibold border-b border-red-500/30 animate-pulse">
          <span className="material-symbols-outlined text-sm">support_agent</span>
          Human Escalation Active: Conversation routed to Senior Support Specialist.
        </div>
      ) : (
        <div className="bg-dash-tertiary-container text-dash-on-tertiary-container px-4 py-2 flex items-center gap-2 text-label-md font-semibold border-b border-dash-tertiary/20">
          <span className="material-symbols-outlined text-sm">smart_toy</span>
          AI Agent Gateway Active • Real-Time NLP Intelligence & Continuous Monitoring
        </div>
      )}

      {/* Chat Header */}
      <div className="p-4 border-b border-dash-outline-variant flex justify-between items-center bg-dash-surface-bright shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-dash-surface-container flex items-center justify-center text-dash-on-surface-variant font-bold text-headline-sm">
            CU
          </div>
          <div>
            <h3 className="font-bold text-body-md text-dash-on-surface">Customer Session</h3>
            <p className="text-label-xs text-dash-on-surface-variant flex items-center gap-1">
              <span className="w-2 h-2 rounded-full bg-green-500" />
              Active • ID: {conversationId ? conversationId : "New Session"}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          <span className="px-2 py-1 bg-dash-surface-container-high rounded text-label-xs text-dash-on-surface-variant border border-dash-outline-variant">
            Multi-Domain
          </span>
          {isEscalated ? (
            <span className="px-2 py-1 bg-red-600/30 text-red-300 font-semibold rounded text-label-xs border border-red-500/30 animate-pulse">
              ESCALATED
            </span>
          ) : (
            <span className="px-2 py-1 bg-emerald-500/20 text-emerald-300 font-semibold rounded text-label-xs border border-emerald-500/30">
              AI MONITORING
            </span>
          )}
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
            bubbleStyles = "bg-amber-100 text-amber-950 dark:bg-amber-950/80 dark:text-amber-100 border border-amber-500/60 font-medium rounded-tr-none shadow-sm";
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
                {isAi && " • AI Support Agent"}
                {isSupervisor && " • Human Escalation Specialist"}
                {isCustomer && " • Customer"}
              </span>
            </div>
          );
        })}
      </div>

      {/* Chat Input Area */}
      <div className="p-4 border-t border-dash-outline-variant bg-dash-surface-bright shrink-0">
        {/* File Preview Chip */}
        {selectedFile && (
          <div className="flex items-center gap-2 mb-2 px-3 py-1.5 bg-dash-surface-container rounded-lg border border-dash-outline-variant w-fit">
            <span className="material-symbols-outlined text-sm text-dash-primary">attach_file</span>
            <span className="text-label-xs text-dash-on-surface truncate max-w-[200px]">{selectedFile.name}</span>
            <button
              onClick={() => setSelectedFile(null)}
              className="text-dash-on-surface-variant hover:text-dash-error transition-colors cursor-pointer"
              title="Remove file"
            >
              <span className="material-symbols-outlined text-sm">close</span>
            </button>
          </div>
        )}

        <div className="relative flex items-center gap-2">
          {/* Hidden File Input */}
          <input
            ref={fileInputRef}
            type="file"
            className="hidden"
            onChange={handleFileSelect}
          />

          {/* + File Upload Button */}
          <button
            onClick={() => fileInputRef.current?.click()}
            type="button"
            title="Attach a file"
            className="p-2 rounded-full text-dash-on-surface-variant hover:bg-dash-surface-variant hover:text-dash-primary transition-colors flex items-center justify-center cursor-pointer shrink-0"
          >
            <span className="material-symbols-outlined text-xl">add_circle</span>
          </button>

          <input
            className={`w-full bg-dash-surface-container-lowest border border-dash-outline-variant rounded-xl pl-4 pr-24 py-3 text-body-sm focus:ring-2 focus:ring-dash-primary focus:border-transparent outline-none transition-all placeholder:text-dash-outline ${
              isListening ? "ring-2 ring-emerald-500 border-emerald-500" : ""
            }`}
            placeholder={
              isListening ? "Listening... Speak now..." : "Type customer message or voice query..."
            }
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={isLoading}
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
              disabled={isLoading}
              title="Send Message"
              className="p-2 text-dash-primary hover:bg-dash-surface-variant rounded-full transition-colors flex items-center justify-center cursor-pointer disabled:opacity-50"
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

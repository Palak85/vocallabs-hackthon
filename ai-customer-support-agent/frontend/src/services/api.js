const BASE_URL = '/api';
const TENANT_HEADER = { 'X-Tenant-Id': 'default' };

export const api = {
  /**
   * Streams a chat message from backend via SSE (/api/v1/chat/stream).
   * Calls onEvent(eventType, data) as events (nlp, escalation_alert, token, sources, human_agent_active, done, error) arrive.
   */
  async streamMessage(message, conversationId = null, customerId = 'cust_web_user', onEvent = () => {}) {
    const payload = {
      question: message,
      text: message,
      message: message,
      customerId: customerId,
    };
    if (conversationId) {
      payload.conversationId = conversationId;
      payload.conversation_id = conversationId;
    }

    const res = await fetch(`${BASE_URL}/v1/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        ...TENANT_HEADER,
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Stream connection failed');
    }

    const reader = res.body?.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    if (!reader) throw new Error('ReadableStream not supported');

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const blocks = buffer.split('\n\n');
      buffer = blocks.pop() || '';

      for (const block of blocks) {
        if (!block.trim()) continue;
        const lines = block.split(/\r?\n/);
        let event = 'message';
        const dataLines = [];

        for (const line of lines) {
          if (line.startsWith('event:')) {
            event = line.substring(6).trim();
          } else if (line.startsWith('data:')) {
            // SSE standard: strip only the single optional space after "data:"
            const val = line.startsWith('data: ') ? line.substring(6) : line.substring(5);
            dataLines.push(val);
          }
        }

        const data = dataLines.join('\n');
        onEvent(event, data);
      }
    }
  },

  /**
   * Sends a chat message to the backend synchronously.
   */
  async sendMessage(message, conversationId = null, customerId = 'cust_web_user') {
    try {
      const v1Payload = {
        question: message,
        text: message,
        message: message,
        customerId: customerId,
      };
      if (conversationId) {
        v1Payload.conversationId = conversationId;
        v1Payload.conversation_id = conversationId;
      }

      const res = await fetch(`${BASE_URL}/v1/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          ...TENANT_HEADER,
        },
        body: JSON.stringify(v1Payload),
      });

      if (res.ok) {
        const data = await res.json();
        return {
          messageId: data.messageId || data.id,
          conversationId: data.conversationId || data.conversation_id,
          answer: data.answer || data.text || data.response || '',
          intent: data.intent,
          emotion: data.emotion,
          frustrationScore: data.frustrationScore,
          sources: data.sources || [],
          nlp: data.nlp,
        };
      }
    } catch (e) {
      console.warn('v1 chat failed, attempting fallback...', e);
    }

    // Fallback to /api/chat
    const legacyPayload = { message, question: message };
    if (conversationId) {
      legacyPayload.conversationId = conversationId;
    }

    const legacyRes = await fetch(`${BASE_URL}/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...TENANT_HEADER,
      },
      body: JSON.stringify(legacyPayload),
    });

    if (!legacyRes.ok) {
      const errorData = await legacyRes.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to send message');
    }

    return legacyRes.json();
  },

  /**
   * Uploads a document for ingestion into pgvector knowledge base.
   */
  async uploadDocument(file, category = 'general', title = '') {
    const formData = new FormData();
    formData.append('file', file);
    if (category) formData.append('category', category);
    if (title || file.name) formData.append('title', title || file.name);

    const res = await fetch(`${BASE_URL}/v1/documents`, {
      method: 'POST',
      headers: {
        ...TENANT_HEADER,
      },
      body: formData,
    });

    if (res.ok) {
      return res.json();
    }

    // Fallback
    const legacyRes = await fetch(`${BASE_URL}/documents`, {
      method: 'POST',
      headers: {
        ...TENANT_HEADER,
      },
      body: formData,
    });

    if (!legacyRes.ok) {
      const errorData = await legacyRes.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to upload document');
    }

    return legacyRes.json();
  },

  /**
   * Lists all uploaded knowledge base documents.
   */
  async listDocuments() {
    try {
      const res = await fetch(`${BASE_URL}/v1/documents`, {
        headers: {
          'Accept': 'application/json',
          ...TENANT_HEADER,
        },
      });
      if (res.ok) {
        return res.json();
      }
    } catch (e) {
      console.warn('v1 listDocuments failed, trying fallback...', e);
    }

    const legacyRes = await fetch(`${BASE_URL}/documents`, {
      headers: {
        'Accept': 'application/json',
        ...TENANT_HEADER,
      },
    });

    if (!legacyRes.ok) {
      throw new Error('Failed to fetch documents');
    }

    return legacyRes.json();
  },

  /**
   * Deletes a document by ID.
   */
  async deleteDocument(id) {
    try {
      const res = await fetch(`${BASE_URL}/v1/documents/${id}`, {
        method: 'DELETE',
        headers: {
          ...TENANT_HEADER,
        },
      });
      if (res.ok) return true;
    } catch (e) {
      console.warn('v1 deleteDocument failed, trying fallback...', e);
    }

    const legacyRes = await fetch(`${BASE_URL}/documents/${id}`, {
      method: 'DELETE',
      headers: {
        ...TENANT_HEADER,
      },
    });
    return legacyRes.ok;
  },

  /**
   * Checks the status of a document ingestion.
   */
  async getDocumentStatus(documentId) {
    try {
      const res = await fetch(`${BASE_URL}/v1/documents/${documentId}`, {
        headers: { 'Accept': 'application/json', ...TENANT_HEADER },
      });
      if (res.ok) return res.json();
    } catch (e) {}

    const response = await fetch(`${BASE_URL}/documents/${documentId}/status`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!response.ok) throw new Error('Failed to fetch document status');
    return response.json();
  },

  // ── Conversation & Call History Endpoints ──
  async listConversations() {
    const res = await fetch(`${BASE_URL}/v1/conversations`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!res.ok) return [];
    return res.json();
  },

  async getConversation(id) {
    const res = await fetch(`${BASE_URL}/v1/conversations/${id}`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!res.ok) throw new Error('Conversation not found');
    return res.json();
  },

  // ── Supervisor & Monitoring Console Endpoints ──
  async getMonitoringStats() {
    const res = await fetch(`${BASE_URL}/v1/monitoring/stats`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!res.ok) return null;
    return res.json();
  },

  async listMonitoredConversations() {
    const res = await fetch(`${BASE_URL}/v1/monitoring/conversations`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!res.ok) return [];
    return res.json();
  },

  async getMonitoringDetail(id) {
    const res = await fetch(`${BASE_URL}/v1/monitoring/conversations/${id}`, {
      headers: { 'Accept': 'application/json', ...TENANT_HEADER },
    });
    if (!res.ok) throw new Error('Monitored session not found');
    return res.json();
  },

  async takeoverConversation(id, agentName = 'Lead Specialist') {
    const res = await fetch(`${BASE_URL}/v1/monitoring/conversations/${id}/takeover`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...TENANT_HEADER,
      },
      body: JSON.stringify({ agentName }),
    });
    if (!res.ok) throw new Error('Takeover request failed');
    return res.json();
  },

  async handbackConversation(id) {
    const res = await fetch(`${BASE_URL}/v1/monitoring/conversations/${id}/handback`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        ...TENANT_HEADER,
      },
    });
    if (!res.ok) throw new Error('Handback request failed');
    return res.json();
  },

  async sendAgentMessage(id, message, agentName = 'Lead Specialist') {
    const res = await fetch(`${BASE_URL}/v1/monitoring/conversations/${id}/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...TENANT_HEADER,
      },
      body: JSON.stringify({ message, agentName }),
    });
    if (!res.ok) throw new Error('Failed to send supervisor message');
    return res.json();
  },

  getDocumentFileUrl(documentId) {
    return `${BASE_URL}/documents/${documentId}/file`;
  },

  getDocumentDownloadUrl(documentId) {
    return `${BASE_URL}/documents/${documentId}/download`;
  },

  /**
   * Subscribes to backend SSE stream (/api/v1/monitoring/stream) for real-time updates
   * (document_status, documents, stats, conversations, session_update) with ZERO polling.
   */
  subscribeToMonitoringEvents(onEvent = () => {}) {
    const url = `${BASE_URL}/v1/monitoring/stream`;
    const eventSource = new EventSource(url);

    const eventNames = ['init', 'stats', 'conversations', 'session_update', 'document_status', 'documents'];
    eventNames.forEach((evName) => {
      eventSource.addEventListener(evName, (e) => {
        try {
          const parsed = JSON.parse(e.data);
          onEvent(evName, parsed);
        } catch (_) {
          onEvent(evName, e.data);
        }
      });
    });

    eventSource.onerror = (err) => {
      console.warn('SSE monitoring stream re-connecting...', err);
    };

    return () => {
      eventSource.close();
    };
  },
};

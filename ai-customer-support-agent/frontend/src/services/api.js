const BASE_URL = '/api';
const TENANT_HEADER = { 'X-Tenant-Id': 'default' };

export const api = {
  /**
   * Sends a chat message to the backend (supports v1 and legacy endpoints).
   * @param {string} message - User's question or message.
   * @param {string} conversationId - Existing conversation ID (optional).
   * @param {string} customerId - Customer identifier (optional).
   * @returns {Promise<Object>} - Response containing messageId, conversationId, and answer.
   */
  async sendMessage(message, conversationId = null, customerId = 'cust_web_user') {
    // Try v1 endpoint first
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
        };
      }
    } catch (e) {
      console.warn('v1 chat failed, attempting legacy chat...', e);
    }

    // Fallback to /api/chat
    const legacyPayload = { message };
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
   * @param {File} file - The file to upload.
   * @param {string} category - Document category (e.g. banking, ecommerce, general).
   * @param {string} title - Optional document title.
   * @returns {Promise<Object>}
   */
  async uploadDocument(file, category = 'general', title = '') {
    const formData = new FormData();
    formData.append('file', file);
    if (category) formData.append('category', category);
    if (title || file.name) formData.append('title', title || file.name);

    // Try /api/v1/documents first
    try {
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
    } catch (e) {
      console.warn('v1 upload failed, trying legacy upload...', e);
    }

    // Fallback to /api/documents
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
   * @returns {Promise<Array>}
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
      console.warn('v1 listDocuments failed, trying legacy...', e);
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
   * @param {string} id
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
      console.warn('v1 deleteDocument failed, trying legacy...', e);
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
   * @param {string} documentId
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
};

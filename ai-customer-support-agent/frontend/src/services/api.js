const BASE_URL = '/api';

export const api = {
  /**
   * Sends a chat message to the backend.
   * @param {string} message - User's question or message.
   * @param {string} conversationId - Existing conversation ID (optional).
   * @returns {Promise<Object>} - Response containing messageId, conversationId, and answer.
   */
  async sendMessage(message, conversationId = null) {
    const payload = { message };
    if (conversationId) {
      payload.conversationId = conversationId;
    }

    const response = await fetch(`${BASE_URL}/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to send message');
    }

    return response.json();
  },

  /**
   * Uploads a document for ingestion.
   * @param {File} file - The file to upload.
   * @returns {Promise<Object>} - Response containing documentId, filename, status, message.
   */
  async uploadDocument(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${BASE_URL}/documents`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to upload document');
    }

    return response.json();
  },

  /**
   * Checks the status of a document ingestion.
   * @param {string} documentId - The document identifier.
   * @returns {Promise<Object>} - Status response.
   */
  async getDocumentStatus(documentId) {
    const response = await fetch(`${BASE_URL}/documents/${documentId}/status`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch document status');
    }

    return response.json();
  },

  /**
   * Lists all documents.
   * @returns {Promise<Array>} - List of documents.
   */
  async listDocuments() {
    const response = await fetch(`${BASE_URL}/documents`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch documents');
    }

    return response.json();
  },

  /**
   * Returns the direct URL to view / stream the original document.
   * @param {string} documentId - The document identifier.
   * @returns {string} - Direct URL for streaming/viewing.
   */
  getDocumentFileUrl(documentId) {
    return `${BASE_URL}/documents/${documentId}/file`;
  },

  /**
   * Returns the direct URL to download the original document.
   * @param {string} documentId - The document identifier.
   * @returns {string} - Direct URL for download.
   */
  getDocumentDownloadUrl(documentId) {
    return `${BASE_URL}/documents/${documentId}/download`;
  }
};

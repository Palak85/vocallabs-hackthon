import { useState, useEffect, useRef } from "react";
import { api } from "../../services/api";

const CATEGORIES = ["general", "banking", "ecommerce", "technical", "policies"];

export default function KnowledgeBasePanel() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("general");
  const [docTitle, setDocTitle] = useState("");
  const [dragActive, setDragActive] = useState(false);
  const [statusMsg, setStatusMsg] = useState(null);

  const fileInputRef = useRef(null);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const docs = await api.listDocuments();
      setDocuments(Array.isArray(docs) ? docs : []);
    } catch (err) {
      console.error("Error loading documents:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
    // Poll ingestion status every 4 seconds as specified in frontend guide
    const interval = setInterval(fetchDocuments, 4000);
    return () => clearInterval(interval);
  }, []);

  const handleFileUpload = async (file) => {
    if (!file) return;
    try {
      setUploading(true);
      setStatusMsg({ type: "info", text: `Uploading and chunking "${file.name}"...` });
      await api.uploadDocument(file, selectedCategory, docTitle || file.name);
      setStatusMsg({ type: "success", text: `Document "${file.name}" ingested successfully!` });
      setDocTitle("");
      fetchDocuments();
    } catch (err) {
      setStatusMsg({ type: "error", text: `Upload failed: ${err.message}` });
    } finally {
      setUploading(false);
      setTimeout(() => setStatusMsg(null), 5000);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Are you sure you want to delete this document from the vector store?")) return;
    try {
      await api.deleteDocument(id);
      fetchDocuments();
    } catch (err) {
      alert("Failed to delete document: " + err.message);
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileUpload(e.dataTransfer.files[0]);
    }
  };

  return (
    <div className="flex-1 flex flex-col bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
      {/* Panel Header */}
      <div className="p-4 border-b border-slate-200 bg-slate-50/80 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#006a6a]/10 text-[#006a6a] flex items-center justify-center">
            <span className="material-symbols-outlined text-lg">folder_data</span>
          </div>
          <div>
            <h2 className="font-bold text-sm text-slate-800">Knowledge Base Ingestion</h2>
            <p className="text-[11px] text-slate-500">Vector Embeddings & Document Management</p>
          </div>
        </div>
        <button
          onClick={fetchDocuments}
          title="Refresh Documents"
          className="p-1.5 text-slate-500 hover:text-[#006a6a] hover:bg-white rounded-lg transition-colors cursor-pointer"
        >
          <span className="material-symbols-outlined text-lg">refresh</span>
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-5">
        {/* Upload Form */}
        <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 flex flex-col gap-3">
          <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
            <span className="material-symbols-outlined text-base text-[#006a6a]">upload_file</span>
            Upload New Document
          </h3>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-[11px] font-semibold text-slate-600 mb-1 block">Category</label>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full bg-white border border-slate-300 rounded-lg px-2.5 py-1.5 text-xs text-slate-800 outline-none focus:border-[#006a6a]"
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>
                    {c.toUpperCase()}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-semibold text-slate-600 mb-1 block">Title (Optional)</label>
              <input
                type="text"
                placeholder="e.g. UPI Refund Policy"
                value={docTitle}
                onChange={(e) => setDocTitle(e.target.value)}
                className="w-full bg-white border border-slate-300 rounded-lg px-2.5 py-1.5 text-xs text-slate-800 outline-none focus:border-[#006a6a]"
              />
            </div>
          </div>

          {/* Drag & Drop Zone */}
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer transition-all ${
              dragActive
                ? "border-[#006a6a] bg-[#eef5f4]"
                : "border-slate-300 bg-white hover:border-[#006a6a] hover:bg-slate-50/80"
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.docx,.txt,.md"
              className="hidden"
              onChange={(e) => {
                if (e.target.files?.[0]) handleFileUpload(e.target.files[0]);
                e.target.value = "";
              }}
            />
            <span className="material-symbols-outlined text-3xl text-[#006a6a] mb-1">
              {uploading ? "progress_activity" : "cloud_upload"}
            </span>
            <p className="text-xs font-bold text-slate-700">
              {uploading ? "Ingesting & Chunking Document..." : "Click or Drag & Drop File Here"}
            </p>
            <p className="text-[10px] text-slate-400 mt-0.5">
              Supports .PDF, .DOCX, .TXT, .MD (Max 20MB)
            </p>
          </div>

          {/* Status Message */}
          {statusMsg && (
            <div
              className={`p-2.5 rounded-lg text-xs font-medium flex items-center gap-2 ${
                statusMsg.type === "error"
                  ? "bg-red-50 text-red-700 border border-red-200"
                  : statusMsg.type === "success"
                  ? "bg-emerald-50 text-emerald-800 border border-emerald-200"
                  : "bg-blue-50 text-blue-800 border border-blue-200"
              }`}
            >
              <span className="material-symbols-outlined text-sm">
                {statusMsg.type === "error" ? "error" : "check_circle"}
              </span>
              {statusMsg.text}
            </div>
          )}
        </div>

        {/* Ingested Documents List */}
        <div className="flex-1 flex flex-col">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
              <span className="material-symbols-outlined text-base text-[#006a6a]">library_books</span>
              Ingested Knowledge Base ({documents.length})
            </h3>
          </div>

          {loading && documents.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400">Loading documents...</div>
          ) : documents.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400 bg-slate-50 rounded-xl border border-dashed border-slate-200">
              No documents in vector database yet. Upload a document above to empower the AI!
            </div>
          ) : (
            <div className="flex flex-col gap-2">
              {documents.map((doc) => {
                const status = (doc.status || "COMPLETED").toUpperCase();
                const isProcessing = status === "PENDING" || status === "PROCESSING";
                const isFailed = status === "FAILED";

                return (
                  <div
                    key={doc.id}
                    className="p-3 bg-white rounded-xl border border-slate-200 hover:border-slate-300 shadow-2xs transition-all flex items-center justify-between gap-3"
                  >
                    <div className="flex items-center gap-3 overflow-hidden">
                      <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0 text-slate-600">
                        <span className="material-symbols-outlined text-lg">description</span>
                      </div>
                      <div className="overflow-hidden">
                        <h4 className="text-xs font-bold text-slate-800 truncate">
                          {doc.title || doc.filename || "Untitled Document"}
                        </h4>
                        <div className="flex items-center gap-2 text-[10px] text-slate-400 mt-0.5">
                          <span className="capitalize">{doc.category || "General"}</span>
                          <span>•</span>
                          <span>{doc.chunkCount || doc.chunk_count || 0} chunks</span>
                          {doc.size && (
                            <>
                              <span>•</span>
                              <span>{(doc.size / 1024).toFixed(1)} KB</span>
                            </>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      {/* Status pill */}
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold flex items-center gap-1 ${
                          isProcessing
                            ? "bg-amber-100 text-amber-800 border border-amber-200"
                            : isFailed
                            ? "bg-red-100 text-red-800 border border-red-200"
                            : "bg-emerald-100 text-emerald-800 border border-emerald-200"
                        }`}
                      >
                        {isProcessing && (
                          <span className="material-symbols-outlined text-[10px] animate-spin">
                            progress_activity
                          </span>
                        )}
                        {isProcessing ? "Processing" : isFailed ? "Failed" : "Ready"}
                      </span>

                      {/* Delete */}
                      <button
                        onClick={() => handleDelete(doc.id)}
                        className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors cursor-pointer"
                        title="Delete Document"
                      >
                        <span className="material-symbols-outlined text-base">delete</span>
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

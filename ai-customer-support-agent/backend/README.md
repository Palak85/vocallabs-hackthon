# AI-Powered Chat QnA Backend

This is the Spring Boot backend service for the AI-powered conversational QnA system.

## Key Features
- **Asynchronous Document Ingestion**: Multi-format document parsing (PDF, TXT), chunking, and embedding generation in background worker threads.
- **pgvector + Spring AI Integration**: Native vector similarity search backed by PostgreSQL with pgvector extension.
- **Gemini LLM Orchestration**: Grounded QnA using Google Gemini 2.5 Flash via Spring AI.
- **NLP Analysis Layer**: Pluggable NLP interface with deterministic keyword-based mock and HTTP microservice integration.
- **Policy / Refusal Engine**: Deterministic policy evaluation before retrieval/LLM execution to block prompt injections and exploits.
- **Invisible Internal Citations & Auditing**: Tracks supporting document chunks, similarity rankings, and token usage without leaking metadata to the public UI.

## Quick Links
- [API Documentation](../docs/api.md)
- [Startup & Deployment Guide](../docs/backend-startup-guide.md)

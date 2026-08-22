"""
FastAPI Microservice Main Entry Point.
Initializes application lifecycle, loads models into memory once in lifespan, and mounts API routes.
"""

from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.database import init_db
from app.routes.nlp import router as nlp_router
from app.routes.health import router as health_router
from app.routes.model_info import router as model_info_router
from app.routes.conversation import router as conversation_router
from app.services.inference import pipeline
from app.utils.logger import get_logger

logger = get_logger("main")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting up NLP Microservice...")
    # Initialize database tables
    init_db()
    # Attach NLP Pipeline to application state
    app.state.pipeline = pipeline
    logger.info("NLP Microservice startup complete with models preloaded into memory.")
    yield
    logger.info("Shutting down NLP Microservice...")


app = FastAPI(
    title="AI-Powered Multi-Domain Customer Support NLP Microservice",
    version="1.0.0",
    description="Microservice for real-time domain detection, hierarchical intent classification, emotion/frustration analysis, and hybrid NER.",
    lifespan=lifespan
)

# CORS middleware for development and integration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Routes
@app.get("/", tags=["Root"])
def root_index():
    return {
        "service": "AI-Powered Customer Support NLP Microservice",
        "status": "running",
        "documentation": "/docs",
        "openapi_schema": "/openapi.json",
        "health": "/api/health"
    }

app.include_router(health_router)
app.include_router(model_info_router)
app.include_router(nlp_router)
app.include_router(conversation_router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)

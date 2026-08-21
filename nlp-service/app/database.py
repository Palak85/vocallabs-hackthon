"""
Database Connection & Session Management.
Uses SQLAlchemy 2.0 with PostgreSQL connection pooling and automatic fallback to SQLite for local tests.
"""

import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from app.utils.logger import get_logger

logger = get_logger("database")

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./nlp_local.db")

# SQLite connection args vs PostgreSQL connection args
connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}

engine = create_engine(
    DATABASE_URL,
    echo=False,
    connect_args=connect_args,
    pool_pre_ping=True
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db():
    """FastAPI Dependency for database session."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db():
    """Initializes all database tables."""
    try:
        Base.metadata.create_all(bind=engine)
        logger.info(f"Database initialized successfully at {DATABASE_URL}")
    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")

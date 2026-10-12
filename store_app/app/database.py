from sqlmodel import SQLModel, Session, create_engine
import os
from dotenv import load_dotenv

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")
# SQLite için gerekli parametre. FastAPI'ya uygun olarak birden fazla thread kullanmadığından kaynaklı.
# connect_args = {"check_same_thread": False} # Bu parametre bu engeli kaldırır.

engine = create_engine(DATABASE_URL, echo=True) # , connect_args={connect_args})

def create_db_and_tables():
    SQLModel.metadata.create_all(engine)

def get_session():
    with Session(engine) as session:
        yield session
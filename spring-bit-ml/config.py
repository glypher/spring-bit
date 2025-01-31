import os
from pydantic_settings import BaseSettings
from dotenv import load_dotenv

# Load the correct environment file
env_file = f".env.{os.getenv('ENV', 'development')}"
load_dotenv(env_file)

class Settings(BaseSettings):
    ENV: str
    KAFKA_BOOTSTRAP_SERVERS: str
    TOPIC_NAME: str
    DEBUG: bool

    class Config:
        env_file = env_file

settings = Settings()
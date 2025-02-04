import os
from pydantic_settings import BaseSettings
from dotenv import load_dotenv

# Load the correct environment file
env_file = f"config/.env.{os.getenv('ENV', 'development')}"
load_dotenv(env_file)

class Settings(BaseSettings):
    ENV: str
    KAFKA_BOOTSTRAP_SERVERS: str
    TOPIC_CRYPTO: str
    TOPIC_ACTION: str
    CRYPTO_SERVICE: str
    LOG_DIR: str
    LOG_LEVEL: str

    class Config:
        env_file = env_file

settings = Settings()

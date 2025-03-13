from typing import List, Dict

from pydantic import BaseModel, Field
from datetime import datetime

class Crypto(BaseModel):
    name: str
    symbol: str
    quoteDate: datetime = Field(..., description="Timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    quotePrice: float

    class Config:
        json_encoders = {datetime: lambda v: v.strftime("%Y-%m-%dT%H:%M:%SZ")}
        from_attributes = True

class CryptoAction(BaseModel):
    name: str
    symbol: str
    operation: str
    quantity: float
    quoteDate: datetime = Field(..., description="Timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    quotePrice: float

    class Config:
        json_encoders = {datetime: lambda v: v.strftime("%Y-%m-%dT%H:%M:%SZ")}
        from_attributes = True

class CryptoInfoRequest(BaseModel):
    start_date: datetime = Field(..., description="Start date timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    end_date: datetime = Field(..., description="End date timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    symbols: List[str] = Field(..., description="List of crypto symbols like BTC, ETH")

class CryptoInfo(BaseModel):
    correlation: Dict[str, Dict[str, float]]  # Outer dict: symbol -> (Inner dict: symbol -> correlation)

class ChatRequest(BaseModel):
    prompt: str
    history: List[Dict[str, str]]

class ChatReply(BaseModel):
    reply: str

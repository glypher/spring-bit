from pydantic import BaseModel, Field
from datetime import datetime

class Crypto(BaseModel):
    name: str
    symbol: str
    quoteDate: datetime = Field(..., description="Timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    quotePrice: float

    class Config:
        json_encoders = {datetime: lambda v: v.strftime("%Y-%m-%dT%H:%M:%SZ")}
        orm_mode = True

class CryptoAction(BaseModel):
    name: str
    symbol: str
    operation: str
    quantity: float
    quoteDate: datetime = Field(..., description="Timestamp in format: yyyy-MM-ddTHH:mm:ssZ")
    quotePrice: float

    class Config:
        json_encoders = {datetime: lambda v: v.strftime("%Y-%m-%dT%H:%M:%SZ")}
        orm_mode = True

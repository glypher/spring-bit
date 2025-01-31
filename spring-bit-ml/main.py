from fastapi import FastAPI
from crypto.crypto_service import CryptoService
import logging

logging.basicConfig(level=logging.DEBUG)

app = FastAPI()

crypto_service = CryptoService()


@app.get("/")
async def root():
    return {"message": "Springbit ML crypto predictions"}


@app.post("/crypto/{symbol}/predict")
async def start_predictions(symbol: str):
    status = await crypto_service.start_predict(symbol)
    return {'status': 'ok'}

@app.post("/crypto/{symbol}/stop")
async def stop_predictions(symbol: str):
    status = await crypto_service.stop(symbol)
    return {'status': 'ok'}

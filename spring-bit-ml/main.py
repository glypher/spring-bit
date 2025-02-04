import sys

from fastapi import FastAPI

from config.config import settings
from crypto.crypto_service import CryptoService
import logging
from logging.handlers import TimedRotatingFileHandler
from pathlib import Path


logger = logging.getLogger('springbit-ml')
logger.setLevel(settings.LOG_LEVEL)

if settings.LOG_DIR:
    log_dir = settings.LOG_DIR
else:
    log_dir = Path(__file__).resolve().parent / 'logs'
    log_dir.mkdir(exist_ok=True)

handler = TimedRotatingFileHandler(f'{log_dir}/springbit-ml.log', when='midnight', interval=1, backupCount=7)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)
logger.addHandler(logging.StreamHandler(sys.stdout))

app = FastAPI()

crypto_service = CryptoService()

crypto_service.start_listener()

@app.get("/")
async def root():
    return {"message": "Springbit ML crypto predictions"}


@app.post("/crypto/{symbol}/predict")
async def start_predictions(symbol: str):
    status = await crypto_service.start_predict(symbol)
    return {'status': status}

@app.post("/crypto/{symbol}/stop")
async def stop_predictions(symbol: str):
    status = await crypto_service.stop(symbol)
    return {'status': status}

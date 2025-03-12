import sys

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from config.config import settings
from crypto.crypto_service import CryptoService
from crypto.dto.types import CryptoInfoRequest, CryptoInfo, ChatRequest, ChatReply
from crypto.statistics.CryptoSeries import CryptoSeries
from crypto.chat.Chat import Chat
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

# Add CORSMiddleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins (use specific domains for production)
    allow_credentials=True,
    allow_methods=["*"],  # Allow all HTTP methods (GET, POST, etc.)
    allow_headers=["*"],  # Allow all headers
)

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

@app.post("/crypto/info")
async def crypto_info(request: CryptoInfoRequest) -> CryptoInfo:
    try:
        return CryptoSeries.get().correlation(request.symbols, request.start_date, request.end_date)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/crypto/chat")
async def crypto_chat(request: ChatRequest) -> ChatReply:
    try:
        return await Chat.get().chat(request)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
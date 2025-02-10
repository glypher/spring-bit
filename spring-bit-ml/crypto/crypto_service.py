import asyncio
import logging
from typing import List

import httpx

from config.config import settings
from crypto import ModelRegistry
from crypto.dto.types import Crypto, CryptoAction
from crypto.kafka_service import KafkaService


class CryptoService:
    def __init__(self):
        self._tasks = {}
        self._logger = logging.getLogger('springbit-ml')
        # Add a listening topic for actions
        self._kafka_listener = KafkaService(settings.TOPIC_ACTION)

    @staticmethod
    async def fetch_crypto_quote(symbol: str) -> List[Crypto]:
        async with httpx.AsyncClient() as client:
            response = await client.get(settings.CRYPTO_SERVICE + f'/crypto/{symbol}/quote')
            data = response.json()

            return [Crypto.model_validate(item) for item in data]

    def start_listener(self):
        asyncio.create_task(self._kafka_listener.start_consumer(self._listen_actions))

    async def start_predict(self, symbol: str):
        task = self._tasks.get(symbol)
        if not task:
            task = {'count': 1,
                    'symbol': symbol,
                    'kafka': KafkaService(f'{settings.TOPIC_CRYPTO}-{symbol}'),
                    'model': settings.MODEL,
                    'task': None }
            task['task'] = asyncio.create_task(self._consume_model(task))
            self._tasks[symbol] = task
        else:
            task['count'] += 1

        self._logger.info(f"Started predict for {symbol} count={task['count']}")
        return 'ok'

    async def stop(self, symbol: str):
        task = self._tasks.get(symbol)
        if task:
            task['count'] -= 1
            if task['count'] <= 0:
                try:
                    task['task'].cancel()
                    await asyncio.wait_for(task['task'], timeout=3)
                    return "ok"
                except asyncio.CancelledError:
                    self._logger.error("Caught task cancellation.")
                finally:
                    self._logger.info(f"Stopped predict for {symbol} count={task['count']}")
                    del self._tasks[symbol]
        return "failed"

    async def _consume_model(self, task):
        kafka = task['kafka']
        symbol = task['symbol']
        await kafka.start_producer()

        model = task.get('model', 'random')
        try:
            # get the latest quote
            cryptos = await CryptoService.fetch_crypto_quote(symbol)
        except Exception as e:
            cryptos = None
            self._logger.error(f"Exception in getting crypto quotes {e}")

        model = ModelRegistry.get_model(model, cryptos=cryptos)
        task['model'] = model
        async for crypto in model.predict(symbol=symbol, delay=1):
            try:
                await kafka.produce(crypto)
            except Exception as e:
                self._logger.error(f"Exception in predict {e}")

    async def _listen_actions(self, message: str):
        try:
            crypto_action = CryptoAction.model_validate(message)
            if crypto_action.operation == 'predict':
                await self.start_predict(crypto_action.symbol)
                return
            if crypto_action.operation == 'stop':
                await self.stop(crypto_action.symbol)

            task = self._tasks.get(crypto_action.symbol, None)
            if task and 'model' in task:
                self._logger.info(f'Received crypto action {crypto_action}')
                await task['model'].receive(action=crypto_action)
        except Exception as e:
            self._logger.warning(f"Error in receive crypto action {e}")

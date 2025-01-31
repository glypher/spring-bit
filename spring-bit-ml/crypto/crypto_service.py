import asyncio
import json
import logging

from crypto import ModelRegistry
from crypto.kafka_service import KafkaService


class CryptoService:
    def __init__(self):
        self.kafka = KafkaService()
        self._tasks = {}

    async def _consume_model(self, symbol):
        await self.kafka.start_producer()
        model = ModelRegistry.get_model('random')
        async for crypto in model.predict(symbol=symbol, min=10000, max=200000, delay=3):
            try:
                await self.kafka.produce(crypto)
            except Exception as e:
                logging.error(f"Exception in predict {e}")

    async def start_predict(self, symbol: str):
        task = self._tasks.get(symbol)
        if not task:
            self._tasks[symbol] = {'count': 1, 'task': asyncio.create_task(self._consume_model(symbol)) }
        else:
            task['count'] += 1

    async def stop(self, symbol: str):
        task = self._tasks.get(symbol)
        if task:
            task['count'] -= 0
            if task['count'] == 0:
                try:
                    task['task'].cancel()
                    await asyncio.wait_for(task['task'], timeout=5)
                except asyncio.CancelledError:
                    print("Caught task cancellation.")
                finally:
                    del task[symbol]

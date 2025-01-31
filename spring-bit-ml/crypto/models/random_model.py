import asyncio
import datetime
import random
from typing import AsyncIterable

from crypto import register_model
from crypto.crypto_model import CryptoModel
from crypto.dto.types import Crypto


@register_model(name='random')
class RandomModel(CryptoModel):
    def __init__(self):
        pass

    async def predict(self, **kwargs) -> AsyncIterable[Crypto]:
        min_interval = kwargs.get('min', 10000)
        max_interval = kwargs.get('max', 200000)
        delay = kwargs.get('delay', 3)
        symbol = kwargs.get('symbol', 'BTC')
        while True:
            price = random.uniform(min_interval, max_interval)
            crypto = Crypto(name=symbol, symbol=symbol, quoteDate=datetime.datetime.now(), quotePrice=price)
            yield crypto
            await asyncio.sleep(delay)

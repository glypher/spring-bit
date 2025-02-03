import asyncio
import datetime
import random
from typing import AsyncIterable

from crypto import register_model
from crypto.crypto_model import CryptoModel
from crypto.dto.types import Crypto


@register_model(name='random')
class RandomModel(CryptoModel):
    def __init__(self, **kwargs):
        cryptos = kwargs.get('cryptos')
        self._last_price = cryptos[0].quotePrice if cryptos else 0

    async def predict(self, **kwargs) -> AsyncIterable[Crypto]:
        min_per = kwargs.get('min_percentage', 0.8)
        max_per = kwargs.get('max_percentage', 1.2)
        delay = kwargs.get('delay', 1)
        symbol = kwargs.get('symbol', 'BTC')
        while True:

            price = random.uniform(round(self._last_price * min_per)-1, round(self._last_price * max_per)+1)
            crypto = Crypto(name=symbol, symbol=symbol, quoteDate=datetime.datetime.now(), quotePrice=price)
            self._last_price = price
            yield crypto
            await asyncio.sleep(delay)

    async def receive(self, **kwargs) -> AsyncIterable[Crypto]:
        pass

import asyncio
import datetime
from typing import AsyncIterable

from crypto import register_model
from crypto.crypto_model import CryptoModel
from crypto.dto.types import Crypto
from crypto.statistics.CryptoSeries import CryptoSeries


@register_model(name='historical')
class HistoricalModel(CryptoModel):
    def __init__(self, **kwargs):
        cryptos = kwargs.get('cryptos')
        self._last_price = cryptos[0].quotePrice if cryptos else 0
        self._series = CryptoSeries.get()

    async def predict(self, **kwargs) -> AsyncIterable[Crypto]:
        delay = kwargs.get('delay', 1)
        symbol = kwargs.get('symbol', 'BTC')
        period = kwargs.get('period', 20)
        while True:
            # get a 20 days period
            changes = self._series.get_change_series(symbol, period)
            for ch in changes:
                price = self._last_price * (1. + ch / 100.)
                crypto = Crypto(name=symbol, symbol=symbol, quoteDate=datetime.datetime.now(), quotePrice=price)
                self._last_price = price
                yield crypto
                await asyncio.sleep(delay)

    async def receive(self, **kwargs) -> AsyncIterable[Crypto]:
        pass

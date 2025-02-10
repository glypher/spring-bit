from datetime import datetime
from pathlib import Path
import random

import pandas as pd


class CryptoSeries:
    _dfs = {}

    _instance_ = None

    @staticmethod
    def get():
        if CryptoSeries._instance_ is None:
            CryptoSeries._instance_ = CryptoSeries()
        return CryptoSeries._instance_

    def _load_series(self, symbol: str):
        symbol = symbol.upper()
        if symbol in self._dfs:
            return self._dfs[symbol]

        data_dir = Path(__file__).resolve().parent / '../../data'
        df = pd.read_csv(f'{data_dir}/{symbol}.csv', dtype=str)
        df['Date'] = pd.to_datetime(df["Date"])
        df['Open'] = df['Open'].str.replace(',', '').astype(float)
        df['Low'] = df['Low'].str.replace(',', '').astype(float)
        df['High'] = df['High'].str.replace(',', '').astype(float)
        df['Change %'] = df['Change %'].str.rstrip("%").astype(float)

        df = df[df['Date'] > datetime.strptime('2017-01-01', "%Y-%m-%d")]
        self._dfs[symbol] = df
        return df

    def get_change_series(self, symbol: str, count: int):
        df = self._load_series(symbol)
        last = len(df) - count
        start = round(random.uniform(0, last))
        return list(df['Change %'][start:start+count])

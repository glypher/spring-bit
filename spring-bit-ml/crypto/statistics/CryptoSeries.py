from datetime import datetime
from pathlib import Path
import random

import pandas as pd
import numpy as np

from crypto.dto.types import CryptoInfo


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

    def correlation(self, symbols: list, start: datetime, end: datetime):
        start = pd.to_datetime(start.replace(tzinfo=None))
        end = pd.to_datetime(end.replace(tzinfo=None))
        full_range = pd.date_range(start=start, end=end)

        change = []
        missing = []
        df_range = {}
        for symbol in symbols:
            try:
                df = self._load_series(symbol)
            except Exception as e:
                raise Exception(f'{symbol} data not found')

            df = df[(df['Date'] >= start) & (df['Date'] <= end)]

            missing += full_range.difference(df['Date']).to_list()
            df_range[symbol] = df

        for symbol in symbols:
            df =  df_range[symbol]
            df = df[~df['Date'].isin(missing)]
            if len(df) == 0:
                raise Exception(f'Insufficient data for {symbol}')
            change.append(df['Change %'].to_numpy())

        # Compute correlation matrix
        data = np.vstack(change)
        corr_matrix = np.corrcoef(data)

        result = CryptoInfo(correlation={})
        for i in range(len(symbols)):
            corr = {}
            for j in range(i+1, len(symbols)):
                corr[symbols[j]] = corr_matrix[i][j]
            result.correlation[symbols[i]] = corr

        return result

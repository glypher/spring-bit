from abc import ABC, abstractmethod
from typing import AsyncIterable

from crypto.dto.types import Crypto


class CryptoModel(ABC):
    @abstractmethod
    async def predict(self, **kwargs) -> AsyncIterable[Crypto]:
        pass

    @abstractmethod
    async def receive(self, **kwargs) -> AsyncIterable[Crypto]:
        pass

class ModelRegistry:
    _models = {}

    @staticmethod
    def get_model(name: str, **kwargs) -> CryptoModel:
        if name not in ModelRegistry._models:
            raise Exception(f"Model {name} not found!")
        return ModelRegistry._models[name](**kwargs)

    @staticmethod
    def register_model(name, model: CryptoModel):
        ModelRegistry._models[name] = model

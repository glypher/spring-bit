from .crypto_model import ModelRegistry

def register_model(**kwargs):
    def decorator(cls):
        ModelRegistry.register_model(kwargs.get('name', cls.__name__.lower()), cls)
        return cls
    return decorator

import crypto.models

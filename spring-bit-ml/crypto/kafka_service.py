import asyncio
import logging

from aiokafka import AIOKafkaProducer, AIOKafkaConsumer
import json

from aiokafka.errors import KafkaConnectionError
from pydantic import BaseModel

from config.config import settings

class KafkaService:
    def __init__(self, topic: str):
        self.bootstrap_servers = settings.KAFKA_BOOTSTRAP_SERVERS
        self.topic = topic
        self.producer = None
        self.consumer = None
        self._logger = logging.getLogger('springbit-ml')

    async def start_producer(self):
        if self.producer:
            return

        """Initialize the Kafka Producer"""
        self.producer = AIOKafkaProducer(
            bootstrap_servers=self.bootstrap_servers,
            value_serializer=lambda v: v.model_dump_json().encode('utf-8')
        )
        await self.producer.start()
        self._logger.info(f"Kafka Producer started on {self.bootstrap_servers}")

    async def produce(self, message: BaseModel):
        """Send a message to the Kafka topic"""
        while True:
            try:
                if not self.producer:
                    await self.start_producer()

                await self.producer.send_and_wait(self.topic, message)
                self._logger.debug(f"Sent: {message.model_dump_json()}")
                break
            except KafkaConnectionError as e:
                self._logger.error(f"Connection error: {e}")
                self._logger.info("Retrying connection in 5 seconds...")
                await asyncio.sleep(5)
            except Exception as e:
                self._logger.error(f"Error sending message: {e}")
                break

    async def start_consumer(self, callback):
        if self.consumer:
            return

        """Start Kafka Consumer and process messages"""
        self.consumer = AIOKafkaConsumer(
            self.topic,
            bootstrap_servers=self.bootstrap_servers,
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            auto_offset_reset="latest", # earliest
        )
        while True:
            try:
                await self.consumer.start()
            except KafkaConnectionError as e:
                self._logger.error(f"Connection error: {e}")
                self._logger.info("Retrying connection in 5 seconds...")
                await asyncio.sleep(5)
                continue

            self._logger.info(f"Kafka Consumer started on {self.bootstrap_servers}")

            try:
                async for msg in self.consumer:
                    self._logger.info(f"Received: {msg.value}")
                    await callback(msg.value)  # Process message with callback
            except KafkaConnectionError as e:
                self._logger.error(f"Connection error: {e}")
                self._logger.info("Retrying connection in 5 seconds...")
                await asyncio.sleep(5)
                continue
            finally:
                await self.consumer.stop()

    async def stop_producer(self):
        """Shutdown Kafka Producer"""
        if self.producer:
            await self.producer.stop()
            self._logger.info("Kafka Producer stopped.")

    async def stop_consumer(self):
        """Shutdown Kafka Consumer"""
        if self.consumer:
            await self.consumer.stop()
            self._logger.info("Kafka Consumer stopped.")
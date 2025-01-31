from aiokafka import AIOKafkaProducer, AIOKafkaConsumer
import json

from pydantic import BaseModel

from config import settings

class KafkaService:
    def __init__(self):
        self.bootstrap_servers = settings.KAFKA_BOOTSTRAP_SERVERS
        self.topic = settings.TOPIC_NAME
        self.producer = None
        self.consumer = None

    async def start_producer(self):
        if self.producer:
            return

        """Initialize the Kafka Producer"""
        self.producer = AIOKafkaProducer(
            bootstrap_servers=self.bootstrap_servers,
            value_serializer=lambda v: v.model_dump_json().encode('utf-8')
        )
        await self.producer.start()
        print(f"✅ Kafka Producer started on {self.bootstrap_servers}")

    async def produce(self, message: BaseModel):
        """Send a message to the Kafka topic"""
        if not self.producer:
            await self.start_producer()

        try:
            await self.producer.send_and_wait(self.topic, message)
            print(f"📤 Sent: {message.model_dump_json()}")
        except Exception as e:
            print(f"❌ Error sending message: {e}")

    async def start_consumer(self, callback):
        """Start Kafka Consumer and process messages"""
        self.consumer = AIOKafkaConsumer(
            self.topic,
            bootstrap_servers=self.bootstrap_servers,
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            auto_offset_reset="earliest",
        )
        await self.consumer.start()
        print(f"✅ Kafka Consumer started on {self.bootstrap_servers}")

        try:
            async for msg in self.consumer:
                print(f"📥 Received: {msg.value}")
                await callback(msg.value)  # Process message with callback
        finally:
            await self.consumer.stop()

    async def stop_producer(self):
        """Shutdown Kafka Producer"""
        if self.producer:
            await self.producer.stop()
            print("🔴 Kafka Producer stopped.")

    async def stop_consumer(self):
        """Shutdown Kafka Consumer"""
        if self.consumer:
            await self.consumer.stop()
            print("🔴 Kafka Consumer stopped.")
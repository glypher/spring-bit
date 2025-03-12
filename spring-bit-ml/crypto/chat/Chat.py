from langchain_core.messages import HumanMessage
from langchain.chat_models import ChatOpenAI

from crypto.dto.types import ChatRequest, ChatReply


class Chat:
    _instance_ = None

    @staticmethod
    def get():
        if Chat._instance_ is None:
            Chat._instance_ = Chat()
        return Chat._instance_

    def __init__(self):
        self._chat_model = ChatOpenAI(model_name="gpt-3.5-turbo")

    async def chat(self, chat: ChatRequest) -> ChatReply:
        response = await self._chat_model.agenerate([[HumanMessage(content=chat.prompt)]])

        # Extract the AI-generated message
        response = response.generations[0][0].text
        return ChatReply(reply=response)

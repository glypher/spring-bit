from langchain_openai import ChatOpenAI
from langchain.agents import tool
from langchain.tools.render import format_tool_to_openai_function
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.agents.output_parsers import OpenAIFunctionsAgentOutputParser
from langchain.schema.agent import AgentFinish
from langchain.schema.runnable import RunnablePassthrough
from langchain.agents.format_scratchpad import format_to_openai_functions
from langchain.memory import ConversationBufferMemory

from datetime import datetime

from crypto.dto.types import ChatRequest, ChatReply, CryptoInfoRequest
from crypto.statistics.CryptoSeries import CryptoSeries


@tool(args_schema=CryptoInfoRequest)
def crypto_correlation(symbols: list, start_date: datetime, end_date: datetime):
    """Returns a correlation matrix for the given list of symbols and between start_date and end_date"""
    return CryptoSeries.get().correlation(symbols, start_date, end_date)


class Chat:
    _instance_ = None

    @staticmethod
    def get():
        if Chat._instance_ is None:
            Chat._instance_ = Chat()
        return Chat._instance_

    def __init__(self):
        functions = [
            format_tool_to_openai_function(f) for f in [
                crypto_correlation
            ]
        ]
        self._chat_model  = ChatOpenAI(model_name="gpt-3.5-turbo", temperature=0).bind(functions=functions)

        prompt = ChatPromptTemplate.from_messages([
            ("system", "You are helpful but sassy assistant with crypto knowledge"),
            MessagesPlaceholder(variable_name="chat_history"),
            ("user", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad")
        ])

        self._chat_agent = RunnablePassthrough.assign(
            agent_scratchpad= lambda x: format_to_openai_functions(x["intermediate_steps"])
        ) | prompt | self._chat_model | OpenAIFunctionsAgentOutputParser()

    async def chat(self, chat: ChatRequest) -> ChatReply:
        intermediate_steps = []
        memory = []
        for msg in chat.history:
            if 'text' in msg:
                memory.append(msg['text'])

        while True:
            result = await self._chat_agent.ainvoke({
                "chat_history": memory,
                "input": chat.prompt,
                "intermediate_steps": intermediate_steps
            })
            if isinstance(result, AgentFinish):
                output = result.return_values['output']
                return ChatReply(reply=output)

            tool_to_run = {
                "crypto_correlation": crypto_correlation
            }[result.tool]
            observation = tool_to_run.run(result.tool_input)
            intermediate_steps.append((result, observation))

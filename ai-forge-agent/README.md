# AI Forge Agent

基于 FastAPI + LangChain 构建的企业级 AI Agent 服务后端。
本项目采用分层架构设计，旨在实现业务逻辑与 AI 核心能力的解耦，支持高并发的客服聊天、RAG 问答及数据处理功能。

## 📂 目录结构说明

```text
ai-forge-agent/
├── .venv/                # Python 虚拟环境 (使用 uv 管理)
├── agents/               # 【Agent 核心层】存放所有智能体逻辑
│   ├── __init__.py
│   ├── base_agent.py     # Agent 抽象基类
│   ├── customer.py       # 客服 Agent (处理聊天逻辑)
│   └── rag_agent.py      # 知识库 Agent (处理文章/数据集查询)
│
├── app/                  # 【应用接口层】FastAPI 路由与控制器
│   ├── __init__.py
│   ├── api.py            # API 路由定义 (提供给 Java 调用)
│   └── schemas.py        # Pydantic 数据模型 (请求/响应定义)
│
├── models/               # 【数据持久层】ORM 模型或数据结构
│   ├── __init__.py
│   └── data_models.py    # 数据库表结构或向量库定义
│
├── services/             # 【业务服务层】通用工具与外部服务封装
│   ├── __init__.py
│   ├── llm_provider.py   # LLM 封装 (OpenAI/Anthropic 调用)
│   ├── vector_store.py   # 向量数据库操作 (Chroma/Pinecone)
│   └── utils.py          # 通用工具函数
│
├── main.py               # 项目启动入口
├── pyproject.toml        # 项目依赖管理 (uv/pip)
└── test_main.http        # HTTP Client 测试脚本

项目开发规范
为了保证代码的可维护性和与 Java 后端的无缝对接，本项目遵循以下严格规范：
分层架构原则
app/ (接口层)：仅负责接收 HTTP 请求、参数校验和返回 JSON。禁止在此处编写复杂的业务逻辑。
services/ (业务层)：处理核心业务流程（如：用户积分扣除、文章保存逻辑）。
agents/ (智能层)：封装 LangChain 逻辑，专注于 Prompt 编排和 LLM 交互。
数据传输对象规范
所有 API 的入参必须定义在 app/schemas.py 中，类名以 Request 结尾（如 ChatRequest）。
所有 API 的出参必须定义在 app/schemas.py 中，类名以 Response 结尾（如 ChatResponse）。
禁止直接将数据库模型或 Agent 的原始输出返回给前端，必须转换为 Response 对象。
命名与类型规范
变量/函数：使用 snake_case (下划线命名法)。
类名：使用 PascalCase (大驼峰命名法)。
类型提示：所有函数必须包含 Python Type Hints (如 def foo(a: int) -> str:)。
异常处理
禁止在底层直接 print 错误。必须抛出自定义异常或标准的 HTTPException，由全局异常处理器统一捕获。


# Role
You are a Senior Python Architect specializing in FastAPI and LangChain.

# Global Constraints
1. Strictly follow the layered architecture: API routes in `app/`, business logic in `services/`, AI logic in `agents/`.
2. All code must include Python Type Hints.
3. Use `pydantic.BaseModel` for all data validation.

# Coding Standards
- **Naming**:
  - Variables and functions must use `snake_case`.
  - Class names must use `PascalCase`.
- **DTOs (Schemas)**:
  - All API inputs/outputs must be defined in `app/schemas.py`.
  - Input classes must end with `Request` (e.g., `ChatRequest`).
  - Output classes must end with `Response` (e.g., `ChatResponse`).
  - Never use raw `dict` or `str` as request bodies in routes; always use Schema classes.
- **Imports**:
  - Use absolute imports (e.g., `from app.schemas import ChatRequest`).

# Logic Implementation
- In `app/api.py`, handle only request/response logic and delegate to `services`.
- In `services/`, orchestrate calls to `agents`.
- Never hardcode configurations; use `pydantic.BaseSettings` or `os.getenv`.
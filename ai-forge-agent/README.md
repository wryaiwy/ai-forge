# AI Forge Agent

基于 FastAPI + LangChain 构建的企业级 AI Agent 服务后端。
本项目采用分层架构设计，旨在实现业务逻辑与 AI 核心能力的解耦，支持高并发的客服聊天、RAG 问答及数据处理功能。

## 📂 目录结构说明

ai-forge-agent/
├── .venv/                # Python 虚拟环境 (使用 uv 管理)
├── agents/               # 【Agent 核心层】存放所有智能体逻辑，专注 Prompt 编排与 LLM 交互
│   ├── __init__.py
│   ├── base_agent.py     # Agent 抽象基类 (定义通用的思考、执行规范)
│   ├── customer.py       # 客服 Agent (处理多轮聊天逻辑)
│   └── rag_agent.py      # 知识库 Agent (处理文章/数据集查询与总结)
│
├── app/                  # 【应用接口层】FastAPI 路由与全局应用配置
│   ├── __init__.py
│   ├── api.py            # API 路由定义 (提供给前端或 Java 后端调用)
│   ├── config.py         # 全局配置管理 (基于 pydantic-settings，读取环境变量)
│   ├── dependencies.py   # 依赖注入模块 (统一管理数据库会话获取、鉴权校验等)
│   ├── exceptions.py     # 全局异常类定义与统一异常捕获处理器
│   └── schemas.py        # Pydantic 数据模型 (严格定义 Request/Response)
│
├── models/               # 【数据持久层】ORM 模型或向量数据结构
│   ├── __init__.py
│   ├── database.py       # 数据库连接初始化 (SQLAlchemy Engine/Session)
│   └── data_models.py    # 数据库表结构定义或向量库 Schema 定义
│
├── services/             # 【业务服务层】核心业务逻辑、通用工具与外部服务封装
│   ├── __init__.py
│   ├── llm_provider.py   # LLM 模型调用封装 (OpenAI/Anthropic 等统一出口)
│   ├── vector_store.py   # 向量数据库操作封装 (Chroma/Pinecone/Milvus 等)
│   └── utils.py          # 通用工具函数 (如 token 计算、数据格式化等)
│
├── .gitignore            # Git 忽略配置
├── main.py               # FastAPI 项目启动入口 (包含路由注册、中间件挂载)
├── pyproject.toml        # 项目核心依赖与元数据管理 (uv/pip)
├── README.md             # 项目说明文档
├── test_main.http        # HTTP Client 本地测试脚本
└── uv.lock               # uv 依赖锁定文件 (确保团队环境绝对一致)


## 🛠️ 项目开发规范

为了保证代码的高可维护性、团队协作效率，以及与外部系统的无缝对接，本项目遵循以下严格的工程化开发规范：

### 1. 分层架构与职责边界
采用类似经典后端架构的严格分层设计，各层职责相互隔离，严禁越权调用：
* **`app/` (接口层 / Controller)**：仅负责处理 HTTP 请求、基于 Pydantic 的参数校验、依赖注入以及组装最终响应。**禁止**在此处编写任何核心业务逻辑或 AI 编排逻辑。
* **`services/` (业务层 / Service)**：承载核心业务链路（如：鉴权逻辑判断、数据持久化前置处理、业务规则校验）。负责向上提供业务接口，向下调度 `models` 和 `agents`。
* **`agents/` (智能层 / AI Domain)**：专注于封装 LangChain 逻辑、Prompt 组装、记忆（Memory）管理、工具链（Tools）调用以及与底层 LLM 的交互。
* **`models/` (数据层 / Entity & DAO)**：仅包含数据库表结构映射（ORM）和底层数据访问，不包含任何业务计算或 AI 逻辑。

### 2. 数据传输对象 (DTO) 与全局响应
实现数据进出的严格管控，彻底隔离外部接口与底层数据库模型：
* **入参/出参规约**：所有 API 的请求体与响应体必须使用 Pydantic 定义在 `app/schemas.py` 中。
    * 入参类名必须以 `Request` 结尾（如 `ChatRequest`）。
    * 出参类名必须以 `Response` 结尾（如 `ChatResponse`）。
    * **禁止**直接在路由中接收/返回原生的 `dict`、`str`，绝对禁止直接将 ORM 模型对象暴露给前端。
* **统一返回结构**：所有接口（无论成功或失败）必须遵循全局统一的 JSON 包装结构，以便调用方进行统一拦截与处理：
    ```json
    {
      "code": 200,
      "message": "success",
      "data": { ... } // 具体的 Response放在这里
    }
    ```

### 3. 命名规范与类型提示 (Type Hints)
* **变量与函数**：强制使用 `snake_case`（下划线命名法），例如 `user_id`、`get_chat_history()`。
* **类名**：强制使用 `PascalCase`（大驼峰命名法），例如 `DatabaseConfig`、`CustomerAgent`。
* **强制类型注解**：为了保持代码的工程级健壮性，所有函数、方法定义必须包含完整的 Python 类型提示（Type Hints），包括所有入参和返回值。
    * *正确示例*：`async def process_chat(request: ChatRequest) -> ChatResponse:`

### 4. 异步编程规范 (Async/Await)
* 为了充分发挥 FastAPI 和大模型并发请求的性能优势，所有 I/O 密集型操作（如数据库查询、外部网络请求、LLM 接口调用、缓存读写）**必须**使用 `async/await` 异步语法和对应的异步生态库（如 `asyncpg`, `httpx`）。
* 严禁在异步上下文中运行长时间阻塞的同步代码。

### 5. 异常处理与日志记录
* **全局异常处理**：**禁止**在业务或底层代码中通过 `return` 错误字典的方式处理错误。遇到业务阻断或异常，必须抛出自定义异常类（定义在 `app/exceptions.py` 中）或标准的 `HTTPException`，由 FastAPI 顶层的全局异常处理器拦截，并转化为上述的“统一返回结构”（例如 code 返回 500 或特定业务错误码）。
* **日志规约**：**严禁**在生产代码中使用 `print()` 打印错误或调试信息。必须统一使用 Python 标准的 `logging` 模块（或推荐引入 `loguru`），并严格区分 `DEBUG`、`INFO`、`WARNING`、`ERROR` 等日志级别。
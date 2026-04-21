from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase
from app.config import settings

# 1. 创建数据库引擎 (Engine)
# pool_pre_ping=True: 自动检测连接是否失效，防止 "MySQL server has gone away" 错误
engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,
    echo=False, # 开发时设为 True 可以看到 SQL 语句，生产环境建议 False
    pool_size=5, # 连接池大小
    max_overflow=10 # 最大溢出连接数
)

# 2. 创建会话工厂 (SessionLocal)
# 用于在具体的业务逻辑中获取数据库会话
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 3. 声明基类 (Base)
# 所有的 ORM 模型（表结构）都要继承这个类
class Base(DeclarativeBase):
    pass

# 4. 依赖注入：获取数据库会话的函数
# 这个函数将在 API 路由中被调用，确保请求结束后自动关闭连接
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
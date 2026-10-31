from pydantic import BaseModel, Field
from typing import Optional


class ArticlePolishRequest(BaseModel):
    content: str = Field(..., description="Markdown 文章内容")
    polish_mode: Optional[int] = Field(default=2, alias="polishMode", description="润色模式: 1-语法修正 2-专业润色 3-风格转换")
    target_style: Optional[str] = Field(default=None, alias="targetStyle", description="目标风格")

    model_config = {
        "populate_by_name": True
    }


class ArticleTranslateRequest(BaseModel):
    content: str = Field(..., description="Markdown 文章内容")
    target_lang: str = Field(..., alias="targetLang", description="目标语言代码: en/zh/ja/ko/fr/de/es")

    model_config = {
        "populate_by_name": True
    }


class ArticlePolishResponse(BaseModel):
    original_content: str = Field(..., alias="originalContent")
    polished_content: str = Field(..., alias="polishedContent")
    polish_mode_desc: str = Field(..., alias="polishModeDesc")

    model_config = {
        "populate_by_name": True
    }


class ArticleTranslateResponse(BaseModel):
    original_content: str = Field(..., alias="originalContent")
    translated_content: str = Field(..., alias="translatedContent")
    target_lang_desc: str = Field(..., alias="targetLangDesc")

    model_config = {
        "populate_by_name": True
    }


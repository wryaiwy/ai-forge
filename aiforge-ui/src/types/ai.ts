export enum PolishMode {
  GRAMMAR_FIX = 1,
  PROFESSIONAL_POLISH = 2,
  STYLE_TRANSFER = 3,
}

export interface AiPolishDTO {
  content: string
  polishMode: PolishMode
  targetStyle?: string
}

export interface AiPolishVO {
  originalContent: string
  polishedContent: string
  polishModeDesc: string
}

export const polishModeOptions = [
  { label: '语法修正', value: PolishMode.GRAMMAR_FIX, description: '修正语法、拼写和标点错误，保持原文风格' },
  { label: '专业润色', value: PolishMode.PROFESSIONAL_POLISH, description: '全面优化表达、结构和逻辑衔接' },
  { label: '风格转换', value: PolishMode.STYLE_TRANSFER, description: '将文章改写为指定风格' },
]

export interface AiTranslateDTO {
  content: string
  targetLang: string
}

export interface AiTranslateVO {
  originalContent: string
  translatedContent: string
  targetLangDesc: string
}

export const translateLangOptions = [
  { label: 'English', value: 'en' },
  { label: '中文', value: 'zh' },
  { label: '日本語', value: 'ja' },
  { label: '한국어', value: 'ko' },
  { label: 'Français', value: 'fr' },
  { label: 'Deutsch', value: 'de' },
  { label: 'Español', value: 'es' },
]

export interface AiChatContextAddDTO {
  sessionId: string
  content: string
}

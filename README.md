# 岳麓山景点推荐系统

## AI 模块最新说明

- 已移除火山引擎 SDK 依赖（`com.volcengine`）。
- 后端 AI 对话改为 Spring Boot 2.x 原生 `RestTemplate` 调用 DeepSeek 官方接口。
- AI 配置位于 `backend/src/main/resources/application.yml`：
  - `ai.mock-enabled: false`
  - `ai.base-url: https://api.deepseek.com/chat/completions`
  - `ai.api-key`: 保留现有密钥
  - `ai.model: deepseek-chat`
- AI 接口：`POST /ai/chat`
  - 请求：`{ "message": "..." }`
  - 返回：`Result<{ answer: string }>`

## 关键文件

- `backend/pom.xml`
- `backend/src/main/java/com/yuelu/config/RestTemplateConfig.java`
- `backend/src/main/java/com/yuelu/controller/AiController.java`
- `backend/src/main/resources/application.yml`

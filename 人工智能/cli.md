# Code 使用大全

## gen-cli

官方地址：[gen-cli](https://github.com/gen-cli/gen-cli)

安装方式：
```bash
npm install -g @gen-cli/gen-cli
```

### 创建 `.env` 文件

在项目根目录下创建 `.env` 文件，并添加以下内容：
```env
SILICONFLOW_API_KEY=<your_siliconflow_api_key>
```
将 `SILICONFLOW_API_KEY` 替换为你自己的 API 密钥。

### 基本使用

**进入方式**：
```bash
gen
```

运行以下命令查看帮助：
```bash
gen --help
```

## qwen-code

官方地址：[qwen-code](https://github.com/QwenLM/Qwen-Code)

安装方式：
```bash
npm install -g @qwen/qwen-code
```

### 基本使用

**进入方式**：
```bash
qwen-code
```

运行以下命令查看帮助：
```bash
qwen-code --help
```

## OpenAI Codex CLI

官方地址：[OpenAI Codex](https://openai.com/codex)

安装方式：
```bash
npm install -g openai-codex-cli
```

### 基本使用

**进入方式**：
```bash
codex
```

运行以下命令查看帮助：
```bash
codex --help
```

## Google Gemini CLI

官方地址：[Google Gemini](https://ai.google.dev/gemini-api)

安装方式：
```bash
npm install -g @google/gemini-cli
```

### 基本使用

**进入方式**：
```bash
gemini
```

运行以下命令查看帮助：
```bash
gemini --help
```

## Anthropic Claude Code

官方地址：[Anthropic Claude](https://www.anthropic.com/claude)

安装方式：
```bash
npm install -g @anthropic/claude-code-cli
```

### 基本使用

**进入方式**：
```bash
claude-code
```

运行以下命令查看帮助：
```bash
claude-code --help
```

## 工具对比

以下是各AI代码生成CLI工具的对比信息：

| 工具名称 | 进入方式 | 支持语言数量 | 擅长领域 | 安全性 | 上手难度 | 适用场景 | 特色功能 |
|---------|---------|-------------|---------|-------|---------|---------|---------|
| gen-cli | `gen` | 10+ | 通用代码生成 | 中 | 易 | 快速原型开发 | 基于SiliconFlow API，配置简单 |
| qwen-code | `qwen-code` | 15+ | 多语言代码生成 | 高 | 易 | 企业级开发 | 基于通义千问大模型，支持多种框架 |
| OpenAI Codex CLI | `codex` | 10+ | 后端开发、数据处理 | 高 | 中 | 有明确控制边界的开发 | 性能优化出色，SwiftUI设计能力强 |
| Google Gemini CLI | `gemini` | 20+ | 前端开发、数据可视化 | 中 | 易 | 原型设计、快速开发 | 支持语言广泛，前端框架支持出色 |
| Anthropic Claude Code | `claude-code` | 15+ | 复杂项目、多层架构 | 高 | 中 | 需要稳定性和细节关注的项目 | 精确性高，工具审批模式 |

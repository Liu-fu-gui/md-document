## 修改计划

### 1. 分析现有格式
现有`cli.md`文件采用以下格式：
- 二级标题（## 工具名）
- 官方地址：[工具名](链接)
- 安装方式：```bash 命令 ```
- 可选子标题（### 功能/使用等）
- 代码块（```bash 命令 ```）

### 2. 要添加的工具
根据搜索结果，添加以下主流AI代码生成CLI工具：
- OpenAI Codex CLI
- Google Gemini CLI
- Anthropic Claude Code

### 3. 具体修改内容
在现有文件末尾添加以下内容：

```markdown
## OpenAI Codex CLI

官方地址：[OpenAI Codex](https://openai.com/codex)

安装方式：
```bash
npm install -g openai-codex-cli
```

### 基本使用

运行以下命令开始使用：
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

运行以下命令开始使用：
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

运行以下命令开始使用：
```bash
claude-code --help
```
```

### 4. 修改目标
- 保持与现有格式一致
- 添加主流AI代码生成CLI工具
- 提供清晰的安装和使用说明

### 5. 预期效果
扩展现有文档，包含更多主流AI代码生成CLI工具，方便用户选择和使用。
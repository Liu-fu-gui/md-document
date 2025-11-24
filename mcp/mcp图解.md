![image-20250317180222988](https://liu-fu-gui.github.io/myimg/halo/202503171802056.png)

# 什么是MCP协议？

Anthropic 推出了 模型上下文协议（Model Context Protocol，MCP），旨在让 AI 模型更加强大 🚀。

MCP 是一个 开放标准（同时也是一个开源项目），它让 AI 模型（如 Claude）可以连接数据库、API、文件系统等各种工具，而 无需为每次新集成编写自定义代码 💡。

MCP 采用 客户端-服务器 模型，由 三个核心组件 组成： 
🔹 Host（宿主）：AI 应用（如 Claude）提供 AI 交互环境，使其能够访问不同的工具和数据源。宿主运行 MCP 客户端。
🔹 MCP 客户端：这是 AI 模型内部的一个组件（如 Claude 内部的 MCP 客户端），负责与 MCP 服务器通信。例如，如果 AI 需要从 PostgreSQL 获取数据，MCP 客户端会将请求格式化为结构化消息，并发送给 MCP 服务器 📡。
🔹 MCP 服务器：充当 AI 模型与外部系统（如 PostgreSQL、Google Drive 或 API）之间的 中间桥梁。例如，当 Claude 需要分析 PostgreSQL 中的销售数据时，PostgreSQL 的 MCP 服务器就会在 Claude 与数据库之间 充当连接器 🔗。

MCP 的五个核心构建块（Primitives） 
MCP 由 五个基础构件 组成，分别分布在 客户端 和 服务器端：
🔹 客户端（Client）：
Roots（根）：用于 安全访问文件 📂。 
Sampling（采样）：让 AI 协助完成任务，比如 生成数据库查询语句 🛠️。 

🔹 服务器（Server）：
Prompts（提示）：用于 引导 AI 执行特定任务 📝。 
Resources（资源）：AI 可参考的数据对象 📊。 
Tools（工具）：AI 可以调用的功能，例如 运行数据库查询 ⚙️。 

🤔 你是否已经尝试过 Anthropic 的 模型上下文协议（MCP）？欢迎分享你的探索和想法！🚀

From ByteByteGo
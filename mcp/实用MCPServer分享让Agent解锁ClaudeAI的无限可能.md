![cover_image](https://mmbiz.qpic.cn/sz_mmbiz_jpg/E1DQUYfcS0LvDkkmFakTiahVvJQdklyGbibIXJJAUqTbwmPoopicrEjCYjyEW5lxIEHe5VW5POvo5FPBGXCjPcf0g/0?wx_fmt=jpeg)

#  实用MCP Server分享，让Agent解锁 Claude AI 的无限可能

__ _ _ _ _

_ 点击👇🏻可关注，文章来自  _

🙋‍♂️ 想加入社群的朋友，可看文末方法，进群交流。

大家好，我是肆〇柒。今天要和大家分享几个热门MCP（Model Context
Protocol）服务器。MCP就像一把神奇的钥匙，为大型语言模型（LLM）打开了通往外部世界的大门，让 AI
的能力不再局限于文本生成，而是能够与各种应用程序无缝协作。

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/E1DQUYfcS0LvDkkmFakTiahVvJQdklyGbnDq2ia1swjR4RBGT7ic476uwyaPjicRibbqEydB6KPSibSjib9iazgMwt5m3A/640?wx_fmt=png&from=appmsg)  

#  MCP 是什么？

MCP，全称 Model Context Protocol，是一种标准化协议，用于将 AI （如 Claude）与各种外部工具和数据源连接起来。你可以把
MCP 想象成一个 USB-C 接口——就像 USB-C 简化了我们连接不同设备的方式一样，MCP 简化了 AI 模型与数据、工具和服务的交互方式。

###  MCP 的核心价值

MCP 设计的目的是为了解决传统 API 集成的复杂性。传统上，连接 AI 系统与外部工具需要集成多个 API，每个 API
都需要单独编写代码、处理文档、认证方法、错误处理和维护。而 MCP 提供了一种统一的连接方式，就像一个通用的“接口”，让 AI
模型能够动态地与各种工具和服务交互，而无需为每个工具单独编写代码。

###  MCP 的架构与工作原理

MCP 的架构基于简单的客户端-服务器模型：

  1. 1\.  ** MCP 主机  ** ：这是需要访问外部数据或工具的应用程序，例如 Claude Desktop 或 AI 驱动的 IDE。 
  2. 2\.  ** MCP 客户端  ** ：它们与 MCP 服务器保持专用的点对点连接，负责发送请求和接收响应。 
  3. 3\.  ** MCP 服务器  ** ：轻量级服务器，通过 MCP 协议暴露特定功能，连接到本地或远程数据源。 
  4. 4\.  ** 本地数据源  ** ：本地文件、数据库或服务，由 MCP 服务器安全访问。 
  5. 5\.  ** 远程服务  ** ：基于互联网的外部 API 或服务，由 MCP 服务器访问。 

![MCP
架构示意](https://mmbiz.qpic.cn/sz_mmbiz_png/E1DQUYfcS0LvDkkmFakTiahVvJQdklyGbEAVYdAZJia7w0aWnmzdjlakIr41crxOL8gu03vhs86RiaM3CzUaw9T2w/640?wx_fmt=png&from=appmsg)
MCP 架构示意

MCP 的工作原理类似于一个“智能桥梁”。它本身并不处理复杂的逻辑，而是协调 AI 模型和工具之间的数据和指令流动。通过 MCP，AI
模型可以动态地发现和交互可用的工具，而无需硬编码每个集成的具体细节。

###  MCP 的核心特性

  * •  ** 单一协议，多工具访问  ** ：集成一个 MCP 协议，就可以访问多个工具和服务，而传统 API 需要为每个工具单独集成。 
  * •  ** 动态发现  ** ：MCP 允许 AI 模型动态发现和交互可用工具，无需提前知道每个工具的具体接口。 
  * •  ** 实时双向通信  ** ：MCP 支持持久的实时双向通信，类似于 WebSockets。AI 模型既可以检索信息，也可以动态触发操作。 

###  MCP 与传统 API 的对比

![传统 API 要求开发者为每个服务或数据源编写自定义集成](https://mmbiz.qpic.cn/sz_mmbiz_png/E1DQUYfcS0LvDkkmFakTiahVvJQdklyGb7ZnViadKWnGsJaYvtPcSUaxCnkzkysOdIEbicbd4DVrKOlY6OLVxl7Zg/640?wx_fmt=png&from=appmsg) 传统 API 要求开发者为每个服务或数据源编写自定义集成  特性  |  MCP  |  传统 API   
---|---|---  
** 集成工作量  ** |  单一、标准化集成  |  每个 API 单独集成   
** 实时通信  ** |  ✅ 是  |  ❌ 否   
** 动态发现  ** |  ✅ 是  |  ❌ 否   
** 可扩展性  ** |  容易（即插即用）  |  需要额外集成   
** 安全性和控制  ** |  工具间一致  |  因 API 而异   

###  为什么选择 MCP？

  * •  ** 简化开发  ** ：只需编写一次代码，即可集成多个工具和服务，无需为每个工具单独编写代码。 
  * •  ** 灵活性  ** ：轻松切换 AI 模型或工具，无需复杂的重新配置。 
  * •  ** 实时响应  ** ：MCP 连接始终保持活跃，支持实时上下文更新和交互。 
  * •  ** 安全性和合规性  ** ：内置访问控制和标准化的安全实践。 
  * •  ** 可扩展性  ** ：随着 AI 生态系统的增长，可以轻松添加新功能，只需连接另一个 MCP 服务器。 

#  如何设置 MCP 服务器？

设置 MCP 服务器并不复杂，以下是详细的五步指南：

  1. 1\.  ** 安装 Composio MCP  ** ：使用命令  ` npx @composio/mcp@latest setup "<MCP_SERVER_URL>" --client claude  ` 。这一步会从 npm（Node Package Manager）下载并安装最新的 Composio MCP 包，并按照给定的 MCP 服务器 URL 进行配置，同时指定客户端为 Claude。 
  2. 2\.  ** 验证连接  ** ：当提示时，完成身份验证。这通常涉及输入你的 Claude 凭证或其他授权信息，以确保 MCP 服务器能够与 Claude 正常通信。 
  3. 3\.  ** 测试连接  ** ：运行命令  ` mcp test --client claude  ` 。该命令会向 MCP 服务器发送一个测试请求，验证服务器是否正常运行以及与 Claude 的连接是否稳定。 
  4. 4\.  ** 验证集成  ** ：通过运行简单任务来确认集成是否成功。例如，你可以让 Claude 通过 MCP 服务器获取一条 Reddit 上的热门讨论，或者从 Notion 中读取一页内容。 
  5. 5\.  ** 开始自动化任务  ** ：使用 Claude 和你选择的 MCP 服务器开始自动化任务。比如，设置一个定时任务，让 Claude 每天早上自动整理你的 Gmail 中的重要邮件，并生成一份报告。 

> ** 注意  ** ：在运行 MCP 命令之前，请确保你的电脑上已安装 Node.js，因为它是执行设置过程所必需的。

#  热门 MCP Server推荐

###  ** 浏览器自动化  **

  1. 1\.  ** Playwright+ Python MCP Server  **
     * •  ** 功能  ** ：使用Playwright进行浏览器自动化，更适合LLM。 
     * •  ** 地址  ** ：https://github.com/blackwhite084/playwright-plus-python-mcp 
  2. 2\.  ** Playwright MCP Server  **
     * •  ** 功能  ** ：使用Playwright进行浏览器自动化和网页抓取。 
     * •  ** 地址  ** ：https://github.com/executeautomation/playwright-mcp-server 
  3. 3\.  ** Puppeteer MCP Server  **
     * •  ** 功能  ** ：用于网页抓取和交互的浏览器自动化。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-puppeteer 
  4. 4\.  ** YouTube Transcript MCP Server  **
     * •  ** 功能  ** ：获取YouTube字幕和文字记录以供AI分析。 
     * •  ** 地址  ** ：https://github.com/kimtaeyoon83/mcp-server-youtube-transcript 
  5. 5\.  ** Web Search MCP Server  **
     * •  ** 功能  ** ：支持使用Google搜索结果进行免费网页搜索，无需API密钥。 
     * •  ** 地址  ** ：https://github.com/pskill9/web-search 

###  ** 即时通讯（IM）  **

  1. 1\.  ** Slack MCP Server  **
     * •  ** 功能  ** ：用于频道管理和消息传递的Slack工作区集成。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-slack 
  2. 2\.  ** Bluesky MCP Server  **
     * •  ** 功能  ** ：Bluesky实例集成，用于查询和交互。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-bluesky 
  3. 3\.  ** iMessage Query MCP Server  **
     * •  ** 功能  ** ：提供对iMessage数据库的安全访问，使LLM能够查询和分析对话。 
     * •  ** 地址  ** ：https://github.com/hannesrudolph/imessage-query-fastmcp-mcp-server 
  4. 4\.  ** Nostr MCP Server  **
     * •  ** 功能  ** ：支持与Nostr交互，可发布笔记等功能。 
     * •  ** 地址  ** ：https://github.com/AbdelStark/nostr-mcp 
  5. 5\.  ** Discord MCP Server  **
     * •  ** 功能  ** ：突出重要讨论和固定消息。在社区讨论中，Claude 可以识别并总结重要的讨论点，方便管理员和成员快速了解关键信息。辅助管理各类社区事务。 
     * •  ** 地址  ** ：https://mcp.composio.dev/discord/puny-ancient-nurse-qfnF-0 

###  ** 文件系统  **

  1. 1\.  ** Filesystem MCP Server  **
     * •  ** 功能  ** ：直接访问本地文件系统。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-filesystem 
  2. 2\.  ** Google Drive MCP Server  **
     * •  ** 功能  ** ：Google Drive集成，用于列出、阅读和搜索文件。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-google-drive 
  3. 3\.  ** Box MCP Server  **
     * •  ** 功能  ** ：Box集成，支持文件列表、阅读和搜索功能。 
     * •  ** 地址  ** ：https://github.com/hmk/b  ox-mcp-server 

###  ** 数据库  **

  1. 1\.  ** Elasticsearch MCP Server  **
     * •  ** 功能  ** ：集成Elasticsearch的MCP服务器实现。 
     * •  ** 地址  ** ：https://github.com/cr7258/elasticsearch-mcp-server 
  2. 2\.  ** PostgreSQL MCP Server  **
     * •  ** 功能  ** ：PostgreSQL数据库集成，支持模式检查和查询功能。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-postgres 
  3. 3\.  ** SQLite MCP Server  **
     * •  ** 功能  ** ：SQLite数据库操作，具有内置分析功能。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-sqlite 
  4. 4\.  ** MySQL MCP Server  **
     * •  ** 功能  ** ：MySQL数据库集成，支持模式检查和查询功能。 
     * •  ** 地址  ** ：https://github.com/designcomputer/mysql_mcp_server 

###  ** 开发工具  **

  1. 1\.  ** GitHub MCP Server  **
     * •  ** 功能  ** ：集成GitHub API，支持代码管理、问题跟踪、文件更新等功能。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/servers 
  2. 2\.  ** GitLab MCP Server  **
     * •  ** 功能  ** ：提供GitLab项目管理功能。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/servers/tree/main/src/gitlab 
  3. 3\.  ** Docker MCP Server  **
     * •  ** 功能  ** ：通过MCP进行Docker容器管理和操作。 
     * •  ** 地址  ** ：https://github.com/QuantGeekDev/docker-mcp 
  4. 4\.  ** Kubernetes MCP Server  **
     * •  ** 功能  ** ：连接Kubernetes集群，管理Pod、部署和服务。 
     * •  ** 地址  ** ：https://github.com/MayukhSobo/k8s-mcp-server 
  5. 5\.  ** Fetch MCP Server  **
     * •  ** 功能  ** ：灵活获取JSON、文本和HTML数据。 
     * •  ** 地址  ** ：https://github.com/zcaceres/fetch-mcp 
  6. 6\.  ** OpenAPI MCP Server  **
     * •  ** 功能  ** ：使用开放API规范(v3)连接任何HTTP/REST API服务器。 
     * •  ** 地址  ** ：https://github.com/snaggle  -ai/openapi-mcp-server 

###  ** 位置服务  **

  1. 1\.  ** Google Maps MCP Server  **
     * •  ** 功能  ** ：Google地图集成，提供位置服务、路线规划和地点详细信息。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-google-maps 
  2. 2\.  ** IPInfo MCP Server  **
     * •  ** 功能  ** ：使用IPInfo API获取IP地址的地理位置和网络信息。 
     * •  ** 地址  ** ：https://github.com/mo  delcontextprotocol/server-ipinfo 

###  ** 搜索  **

  1. 1\.  ** Brave Search MCP Server  **
     * •  ** 功能  ** ：使用Brave的搜索API实现网页搜索功能。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-brave-search 
  2. 2\.  ** NYT Search MCP Server  **
     * •  ** 功能  ** ：使用NYTimes API搜索文章。 
     * •  ** 地址  ** ：https://github.com/anghe  ljf/nyt 

###  ** 其他工具  **

  1. 1  .  ** Memory MCP Server  **
     * •  ** 功能  ** ：基于知识图谱的长期记忆系统用于维护上下文。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-memory 
  2. 2\.  ** Sequential Thinking Server  **
     * •  ** 功能  ** ：通过结构化思维过程提供动态和反思性问题解决。 
     * •  ** 地址  ** ：https://github.com/modelcontextprotocol/server-sequential-thinking 
  3. 3\.  ** Notion MCP Server  **
     * •  ** 功能  ** ：创建、更新和组织 Notion 页面。自动化提醒、任务分配和文档更新。总结会议笔记和行动项，方便团队共享。 
     * •  ** 地址  ** ：https://mcp.composio.dev/notion/puny-ancient-nurse-qfnF-0 
  4. 4\.  ** Google Sheets MCP Server  **
     * •  ** 功能  ** ：自动化数据输入、计算和分析。AI 生成报告和实时数据更新。AI 驱动的数据处理，提高准确性。 
     * •  ** 地址  ** ：https://mcp.composio.dev/googlesheets/puny-ancient-nurse-qfnF-0 
  5. 5\.  ** Gmail MCP Server  **
     * •  ** 功能  ** ：分类邮件、起草回复和优先处理消息。提取重要日期并同步到日历。AI 驱动的邮件回复和跟进。 
     * •  ** 地址  ** ：https://mcp.composio.dev/gmail/puny-  ancient-nurse-qfnF-0 

以上就是是我整理的比较热门且常见的MCP Server。

(https://mp.weixin.qq.com/s?__biz=Mzk2NDA0MzcxNw==&mid=2247485093&idx=2&sn=2dfdcec6920e972ed37ebc2d99cd3fb0&scene=21#wechat_redirect)

  

参考资料

  * •  What is Model Context Protocol (MCP)? How it simplifies AI integrations compared to APIs    
https://norahsakal.com/blog/mcp-vs-api-model-context-protocol-explained/

  * • 精选 MCP Server    
https://github.com/punkpeye/awesome-mcp-servers/blob/main/README-zh.md

  * •  smithery ai    
https://smithery.ai/

  * • MCP Servers Hunt    
https://mcphunt.com/zh

  

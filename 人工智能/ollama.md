下载地址

https://ollama.com/download

```
##win 安装
https://ollama.com/download/OllamaSetup.exe
## cpu安装
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
## gpu安装 同时还需要先安装 Nvida container toolkit
https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html#installation

docker run -d --gpus=all -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
```

参考

https://developer.aliyun.com/article/1599835

| 命令                                    | 作用                   |
| --------------------------------------- | ---------------------- |
| ollama list                             | 展示本地大模型列表     |
| ollama rm 本地模型名称                  | 删除单个本地大模型     |
| ollama run 本地模型名                   | 启动本地模型           |
| ollama ps                               | 查看本地运行中模型列表 |
| ollama pull 本地/远程仓库模型名称       | 下载或者更新本地大模型 |
| ollama cp 本地存在的模型名 新复制模型名 | 复制本地大模型         |

```
ollama create deepseek-r1:14b -f models
ollama create deepseek-r1:32b -f /root/.ollama/models/blobs/models


# 千问
ollama pull qwen2:72b
```

## AI提效-本地代码补全助手+AI助手



![img](https://liu-fu-gui.github.io/myimg/halo/202501021014578.png)

```
推荐的模型组合：Codeqwen 7b+Qwen2 7B模型

这两个模型中，codeqwen 7b是一个专门用于代码补全的模型，qwen2 7b又是个通用的聊天模型，并且两者都不是重量级模型，在本地运行也不会那么费劲。这两个模型结合起来就能很好地实现代码补全+AI助手的聊天功能。

下载 Continue
```

![img](https://liu-fu-gui.github.io/myimg/halo/202501021015208.png)

```
{
    "models": [
        {
            "title": "Codeqwen 7B",
            "provider": "ollama",
            "model": "codeqwen",
            "apiBase": "http://127.0.0.1:11434"
        }
    ],
    "tabAutocompleteModel": {
        "title": "Qwen2 7B",
        "provider": "ollama",
        "model": "qwen2:7b",
        "apiBase": "http://127.0.0.1:11434"
    }
}
```

![img](https://liu-fu-gui.github.io/myimg/halo/202501021015721.png)

### 再加上RAG向量检索优化聊天

```
首先，continue插件内置了 @codebase 上下文provider，能自动从代码库检索到最相关的代码片段。假如我们用自己的本地的聊天模型，那么借助 Ollama与LanceDB向量化技术，可以去更高效的进行代码检索和聊天体验。
ollama pull nomic-embed-text
ollama run nomic-embed-text
```

### 继续配置 config.json

![img](https://liu-fu-gui.github.io/myimg/halo/202501021016174.png)

### 代码补全效果及对话功能验证

![img](https://liu-fu-gui.github.io/myimg/halo/202501021016861.png)

![img](https://liu-fu-gui.github.io/myimg/halo/202501021017024.png)



## open-webui

```
version: '3.8'

services:
  open-webui:
    image: ghcr.io/open-webui/open-webui:main
    container_name: open-webui
    ports:
      - "3000:8080"  # 将宿主机的 3000 端口映射到容器的 8080 端口
    volumes:
      - open-webui:/app/backend/data  # 持久化存储
    restart: always  # 容器停止后自动重启
    extra_hosts:
      - "host.docker.internal:host-gateway"  # 容器能够访问宿主机
    environment:
      - SOME_ENV_VAR=value  # 可选，添加任何必要的环境变量

volumes:
  open-webui:  # 定义持久化卷
```

```
docker run -d -p 3000:8080 --add-host=host.docker.internal:host-gateway -v open-webui:/app/backend/data --name open-webui --restart always ghcr.io/open-webui/open-webui:main
```

```
## 模型
C:\Users\Administrator\.ollama
## logs
C:\Users\Administrator\AppData\Local\Ollama
## 主程序
C:\Users\Administrator\AppData\Local\Programs
```



```
# 测试gpu是否启动
xiaoliu@localhost:/mnt/e/k8s/eureka-2.0.3/face$ curl -X POST http://localhost:11434/v1/completions \
-H "Content-Type: application/json" \
-d '{
  "model": "cloudmidleman/gpt5-mini:latest",
  "prompt": "测试 GPU 推理能力",
  "max_tokens": 50
}'

{"id":"cmpl-785","object":"text_completion","created":1735522470,"model":"cloudmidleman/gpt5-mini:latest","system_fingerprint":"fp_ollama","choices":[{"text":"用Cuda版本的pytorch运行推理，可达到350megabyte，这个性能满足初级 学习和日常工作需要。具体结果参考在我们","index":0,"finish_reason":"length"}],"usage":{"prompt_tokens":311,"completion_tokens":50,"total_tokens":361}}
```



ollama 使用显卡

Ollama 支持使用 GPU 来加速模型推理。以下是如何设置使用显卡的参数：

```
# 查看显卡信息
nvidia-smi
# 查看显卡的uuid
nvidia-smi -L
```

```
# 设置环境变量指定显卡处理
export CUDA_VISIBLE_DEVICES=0,1
```








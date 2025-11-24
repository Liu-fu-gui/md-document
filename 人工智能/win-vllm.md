

# vllm

https://hf-mirror.com/

[`huggingface-cli` ](https://hf-mirror.com/docs/huggingface_hub/guides/download#download-from-the-cli)是 Hugging Face 官方提供的命令行工具，自带完善的下载功能。

```
# 安装依赖
pip install -U huggingface_hub
# Linux
export HF_ENDPOINT=https://hf-mirror.com
# Windows Powershell
$env:HF_ENDPOINT = "https://hf-mirror.com"

#模型
huggingface-cli download --resume-download Qwen/Qwen2.5-7B-Instruct-AWQ  --local-dir Qwen2.5-7B-Instruct-AWQ
# 训练集
huggingface-cli download --repo-type dataset --resume-download wikitext --local-dir wikitext
可以添加 --local-dir-use-symlinks False 参数禁用文件软链接，这样下载路径下所见即所得，详细解释请见上面提到的教程
```

```
# 目录结构
PS D:\vscode\docker-compose\vllm> ls


    目录: D:\vscode\docker-compose\vllm


Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----          2025/3/7     16:46                models
-a----          2025/3/7     16:48            489 docker-compose.yaml


PS D:\vscode\docker-compose\vllm>
```



```
# docker-compose.yaml
services:
  vllm-openai:
    image: vllm/vllm-openai
    container_name: vllm-openai
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]
    ports:
      - "8000:8000"
    volumes:
      - ./models:/models  
    command: >
      --model /models/Qwen2.5-7B-Instruct-AWQ
      --host 0.0.0.0
      --port 8000
      --quantization awq
      --gpu-memory-utilization 0.95
      --max-model-len 3000
```


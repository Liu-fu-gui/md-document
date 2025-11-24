官方参考

https://github.com/hiyouga/LLaMA-Factory

```
git clone --depth 1 https://github.com/hiyouga/LLaMA-Factory.git
cd LLaMA-Factory
pip install -e ".[torch,metrics]"
```

cuda环境未识别

```
CUDA_VISIBLE_DEVICES=0 GRADIO_SHARE=1 GRADIO_SERVER_PORT=7860 llamafactory-cli webui


#!/bin/bash

# 设置CUDA设备，选择使用第0个GPU
export CUDA_VISIBLE_DEVICES=0

# 设置Gradio分享链接为1，允许公开分享
export GRADIO_SHARE=1

# 设置Gradio服务器监听端口为7860
export GRADIO_SERVER_PORT=7860

# 启动llamafactory-cli webui
llamafactory-cli webui

```


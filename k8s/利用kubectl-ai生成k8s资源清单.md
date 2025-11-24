kubectl-ai 项目是一个`kubectl`使用 AI 生成和应用 Kubernetes 清单的插件。

> github地址：https://github.com/sozercan/kubectl-ai

![img](https://mmbiz.qpic.cn/mmbiz_png/aSJ8tDK6zEvIVn3A8LhhNHKf7TeAYn1my0cSE7EMzficGdenlUzlMzO9gTiclEBBfw3aL7XSlZ9sKQicUyMSL3P5w/640?wx_fmt=png&from=appmsg)



### 安装kubectl-ai



```
#下载wget https://github.com/sozercan/kubectl-ai/releases/download/v0.0.13/kubectl-ai_linux_amd64.tar.gz
tar -zxvf kubectl-ai_linux_amd64.tar.gz -C /usr/local/bin/
```



### 配置kubectl-ai



```
docker run -d --rm -p 8080:8080 ghcr.io/sozercan/llama3.1:8bexport OPENAI_ENDPOINT="http://localhost:8080/v1"export OPENAI_DEPLOYMENT_NAME="llama-3.1-8b-instruct"export OPENAI_API_KEY="n/a"
```



> https://sozercan.github.io/aikit/docs/quick-start



镜像拉取不下来，利用github action拉取到个人镜像仓库，再从个人镜像仓库拉取



```
docker run -d --rm -p 8080:8080 registry.cn-hangzhou.aliyuncs.com/tulingfox/llama3.1:8b
```



### 使用kubectl-ai



```
kubectl-ai "create an nginx deployment with 3 replicas ，and create an servie"kubectl-ai "create an nginx deployment with 3 replicas ，and create an servie" --raw 
kubectl-ai "创建一个包含3个副本的nginx deployment控制器包括service，使用NodePort暴露30645端口"  
kubectl-ai "创建一个包含3个副本的nginx deployment控制器包括service，使用NodePort暴露30645端口" --raw > my-nginx-demo.yaml
```



![img](https://mmbiz.qpic.cn/mmbiz_png/aSJ8tDK6zEvIVn3A8LhhNHKf7TeAYn1my0cSE7EMzficGdenlUzlMzO9gTiclEBBfw3aL7XSlZ9sKQicUyMSL3P5w/640?wx_fmt=png&from=appmsg)



**常见错误**

apply之后，发现nginx镜像拉取不下来

![img](https://mmbiz.qpic.cn/mmbiz_png/aSJ8tDK6zEvIVn3A8LhhNHKf7TeAYn1m566bU2hR04ibibf6zXbwoTZRaVsFvMn6ZNaCeryN6dgz5ichsXoQy1Mpw/640?wx_fmt=png&from=appmsg)





原因是镜像拉取超时了

![img](https://mmbiz.qpic.cn/mmbiz_png/aSJ8tDK6zEvIVn3A8LhhNHKf7TeAYn1mhIJ4lTicyDKkyH24BPYvYoLIVamgLYQJT26tMGVdGgPCMRnqiaoC5pKg/640?wx_fmt=png&from=appmsg)





解决方案：配置可用的containerd镜像加速

```

# 创建/etc/containerd/certs.d下的hosts文件
mkdir -p /etc/containerd/certs.d/docker.io
tee /etc/containerd/certs.d/docker.io/hosts.toml <<-'EOF'
server = "https://docker.io"
[host."https://docker.1panel.live"]
  capabilities = ["pull", "resolve"]
EOF

systemctl daemon-reload
systemctl restart containerd

```

![img](https://mmbiz.qpic.cn/mmbiz_png/aSJ8tDK6zEvIVn3A8LhhNHKf7TeAYn1mXdC8Id5WP9wSgQxl0W7fVYJWIOEiaRx01p88icXejr49zRyZTFE6FzkA/640?wx_fmt=png&from=appmsg)
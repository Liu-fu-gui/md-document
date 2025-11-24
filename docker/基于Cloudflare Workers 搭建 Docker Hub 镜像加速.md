
<!-- more -->

:::info{title="简介"}

基于Cloudflare Workers 搭建 Docker Hub镜像加速服务。
:::

前置条件


:::warning{title="没有这些就不要往下看了"}
首先要注册一个Cloudflare账号。

Cloudflare账号下域名的一级域名，推荐万网注册个top域名，再转移到Cloudflare，很便宜的。（没有也无所吊慰，有免费域名，只是慢点而已）

注意 Worker 每天每免费账号有次数限制，为10万次。每分钟为1000次。
:::

## 步骤
登录到CF的仪表盘 https://dash.cloudflare.com/

点击 workers-and-pages > 创建应用程序 > 创建 Worker > 点击保存 >点击完成 > 编辑代码


#### 没有注册的可以先注册，登陆后安装图文指引进行
![20241129175718](https://liu-fu-gui.github.io/myimg/halo/20241129175718.png)



#### 为项目命名
![20241129175724](https://liu-fu-gui.github.io/myimg/halo/20241129175724.png)

#### 编写代码导入其中
![20241129175731](https://liu-fu-gui.github.io/myimg/halo/20241129175731.png)

```
import HTML from './docker.html';
export default {
    async fetch(request) {
        const url = new URL(request.url);
        const path = url.pathname;
        const originalHost = request.headers.get("host");
        const registryHost = "registry-1.docker.io";
        if (path.startsWith("/v2/")) {
        const headers = new Headers(request.headers);
        headers.set("host", registryHost);
        const registryUrl = `https://${registryHost}${path}`;
        const registryRequest = new Request(registryUrl, {
            method: request.method,
            headers: headers,
            body: request.body,
            // redirect: "manual",
            redirect: "follow",
        });
        const registryResponse = await fetch(registryRequest);
        console.log(registryResponse.status);
        const responseHeaders = new Headers(registryResponse.headers);
        responseHeaders.set("access-control-allow-origin", originalHost);
        responseHeaders.set("access-control-allow-headers", "Authorization");
        return new Response(registryResponse.body, {
            status: registryResponse.status,
            statusText: registryResponse.statusText,
            headers: responseHeaders,
        });
        } else {
        return new Response(HTML.replace(/{{host}}/g, originalHost), {
            status: 200,
            headers: {
            "content-type": "text/html"
            }
        });
        }
    }
}
```
点击新建文件 **docker.html**，创建此文件。ctrl + s 即可保存。
![20241129175743](https://liu-fu-gui.github.io/myimg/halo/20241129175743.png)
```
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>镜像使用说明</title>
    <style>
        body {
            font-family: 'Roboto', sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f4f4;
        }
        .header {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #fff;
            padding: 20px 0;
            text-align: center;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        .container {
            max-width: 800px;
            margin: 40px auto;
            padding: 20px;
            background-color: #fff;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            border-radius: 10px;
        }
        .content {
            margin-bottom: 20px;
        }
        .footer {
            text-align: center;
            padding: 20px 0;
            background-color: #333;
            color: #fff;
        }
        pre {
            background-color: #272822;
            color: #f8f8f2;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
        }
        code {
            font-family: 'Source Code Pro', monospace;
        }
        a {
            color: #4CAF50;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
        @media (max-width: 600px) {
            .container {
                margin: 20px;
                padding: 15px;
            }
            .header {
                padding: 15px 0;
            }
        }
    </style>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&family=Source+Code+Pro:wght@400;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="header">
        <h1>镜像使用说明</h1>
    </div>
    <div class="container">
        <div class="content">
            <p>为了加速镜像拉取，你可以使用以下命令设置 registry mirror:</p>
            <pre><code>sudo tee /etc/docker/daemon.json &lt;&lt;EOF
{
    "registry-mirrors": ["https://{{host}}"]
}
EOF</code></pre>
            <p>为了避免 Worker 用量耗尽，你可以手动 pull 镜像然后 re-tag 之后 push 至本地镜像仓库:</p>
            <pre><code>docker pull {{host}}/library/alpine:latest # 拉取 library 镜像
docker pull {{host}}/coredns/coredns:latest # 拉取 coredns 镜像</code></pre>
        </div>
    </div>
    <div class="footer">
        <p>Powered by Cloudflare Workers</p>
        <p><a href="https://www.xiaoliu.xn--6qq986b3xl/" target="_blank">访问博客 [songxwn.com](https://www.xiaoliu.xn--6qq986b3xl/)</a></p>
    </div>
</body>
</html>

```
#### 保存部署并配置触发器
上述两个文件的代码保存后，选择部署 > 保存并部署

点击左上角的项目连接，配置触发器。（自定义域名访问）


![20241129175806](https://liu-fu-gui.github.io/myimg/halo/20241129175806.png)


## 效果图
https://rapid-pond-fa49.2467802439.workers.dev/

![20241129175815](https://liu-fu-gui.github.io/myimg/halo/20241129175815.png)
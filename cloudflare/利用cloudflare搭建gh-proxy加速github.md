## **一、前提准备**

需要提前准备好cloudflare账号以及一个域名（可选），需要白嫖域名可以参考往期文章《[免费申请注册eu.org](https://post.smzdm.com/p/a20d905p/)二级域名》

## **二、部署gh-proxy**

登录cloudflare（https://dash.cloudflare.com/），转到Workers和Pages，概述，点击创建应用程序



worker.js

```
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request));
});

async function handleRequest(request) {
  const url = new URL(request.url);
  const targetUrl = url.pathname.slice(1); // 获取目标 URL（去掉开头的斜杠）

  // 检查目标 URL 是否以 https:// 开头
  if (!targetUrl.startsWith('https://')) {
    return new Response('请输入有效的 GitHub 文件 URL', { status: 400 });
  }

  try {
    // 转发请求到 GitHub
    const response = await fetch(targetUrl, {
      headers: {
        'User-Agent': 'Cloudflare-Worker' // GitHub API 要求 User-Agent
      }
    });

    // 如果 GitHub 返回错误，直接返回错误响应
    if (!response.ok) {
      return new Response('文件未找到或请求失败', { status: response.status });
    }

    // 设置缓存（1 小时）
    const cacheControl = 'public, max-age=3600';
    const headers = new Headers(response.headers);
    headers.set('Cache-Control', cacheControl);

    // 返回文件内容
    return new Response(response.body, {
      status: response.status,
      headers: headers
    });
  } catch (error) {
    // 捕获异常并返回错误信息
    return new Response('请求失败，请稍后重试', { status: 500 });
  }
}
```

![image-20250118134511499](https://liu-fu-gui.github.io/myimg/halo/20250118134511567.png)
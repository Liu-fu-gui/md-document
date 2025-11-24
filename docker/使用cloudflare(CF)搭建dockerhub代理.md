
<!-- more -->

:::tip{title="提示"}
目前国内docker所有域名都被屏蔽，造成一些玩docker的用户很是苦恼，更换阿里云的镜像加速但镜像也没dockerhub那么多，有些好用的工具一直拉不下来，自己搭建dockerhub镜像站又耗时还得购买海外服务器，非常不划算。本文按照B站一个大佬的方法为此我撰写一篇文章用最简单和最清晰的思路。
:::

## 准备环境
1. 注册cloudflare账户（必须）https://dash.cloudflare.com/
2. 注册github账户（必须）http://github.com/
3. 购买域名并绑定在cloudflare域下（随你，网站有免费的）

### 一、克隆github项目到自己的库
1. 访问此网站 https://github.com/cmliu/CF-Workers-docker.io
2. 克隆到自己仓库


![20241129175517](https://liu-fu-gui.github.io/myimg/halo/20241129175517.png)
****
### 二、部署到cloudflare

![20241129175533](https://liu-fu-gui.github.io/myimg/halo/20241129175533.png)
****

****

![20241129175547](https://liu-fu-gui.github.io/myimg/halo/20241129175547.png)
****

![20241129175555](https://liu-fu-gui.github.io/myimg/halo/20241129175555.png)

****
![20241129175602](https://liu-fu-gui.github.io/myimg/halo/20241129175602.png)
**选择仓库后一直下一步过程无需选择其它，直接点到此页面**

![20241129175610](https://liu-fu-gui.github.io/myimg/halo/20241129175610.png)

可以在拉取镜像名称前加入此域名，例如 https://<域名>/镜像名:lates


### 结果图
https://cf-workers-docker-io-5lk.pages.dev/


![20241129175618](https://liu-fu-gui.github.io/myimg/halo/20241129175618.png)
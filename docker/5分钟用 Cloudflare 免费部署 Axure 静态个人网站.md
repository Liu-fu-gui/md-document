# 5分钟用 Cloudflare 免费部署 Axure 静态个人网站
![20241212105913](https://liu-fu-gui.github.io/myimg/halo/20241212105913.png)
CloudFlare 是一个可以免费部署静态网站的云平台，Axure 静态网站除了用云服务的对象存储部署外，还可以使用 Cloudflare 进行部署，优点非常多。

免费套餐充足，足够个人网站或者工作室使用。
部署简单，直接上传网站源文件就行。
自动部署SSL证书，无需手动部署或者续期证书。

首先说下 Axure 建站是什么，Axure 是一款原型设计工具，它设计完成后可以直接导出网站源码，部署到云服务平台做自己的网站，以下是我最近设计的个人网站模版：幻影，暗黑系配置，非常好看，预览地址：https://demo.pmdaohang.com/themes/008-phantom/
![20241212105943](https://liu-fu-gui.github.io/myimg/halo/20241212105943.png)

今天给大家用 Axure 做的小报童导航网站为案例，讲解 Cloudflare 部署教程，以下是小报童导航网站实际上线效果：https://xiaobot.pmdaohang.com

![20241212110256](https://liu-fu-gui.github.io/myimg/halo/20241212110256.png)

下面开始讲解 CloudFlare 部署教程：
## 一、CloudFlare 部署

1.访问 CloudFlare(https://dash.cloudflare.com/sign-up)，注册并登录，然后来到控制台。
![20241212110316](https://liu-fu-gui.github.io/myimg/halo/20241212110316.png)
2.在左侧菜单找到 【Workers 和 Pages】 菜单点击进入，来到【概述】页面，选择【Pages】标签页，然后在【使用直接上传创建】下方的框中点击【上传资产】按钮，进入上传页面。
![202412121114735](https://liu-fu-gui.github.io/myimg/halo/202412121114735.png)
3.在上传页面中，输入【项目名称】，项目名称使用英文，会直接作为二级域名名称，如果输入的名称已经有人使用了，CloudFlare 会在你的名称基础上生成新的二级域名，如果没有人使用，就会使用你输入的名称作为二级域名，输入项目名称后，点击【创建项目】按钮，进入上传页面。
![20241212112856](https://liu-fu-gui.github.io/myimg/halo/20241212112856.png)

4.在上传页面中，点击【从计算机中选择】按钮，选择你的小报童导航网站源码压缩包或者将压缩包拖入上传框中，然后等待上传完成。
![20241212112917](https://liu-fu-gui.github.io/myimg/halo/20241212112917.png)
5.上传完成后，点击【部署站点】按钮，部署网站。
![20241212112933](https://liu-fu-gui.github.io/myimg/halo/20241212112933.png)
6.现在你可以点击页面中的链接，访问你的网站了。
![20241212112950](https://liu-fu-gui.github.io/myimg/halo/20241212112950.png)

## 二、CloudFlare 绑定域名

1.在【Workers 和 Pages】-【概述页面】中，点击项目名称，进入项目。

![20241212113106](https://liu-fu-gui.github.io/myimg/halo/20241212113106.png)
2.在项目中，选择【自定义域】标签页，点击【设置自定义域】按钮。
![20241212131254](https://liu-fu-gui.github.io/myimg/halo/20241212131254.png)
3.在【添加自定义域】输入自己购买的域名，点击【继续】按钮，进入 DNS 设置。
![20241212131312](https://liu-fu-gui.github.io/myimg/halo/20241212131312.png)
4.在 DNS 设置中，选择【我的 DNS 提供商】，点击【开始 CNAME 设置】，进入【配置 DNS】。
![20241212131329](https://liu-fu-gui.github.io/myimg/halo/20241212131329.png)
5.在你的域名服务商中，添加下图中的 CNAME 记录，稍等一会儿，点击【检查 DNS 记录】查看是否生效，解析生效后，你的网站就可以通过自己的域名进行访问了。
![20241212131351](https://liu-fu-gui.github.io/myimg/halo/20241212131351.png)
## 三、CloudFlare 更新部署

1.在【Workers 和 Pages】-【概述页面】中，点击项目名称，进入项目。
![20241212131420](https://liu-fu-gui.github.io/myimg/halo/20241212131420.png)
2.在【部署】标签页中，点击【创建新部署】按钮，进入部署页面。
![20241212131454](https://liu-fu-gui.github.io/myimg/halo/20241212131454.png)
3.在上传页面中，点击【从计算机中选择】按钮，选择你的网站源码压缩包或者将压缩包拖入上传框中，然后等待上传完成。
![20241212131512](https://liu-fu-gui.github.io/myimg/halo/20241212131512.png)
4.上传完成后，点击【保存并部署】按钮，部署网站，现在你的网站已经更新好了。
![20241212131529](https://liu-fu-gui.github.io/myimg/halo/20241212131529.png)
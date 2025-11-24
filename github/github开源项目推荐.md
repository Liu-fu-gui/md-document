
<!-- more -->
## 可视化路由追踪神器
https://www.nxtrace.org/

![20241129231429](https://liu-fu-gui.github.io/myimg/halo/20241129231429.png)

![20241129231436](https://liu-fu-gui.github.io/myimg/halo/20241129231436.png)

![20241129231446](https://liu-fu-gui.github.io/myimg/halo/20241129231446.png)

## 恶意流量监控
https://github.com/stamparm/maltrail
![20241129231511](https://liu-fu-gui.github.io/myimg/halo/20241129231511.png)


## 自动解密被加密的报文工具 - Galaxy
https://github.com/outlaws-bai/Galaxy

Burp插件，自动解密被加密的报文，让你像测试明文一样简单
启用后的效果启用成功后，后续代理的所有请求和响应自动解密已解密请求转到Repeater后Send，得到的响应也会被解密

![20241129231530](https://liu-fu-gui.github.io/myimg/halo/20241129231530.png)


## 一款功能强大的操作系统安全监控平台！-Glances


![20241129231539](https://liu-fu-gui.github.io/myimg/halo/20241129231539.png)

![20241129231556](https://liu-fu-gui.github.io/myimg/halo/20241129231556.png)

```
yum -y install glances
glances
```

## 使用 Loki 对 ssh 登录进行实时监控
监控各 ssh 账号成功或失败登录的总次数、登录时所用的 ip 地址、登录时间等关键数据

![20241129231618](https://liu-fu-gui.github.io/myimg/halo/20241129231618.png)
```
环境介绍：
1》Rocky Linux release 8.9 （kernel 4.18.0）
2》rsyslogd 8.2102.0 ， openssh-server-8.0p1（sshd 服务）
3》grafana-server 11.1 ，loki 3.0 ， promtail 3.0
```

具体操作流程如下：

```
1、安装系统日志管理程序
sudo dnf install rsyslog
2、将认证及授权相关日志保存到文件 /var/log/secure# ssh 配置，/etc/ssh/sshd_config
SyslogFacility AUTHPRIV
LogLevel INFO

# rsyslog 配置，/etc/rsyslog.conf
authpriv.*        /var/log/secure


3、设置 grafana 官方仓库# /etc/yum.repos.d/grafana.repo
[grafana]
name=grafana
baseurl=https://rpm.grafana.com
repo_gpgcheck=1
enabled=1
gpgcheck=1
gpgkey=https://rpm.grafana.com/gpg.key
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
4、安装 promtailsudo dnf install promtail

5、配置 promtail 采集日志文件 /var/log/secure# cat /etc/promtail/config.yml
scrape_configs:
- job_name: system
  static_configs:
  - targets:
      - localhost
    labels:
      instance: 192.168.31.13
      job: secure
      # chmod 644  /var/log/secure.log  确保 promtail 程序可读
      __path__: /var/log/secure
      
      
6、加载 ssh 日志监控仪表板模板web 端打开 Grafana Dashboards  --> New --> Import

--> 填入 ssh 仪表板模板 ID：17514  --> Load

```
7、SSH 账号登录成功
![20241129231639](https://liu-fu-gui.github.io/myimg/halo/20241129231639.png)

## 56K star！Nginx的轻量替代者，自动HTTPS的web服务器

```
sudo apt install caddy
```
## Claude
cursor - AI写代码利器
monica - all in one的AI助手
https://textto.site 优点很明显，可以白嫖。缺点呢，就是不支持站点生成内容的微调

## 推荐一款开源的内网杀手工具Bettercap 

![20241129231656](https://liu-fu-gui.github.io/myimg/halo/20241129231656.png)


## IObit Unlocker 文件解锁工具
当你尝试删除某个被进程占用的文件时，经常会遇到“文件正在被另一个程序使用”的错误提示，Unlocker就是为了解决这一难题而生。

![20241129231707](https://liu-fu-gui.github.io/myimg/halo/20241129231707.png)


## IObit Uninstaller 强制卸载软件
不仅可以像自带系统卸载软件,也能卸载浏览器插件等各种功能!

![20241129231717](https://liu-fu-gui.github.io/myimg/halo/20241129231717.png)

## Everything 文件搜索软件

![20241129231723](https://liu-fu-gui.github.io/myimg/halo/20241129231723.png)

## Iris 蓝光过滤护眼软件

![20241129231737](https://liu-fu-gui.github.io/myimg/halo/20241129231737.png)
## PicGo 图片上传管理软件

![20241129231745](https://liu-fu-gui.github.io/myimg/halo/20241129231745.png)

## 实时通知
https://github.com/Finb/Bark

![20241129231753](https://liu-fu-gui.github.io/myimg/halo/20241129231753.png)


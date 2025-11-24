
<!-- more -->
# 加速一切镜像,当前只更新docker部分

[点击打开官网](https://github.com/DaoCloud/public-image-mirror?tab=readme-ov-file#单次单镜像同步)

# 操作流程
### 1.1
docker镜像加速，加入这个地址

```
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io"
  ]
}
```
### 1.2
[点开链接，填入需要拉取的镜像](https://github.com/DaoCloud/public-image-mirror/issues/new?labels=sync+image&template=sync-image.yml)

### 1.3 输入完之后点击submit new issus确认
![20241129180019](https://liu-fu-gui.github.io/myimg/halo/20241129180019.png)
### 1.4 点击详情请查看，可以看到拉取状态
![20241129180025](https://liu-fu-gui.github.io/myimg/halo/20241129180025.png)
### 1.5 拉取状态会变成success

![image.png](http://115.29.205.107/static/img/91fa376da049256ce66c8af21c4df5e0.image.webp)

### 打开终端，进行拉取
命令为

```
docker pull m.daocloud.io/docker.io/jason5ng32/myip:latest
```

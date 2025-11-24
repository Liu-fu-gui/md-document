# 外网镜像通过github同步到阿里云私有仓库
<!-- more -->
## 复制代码到你的仓库

![20241129174905](https://liu-fu-gui.github.io/myimg/halo/20241129174905.png)

# 写变量

![20241129174912](https://liu-fu-gui.github.io/myimg/halo/20241129174912.png)

```
{
    "auth": {
        "registry.cn-hangzhou.aliyuncs.com": {
            "username": "涛涛的存钱罐",
            "password": "DOCKERHUB_PASSWORD"
        }
    },
    "images": {
        "docker.io/nginx:latest": "registry.cn-hangzhou.aliyuncs.com/imagessync/nginx"
    }
}
```
修改username为你的阿里云用户名就可以了
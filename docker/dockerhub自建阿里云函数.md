<!-- more -->
一、准备工作

| 阿里云账号 |  |
| --- | --- |
|  |  |
| 开通容器镜像服务 | x1 |
| 开通一个和FC同地域的OSS | x1 |
| 开通函数计算FC服务 | x1 |
申请一个dockerhub的账号 | x1 |

我这里用的是鬼子国的region

![iwEcAqNwbmcDBgTRAlgF0QJYBrCPfICOGvPUNwUIYJWFgKUAB9I_UGmNCAAJomltCgAL0gABI1w.png_620x10000q90.jpg](https://zywdetuchuang.oss-cn-beijing.aliyuncs.com/VanBlog/1718116859727.jpg)

# 二、开通容器镜像服务添加镜像


地区选择日本或其他 亚洲/东南亚 国家

拉取registry:2镜像 ，上传打个tag上传到容器镜像服务中


```
docker pull registry:2
docker tag [镜像ID] RegistryURL/仓库名/镜像名称:[镜像版本号]
docker login --username=账户名 --password=密码 RegistryURL
docker push RegistryURL/仓库名/镜像名称:[镜像版本号]
```

![20241129180159](https://liu-fu-gui.github.io/myimg/halo/20241129180159.png)
示例：

如果拉取不到registry:2镜像，可以用我的地址


```
docker pull registry.ap-northeast-1.aliyuncs.com/zywdockers/docker-registry:registry
```

完成后，容器镜像服务中的仓库中会出现这个镜像


![20241129180208](https://liu-fu-gui.github.io/myimg/halo/20241129180208.png)
# 三、开通FC相同region的OSS


![20241129180219](https://liu-fu-gui.github.io/myimg/halo/20241129180219.png)
这个没什么好说的，只要region选的没问题，创建的时候注意这几项就ok了

# 四、配置函数计算FC服务
打开日本region的FC函数页面 https://fcnext.console.aliyun.com/ap-northeast-1/services

![20241129180229](https://liu-fu-gui.github.io/myimg/halo/20241129180229.png)
创建函数服务

名字随便填，描述也随便填，日志尽量还是先打开，后面没问题了在关上，因为这个日志也是收费的

![20241129180240](https://liu-fu-gui.github.io/myimg/halo/20241129180240.png)


打开函数，点击服务详情，点击配置

![20241129180251](https://liu-fu-gui.github.io/myimg/halo/20241129180251.png)
配置上之前创建的OSS，backend子目录输入/ ，oss访问地址为默认地址，函数本地目录输入/data/，目录权限给读写权限


![20241129180303](https://liu-fu-gui.github.io/myimg/halo/20241129180303.png)

函数管理-->创建函数-->使用容器镜像创建 函数名称随意填，webserver模式开启，请求处理类型为http类型


![20241129180311](https://liu-fu-gui.github.io/myimg/halo/20241129180311.png)


![20241129180317](https://liu-fu-gui.github.io/myimg/halo/20241129180317.png)

往下滑找到镜像配置，选择ACR中的镜像(这里选择的是刚刚上传到镜像服务的registry:2镜像)，端口配置5000端口


![20241129180324](https://liu-fu-gui.github.io/myimg/halo/20241129180324.png)

填写环境变量 分别是

REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY #/data

REGISTRY_PROXY_USERNAME #docker.io注册的用户名

REGISTRY_PROXY_REMOTEURL #填 https://registry-1.docker.io

REGISTRY_PROXY_PASSWORD #docker.io注册的用户密码


![20241129180332](https://liu-fu-gui.github.io/myimg/halo/20241129180332.png)
点击创建就此函数创建完成


配置docker的镜像加速，只需要配置这个地址即可，如果有域名的话可以了解一下怎么绑定域名，百度啥都有


加速地址配置这个地址
![20241129180338](https://liu-fu-gui.github.io/myimg/halo/20241129180338.png)


```
cp /etc/docker/daemon.json{,.bak}
cat > /etc/docker/daemon.json < EOF
{
    "registry-mirrors": ["https://docker.??????.cn"]
}
EOF
```



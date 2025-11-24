
<!-- more -->

# docker，docker-compose安装部署
## 一 基本介绍
### 1.1 虚拟机问题

阿里云参考

https://help.aliyun.com/zh/ecs/use-cases/install-and-use-docker?spm=a2c4g.11186623.help-menu-search-25365.d_1#4787944e3bwid

虚拟机是带环境安装的一种解决方案。它可以在一种操作系统里面运行另一种操作系统，比如在Windows系统里面运行Linux系统。应用程序对此毫无感知，因为虚拟机看上去跟真实系统一模一样，而对于底层系统来说，虚拟机就是一个普通文件，不需要了就删掉，对其他部分毫无影响。

虽然用户可以通过虚拟机还原软件的原始环境。但是，这个方案有几个缺点。

（1）资源占用多

虚拟机会独占一部分内存和硬盘空间。它运行的时候，其他程序就不能使用这些资源了。哪怕虚拟机里面的应用程序，真正使用的内存只有 1MB，虚拟机依然需要几百 MB 的内存才能运行。

（2）冗余步骤多

虚拟机是完整的操作系统，一些系统级别的操作步骤，往往无法跳过，比如用户登录。

（3）启动慢

启动操作系统需要多久，启动虚拟机就需要多久。可能要等几分钟，应用程序才能真正运行。

### 1.2 什么是应用容器
我们可以把它看成虚拟机，能在一台服务器上隔离出若干个互不干扰的环境。把自己的应用放入容器还可以进行版本管理、复制、分享、修改，就像管理普通的代码一样。它具有启动快、资源占用少、体积小、易操作等等。相比虚拟机有很多优势。

为什么要使用应用容器
因为软件更新发布及部署低效，过程繁琐且需要人工介入。环境一致性难以保证，不同环境之间迁移成本太高。有了应用容器部署可以很大程度解决上面的问题。

Docker 应用容器部署
Docker是一个开源的应用容器引擎，目前有三大类。

（1）提供一次性的环境。比如，本地测试他人的软件、持续集成的时候提供单元测试和构建的环境。

（2）提供弹性的云服务。因为Docker容器可以随开随关，很适合动态扩容和缩容。

（3）组建微服务架构。通过多个容器，一台机器可以跑多个服务，因此在本机就可以模拟出微服务架

## 初始化
- 查看服务器版本：
```
cat /etc/os-release
```
- 关闭防火墙（可选，云服务器不需要）：
```
systemctl disable --now firewalld
```
- 查看当前系统内核版本：

```
uname -sr
```
- 关闭 SELinux：

```
getenforce # 查看 SELinux 是否开启
cat /etc/selinux/config # 查看 SELinux 是否开启
sed -i 's/enforcing/disabled/' /etc/selinux/config # 永久关闭 SELinux ，需要重启
setenforce 0 # 关闭当前会话的 SELinux ，重启之后无效
cat /etc/selinux/config # 查看 SELinux 是否开启
```
- 关闭 swap 分区：

```
free -h # 查看 swap 分区是否存在
swapoff -a # 关闭当前会话的 swap ，重启之后无效
sed -ri 's/.*swap.*/#&/' /etc/fstab # 永久关闭 swap ，需要重启
free -h # 查看 swap 分区是否存在
```
## 二 应用部署
### 安装docker

```
# step 1: 安装必要的一些系统工具
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
# Step 2: 添加软件源信息
sudo yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
# Step 3
sudo sed -i 's+download.docker.com+mirrors.aliyun.com/docker-ce+' /etc/yum.repos.d/docker-ce.repo
# Step 4: 更新并安装Docker-CE
sudo yum makecache fast
sudo yum -y install docker-ce
# Step 4: 开启Docker服务
sudo service docker start
```
### 2.1 配置加速
默认拉取镜像是从这里拉取(https://hub.docker.com)，国外地址拉取的速度比较慢。我们也可以配置国内镜像源。

阿里云镜像加速器
访问地址：https://help.aliyun.com/document_detail/60750.html，进入容器镜像服务控制台创建加速器。


![20241129205806](https://liu-fu-gui.github.io/myimg/halo/20241129205806.png)

### 2.2 配置镜像加速器
针对Docker客户端版本大于 1.10.0 的用户

您可以通过修改daemon配置文件/etc/docker/daemon.json来使用加速器


```
mkdir -pv /etc/docker
tee /etc/docker/daemon.json <<-'EOF'
{
  "exec-opts": [
    "native.cgroupdriver=systemd"  // 使用 systemd 作为 cgroup 驱动
  ],
  "registry-mirrors": [  // 指定 Docker 镜像加速器
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.top"
  ],
  "data-root": "/home/docker/docker",
  "live-restore": true,  // 启用 Live Restore 功能，允许容器在守护进程重启时保持运行
  "log-driver": "json-file",  // 使用 JSON 文件作为日志驱动
  "log-opts": {  // 日志选项配置
    "max-size": "500m",  // 每个日志文件的最大大小为 500MB
    "max-file": "3"  // 最多保留 3 个日志文件，旧的日志文件会被删除
  },
  "max-concurrent-downloads": 10,  // 设置同时最大下载的镜像数为 10
  "max-concurrent-uploads": 5  // 设置同时最大上传的镜像数为 5
}


EOF
systemctl daemon-reload \
    && systemctl restart docker
```
### 格式化检查

```
cat /etc/docker/daemon.json | jq .
docker info --format '{{.DockerRootDir}}'
```
### 2.3 查看docker版本号

```
docker --version
```

### 2.4 docker卸载，看需要

```
systemctl disable --now docker docker.socket
dnf -y remove docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin \
    docker-ce-rootless-extras
rm -rf /var/lib/docker
rm -rf /var/lib/containerd
```


### docker升级
卸载旧的
```
sudo yum remove docker docker-common docker-snapshot docker-io
```



### 2.5 安装docker-compose
官方文档：https://docs.docker.com/compose/install/

#### 独立版
```
curl -SL https://github.com/docker/compose/releases/download/v2.30.3/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose

https://github.com/docker/compose/releases/download/v2.32.0/docker-compose-windows-x86_64.exe
```
#### 插件版


```
yum -y install docker-compose-plugin
docker compose version
```


### 2.7 将可执行权限应用于该二进制文件

```
sudo chmod +x /usr/local/bin/docker-compose
```
### 2.8 查看docker-compose版本号

```
docker-compose --version
```

## 如果出现无法显示版本的问题

![20241129205821](https://liu-fu-gui.github.io/myimg/halo/20241129205821.png)
```

 ll /usr/local/bin/docker-compose 
-rwxr-xr-x 1 root root 64044282 Nov 20 09:35 /usr/local/bin/docker-compose

 docker-compose -v
-bash: docker-compose: command not found

 /usr/local/bin/docker-compose --version
Docker Compose version v2.30.2


## 先检查本地环境
 echo $PATH
/usr/local/sbin:/usr/bin:/bin:/usr/sbin:/sbin:/root/bin
## 没有包含这个地址，所以不生效 执行一下命令加入
 echo 'export PATH=$PATH:/usr/local/bin' >> ~/.bashrc
 source ~/.bashrc
 docker-compose --version
Docker Compose version v2.30.2
```

## 三 最后的tab补全docker docker：tag

### 3.1 安装 bash-completion

```
yum install -y bash-completion
```
安装完成之后重启系统或者重新登录 shell。如果安装成功。键入 docker p 后，再 Tab 键，系统显示如下：

```
pause   plugin  port    ps      pull    push
```
此时，我们运行例如 docker run 之类的命令，键入镜像的首字母，镜像名称依然无法自动补全。

### 3.2 根据 Docker 官方文档进一步配置


```
sudo curl -L https://raw.githubusercontent.com/docker/compose/1.24.1/contrib/completion/bash/docker-compose -o /etc/bash_completion.d/docker-compose
source /etc/bash_completion.d/docker-compose
```

至此，所有补全功能相关的准备工作完成！执行 docker run 类似的命令时，镜像名称及 tag 均能自动补全了。

<!-- more -->




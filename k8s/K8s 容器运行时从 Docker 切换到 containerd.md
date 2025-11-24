# K8s 容器运行时从 Docker 切换到 containerd
<!-- more -->
## 一、引言
在容器化技术的世界里，Kubernetes（K8s）无疑是最具影响力的编排工具之一。而容器运行时作为 K8s 的关键组成部分，对于容器的创建、运行和管理起着至关重要的作用。Docker 曾经是 K8s 中最常用的容器运行时，但随着技术的发展，containerd 逐渐崭露头角，并成为越来越多用户的选择。本文将深入探讨 K8s 容器运行时从 Docker 切换到 containerd 的原因、过程和优势，并详细介绍具体的操作步骤。
## 二、Docker 与 containerd 的简介
### 1. Docker
- Docker 是一个开源的容器化平台，它提供了一种便捷的方式来创建、部署和运行容器。Docker 通过使用镜像来打包应用程序及其依赖项，使得应用程序可以在不同的环境中快速部署和运行。
- Docker 的架构包括 Docker 引擎、Docker 镜像、Docker 容器和 Docker 仓库等组件。Docker 引擎是核心组件，负责创建和管理容器。Docker 镜像则是容器的模板，包含了应用程序及其依赖项。Docker 容器是运行中的实例，由 Docker 引擎根据镜像创建。Docker 仓库用于存储和分发镜像。
### 2. containerd
- containerd 是一个工业级的容器运行时，它专注于容器的运行时管理，提供了高效、可靠的容器运行环境。containerd 被设计为可嵌入到其他系统中，如 Kubernetes，以提供容器运行时的功能。
- containerd 的架构相对简单，主要由 containerd 守护进程和 containerd-shim 进程组成。containerd 守护进程负责管理容器的生命周期，包括创建、启动、停止和删除容器。containerd-shim 进程则负责在容器运行时与容器进行交互，确保容器的隔离性和安全性。


## 三、为什么要从 Docker 切换到 containerd
### 1.更好的性能和资源利用率 
- containerd 被设计为轻量级的容器运行时，相比 Docker 具有更好的性能和资源利用率。containerd 直接与 Linux 内核的容器技术（如 cgroups 和 namespaces）进行交互，减少了中间层的开销，从而提高了容器的启动速度和运行效率。
- 此外，containerd 对资源的管理更加精细，可以更好地控制容器的内存、CPU 和磁盘使用，提高资源的利用率。
### 2. 更紧密的与 Kubernetes 集成
- Kubernetes 从 1.20 版本开始，默认的容器运行时从 Docker 切换到了 containerd。这意味着 Kubernetes 与 containerd 的集成更加紧密，可以更好地利用 containerd 的特性和功能。
- 例如，Kubernetes 可以通过 containerd 的 CRI（Container Runtime Interface）实现对容器的更精细的管理，包括容器的健康检查、资源限制和日志收集等。

### 3. 更好的安全性
- containerd 提供了更强大的安全功能，包括容器的隔离性、访问控制和安全策略等。containerd 通过使用 Linux 内核的安全机制，如 seccomp 和 AppArmor，来限制容器的权限，提高容器的安全性。
- 此外，containerd 还支持多种安全认证方式，如 TLS 和 OAuth2，可以确保容器的通信安全。
### 4. 更好的可扩展性和灵活性
- containerd 是一个可扩展的容器运行时，可以通过插件的方式来扩展其功能。用户可以根据自己的需求，开发和安装自定义的插件，以满足特定的业务需求。
- 同时，containerd 也支持多种容器格式，如 OCI（Open Container Initiative）和 Docker 镜像格式，使得用户可以在不同的容器生态系统中进行选择和切换。
## 四、切换过程
### 1. 评估和准备
- 在进行切换之前，首先需要对当前的环境进行评估，确定切换的可行性和影响。评估的内容包括：
- - 现有应用程序对 Docker 的依赖程度。
- - 容器的数量和规模。
- - 网络和存储配置。
- - 监控和日志系统。
- 根据评估结果，制定切换计划，包括切换的时间、步骤和风险应对措施。同时，还需要准备好切换所需的工具和资源，如 containerd 的安装包、配置文件和文档等。
### 2. 安装和配置 containerd
-  安装 containerd 可以通过多种方式进行，如使用包管理器、二进制安装或容器化安装等。具体的安装方法可以根据不同的操作系统和环境进行选择。以下是在 Ubuntu 系统上使用包管理器安装 containerd 的步骤：
- 更新软件包列表：
```
apt update 
apt install containerd
```
在安装完成后，需要对 containerd 进行配置，以满足特定的业务需求。配置的内容包括：
- 容器存储驱动。containerd 支持多种存储驱动，如 overlay2、aufs 等。可以根据实际情况选择合适的存储驱动。
- 网络配置。可以配置 containerd 使用的网络插件，如 CNI（Container Network Interface）插件。
- 安全认证方式。可以配置 containerd 使用的安全认证方式，如 TLS 和 OAuth2。
- 日志收集和监控配置。可以配置 containerd 的日志收集和监控方式，如使用 Prometheus 和 Grafana 进行监控。

### 1. 迁移容器和镜像
- 在切换过程中，需要将现有的容器和镜像从 Docker 迁移到 containerd。迁移的方法可以根据不同的情况进行选择，如：
- - 对于正在运行的容器，可以使用 Kubernetes 的滚动更新功能，逐步将容器从 Docker 切换到 containerd。具体步骤如下：
- 修改 Kubernetes 集群中节点的 kubelet 配置文件，将容器运行时从 Docker 切换到 containerd。
- 使用 kubectl 命令逐步更新 Deployment、StatefulSet 等资源对象，使容器在更新过程中使用 containerd 运行时。
- 对于离线的容器和镜像，可以使用工具如 skopeo 进行迁移。以下是使用 skopeo 迁移 Docker 镜像到 containerd 的步骤：

```
apt install skopeo   - 查找要迁移的 Docker 镜像的名称和标签：docker images   - 使用 skopeo 命令将 Docker 镜像复制到 containerd 存储库：skopeo copy docker://docker-image-name:tag docker-daemon://containers-storage:containers-storage:tag
```
- 在迁移过程中，需要注意容器和镜像的兼容性问题，确保迁移后的容器可以正常运行。
### 1. 测试和验证

在完成迁移后，需要对新的容器运行时进行测试和验证，确保其功能和性能符合预期。测试的内容包括：
- 容器的创建、启动、停止和删除。
- 容器的网络和存储功能。
- 容器的资源限制和监控功能。
- 应用程序的兼容性和稳定性。
如果发现问题，需要及时进行排查和修复，确保切换的顺利进行。
### 2. 切换和监控
####  在测试和验证通过后，可以正式将容器运行时从 Docker 切换到 containerd。切换的方法可以根据不同的环境进行选择，如：
 - -对于 Kubernetes 集群，可以通过修改 kubelet 的配置文件，将容器运行时从 Docker 切换到 containerd。具体步骤如下：
 - -编辑 kubelet 配置文件，将容器运行时设置为 containerd：
 - 

```
 vi /var/lib/kubelet/config.yaml
```
在文件中找到 containerRuntime 字段，将其值设置为 containerd。 
- 重启 kubelet 服务：

```
sudo systemctl restart kubelet
```
对于独立的容器运行环境，可以直接停止 Docker 服务，启动 containerd 服务。
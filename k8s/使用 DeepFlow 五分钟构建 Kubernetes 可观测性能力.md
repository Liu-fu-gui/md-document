
<!-- more -->

:::tip{title="简介"}
DeepFlow 是一个基于 eBPF 技术的云原生监控平台。它不需要修改现有代码，就能展示应用的性能指标、访问路径和调用链等信息。使用 DeepFlow，云原生应用可以自动获得详细的监控能力，减少开发者手动添加监控代码的麻烦，同时为 DevOps 和 SRE 团队提供从代码到基础设施的全面监控和诊断能力。
:::
## 部署前环境准备
DeepFlow 支持多种环境部署，本篇文章以 K8s  All-in-One 环境为例来演示如何部署 DeepFlow。在开始部署 DeepFlow 之前，需要先搭建 K8s 环境，步骤如下：通过 Sealos 快速安装 K8s  All-in-One 环境：

```
# 安装 Sealos 并添加执行权限
curl -o /usr/bin/sealos https://deepflow-ce.oss-cn-beijing.aliyuncs.com/sealos/sealos
chmod +x /usr/bin/sealos

# 通过 Sealos 安装 AllInOne K8s 集群
sealos run labring/kubernetes:v1.24.0 labring/calico:v3.22.1 --masters $K8S_MASTER_IP -p $K8S_MASTER_PASSWORD

# 删除 Master 节点污点
kubectl taint node node-role.kubernetes.io/master- node-role.kubernetes.io/control-plane- --all
```
通过 Sealos 快速安装 Helm（版本须高于 3.7.0）：

```
sealos run labring/helm:v3.8.2
```
## 部署 DeepFlow
DeepFlow 主要由 Agent 和 Server 两个组件构成。Agent 以各种形态广泛运行于 Serverless Pod、K8s Node、云服务器、虚拟化宿主机等环境中，采集这些环境中所有应用进程的观测数据。Server 运行在一个 K8s 集群中，提供 Agent 管理、数据标签注入、数据写入、数据查询等服务。

![20241129230831](https://liu-fu-gui.github.io/myimg/halo/20241129230831.png)
本案例中，Agent 是以 Daemset 的形式部署在 Node 上，而 Server 则是通过 Deployment 的形式部署在 Node 上。社区提供了 Helm 的部署方式，可快速拉起来 DeepFlow 的 Agent 与 Server。下面展示部署过程：
### 通过 Helm 拉取 DeepFlow Helm Chart 包：

```
# 添加 DeepFlow 仓库并更新
helm repo add deepflow https://deepflowio.github.io/deepflow
helm repo update deepflow

# 查看可拉取的 Chart 包版本
helm search repo deepflow/deepflow -l

# 拉取指定版本并解压
helm pull deepflow/deepflow --version 6.5.012
tar xf deepflow-6.5.012.tgz
```
拉取并解压包之后，可以按需修改 value.yaml 参数，其中 allInOneLocalStorage 必须修改为 true。其他参数的使用参考社区提供的生产环境部署建议。

```
global:
  # 部署 AllInOne DeepFlow 环境
  allInOneLocalStorage: true
```
修改完 value.yaml 后，则使用 Helm 部署 DeepFlow，部署成功后，将看到如下的 Pod：

```
helm upgrade --install deepflow ./deepflow -n deepflow --create-namespace

# 注: 磁盘性能不行（例如使用机械硬盘），可能会导致 server 初始化失败
kubectl get pods -n deepflow
NAME                                       READY   STATUS    RESTARTS   AGE
deepflow-agent-xbg9n                       1/1     Running   0          5m
deepflow-app-77cbcc69f7-hz4s6              1/1     Running   0          5m
deepflow-clickhouse-0                      1/1     Running   0          5m
deepflow-grafana-7f68b548b9-xf6gc          1/1     Running   2          5m
deepflow-mysql-779ffd5f7d-j6v2v            1/1     Running   0          5m
deepflow-server-7c9468d8c6-lpv2q           1/1     Running   3          5m
deepflow-stella-agent-ce-f494bcddc-mbxkb   1/1     Running   0          5m
```
部署完成后，理论上 deepflow-agent 会自动与 deepflow-server 注册，等待 5 分钟左右就能看到 K8s 环境中的可观测数据了，不过最好手动确认下是否注册成功。通过命令行工具 deepflow-ctl 校验 deepflow-agent 是否注册成功：

```
# 通过命令行工具检测 deepflow-agent 状态是否为 NORMAL
deepflow-ctl agent list
ID   NAME      TYPE     CTRL_IP     CTRL_MAC            STATE    GROUP     EXCEPTIONS   REVISION     UPGRADE_REVISION   
1    test-V1   K8S_VM   10.2.8.56   00:16:3e:2e:5c:2c   NORMAL   default                v6.5 10865
```
DeepFlow 数据查看都是依赖 Grafana，Grafana 服务是通过  NodePort 形式部署，先找到其访问的端口：

```
kubectl get --namespace deepflow -o jsonpath="{.spec.ports[0].nodePort}" services deepflow-grafana
# 输出 grafana svc nodeport 端口
3000
```
通过 $node_ip:$grafana_port 登录 Grafana，默认账户为：admin/deepflow。

![20241129230902](https://liu-fu-gui.github.io/myimg/halo/20241129230902.png)
社区已经提供了不少 Dashboard，可以按需查看。
#### 应用性能指标

![20241129230913](https://liu-fu-gui.github.io/myimg/halo/20241129230913.png)
#### SQL 监控（还有不少 Ingress/RPC/DNS 等）

![20241129230919](https://liu-fu-gui.github.io/myimg/halo/20241129230919.png)
#### 调用详情

![20241129230926](https://liu-fu-gui.github.io/myimg/halo/20241129230926.png)
#### 分布式调用链追踪

![20241129230932](https://liu-fu-gui.github.io/myimg/halo/20241129230932.png)

#### 性能剖析

![20241129230939](https://liu-fu-gui.github.io/myimg/halo/20241129230939.png)
# 多master高可用安装（ubt22.04）

## 1.节点信息

| IP           | hostname |
| ------------ | -------- |
| 10.100.23.51 | master01 |
| 10.100.23.54 | master02 |
| 10.100.23.52 | node1    |
| 10.100.23.53 | node2    |
| 10.100.23.60 | k8svip   |

## 2.安装前准备

```
cat > /etc/hosts <<EOF
10.100.23.51 master01
10.100.23.54 master02
10.100.23.52 node1
10.100.23.53 node2
EOF

## dns
sudo ln -sf /run/systemd/resolve/resolv.conf /etc/resolv.conf
cat /etc/resolv.conf
```



## 3.初始化脚本（所有节点执行）

```
#!/usr/bin/env bash

###############################################################################
# 通用 Linux 发行版的 Kubernetes 节点初始化脚本示例
# 适用发行版：CentOS / RedHat / Rocky / Alma / Fedora / Ubuntu / Debian ...
# ----------------------------------------------------------------------------
# 1. 时间同步：安装并启动 chrony
# 2. 设置时区为 Asia/Shanghai
# 3. 禁用 swap
# 4. 禁用防火墙（示例）
# 5. 加载必要的内核模块 (overlay, br_netfilter, ip_vs 等)
# 6. 设置内核转发等 sysctl 参数
# 7. 安装 containerd (示例) 并配置 systemd cgroup
# 8. 配置 crictl
# ----------------------------------------------------------------------------
# 注意：脚本仅作示例，具体命令和步骤需根据环境酌情修改。
###############################################################################

set -e

# -----------------------------------------------------------------------------
# 0. 判断操作系统类型，以便选择合适的包管理器 (yum/apt/dnf 等)
# -----------------------------------------------------------------------------
echo "[Info] Detecting OS type ..."
if [ -f /etc/os-release ]; then
  . /etc/os-release
  OS_FAMILY=$ID   # e.g. "centos", "ubuntu", "debian", "rocky"...
else
  echo "[Error] /etc/os-release not found. Cannot detect OS."
  exit 1
fi

# 定义一个函数，方便在后面统一调用安装命令
install_pkg() {
  local pkg="$1"
  case "$OS_FAMILY" in
    centos|rocky|alma|rhel|ol|fedora)
      # yum 或 dnf
      if command -v dnf &>/dev/null; then
        dnf install -y "$pkg"
      else
        yum install -y "$pkg"
      fi
      ;;
    ubuntu|debian|linuxmint)
      apt-get update -y
      apt-get install -y "$pkg"
      ;;
    *)
      echo "[Warning] 未识别的系统，无法自动安装: $pkg"
      ;;
  esac
}

# -----------------------------------------------------------------------------
# 1. 时间同步：安装并启动 chrony
# -----------------------------------------------------------------------------
echo "[Info] Installing and starting chrony ..."
install_pkg chrony

if [ -x "$(command -v systemctl)" ]; then
  systemctl enable chronyd || systemctl enable chrony || true
  systemctl start chronyd  || systemctl start chrony  || true
fi

if command -v chronyc &>/dev/null; then
  echo "[Info] Chrony sources:"
  chronyc sources -v || true
fi

# -----------------------------------------------------------------------------
# 2. 设置时区为 Asia/Shanghai
# -----------------------------------------------------------------------------
echo "[Info] Setting timezone to Asia/Shanghai ..."
timedatectl set-timezone Asia/Shanghai || echo "[Warning] timedatectl not supported?"

# -----------------------------------------------------------------------------
# 3. 禁用 swap
# -----------------------------------------------------------------------------
echo "[Info] Disabling swap ..."
swapoff -a
# 注释 /etc/fstab 中的 swap 分区
sed -i.bak -r 's/(.*swap.*)/#\1/g' /etc/fstab

# -----------------------------------------------------------------------------
# 4. 禁用防火墙 (仅作示例)
# -----------------------------------------------------------------------------
echo "[Info] Disabling firewall (example) ..."
# 如果是基于 RHEL/CentOS
if [[ "$OS_FAMILY" =~ ^(centos|rhel|rocky|alma|ol|fedora)$ ]]; then
  systemctl disable firewalld --now || true
# 如果是基于 Debian/Ubuntu
elif [[ "$OS_FAMILY" =~ ^(ubuntu|debian|linuxmint)$ ]]; then
  # 可能安装 ufw
  if command -v ufw &>/dev/null; then
    ufw disable || true
  fi
fi

# -----------------------------------------------------------------------------
# 5. 加载 Kubernetes 所需内核模块 (overlay, br_netfilter, ip_vs 等)
# -----------------------------------------------------------------------------
echo "[Info] Loading kernel modules for Kubernetes ..."
cat <<EOF | tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

modprobe overlay || true
modprobe br_netfilter || true

# -----------------------------------------------------------------------------
# 6. 设置 sysctl 参数
# -----------------------------------------------------------------------------
echo "[Info] Setting sysctl params ..."
cat <<EOF | tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sysctl --system || true

# -----------------------------------------------------------------------------
# 7. 安装并配置 IPVS (可选)
# -----------------------------------------------------------------------------
echo "[Info] Installing ipset/ipvsadm if needed ..."
install_pkg ipset
install_pkg ipvsadm

cat <<EOF | tee /etc/modules-load.d/ipvs.conf
ip_vs
ip_vs_rr
ip_vs_wrr
ip_vs_sh
nf_conntrack
EOF

modprobe ip_vs       || true
modprobe ip_vs_rr    || true
modprobe ip_vs_wrr   || true
modprobe ip_vs_sh    || true
modprobe nf_conntrack || true

# -----------------------------------------------------------------------------
# 8. 安装 containerd (示例)
# -----------------------------------------------------------------------------
echo "[Info] Installing containerd ..."
install_pkg containerd

# 若某些系统版本没有 containerd 包，可自行添加 repo 或改用二进制安装

# 生成默认配置
mkdir -p /etc/containerd
if command -v containerd &>/dev/null; then
  containerd config default | tee /etc/containerd/config.toml >/dev/null 2>&1 || true

  # 修改 SystemdCgroup
  sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

  # 修改 sandbox_image
  sed -i 's#registry.k8s.io/pause#registry.cn-hangzhou.aliyuncs.com/google_containers/pause#' /etc/containerd/config.toml
fi

# -----------------------------------------------------------------------------
# 9. 配置 crictl
# -----------------------------------------------------------------------------
echo "[Info] Configuring crictl ..."
cat <<EOF | tee /etc/crictl.yaml
runtime-endpoint: unix:///var/run/containerd/containerd.sock
image-endpoint: unix:///var/run/containerd/containerd.sock
timeout: 10
debug: false
EOF

# -----------------------------------------------------------------------------
# 10. 重启 containerd 并设为开机自启
# -----------------------------------------------------------------------------
echo "[Info] Enabling and restarting containerd ..."
if [ -x "$(command -v systemctl)" ]; then
  systemctl enable containerd
  systemctl restart containerd
fi

echo "[Info] 全部完成。系统已经为Kubernetes安装做好了准备"
```

## 4. 安装haproxy&keepalived(在HaProxy节点执行)

```
apt install -y haproxy

cat <<EOF > /etc/haproxy/haproxy.cfg

# 全局配置
global
    log /dev/log local0               # 日志输出位置，使用系统本地日志 local0
    chroot /var/lib/haproxy           # HAProxy运行的根目录，以提高安全性
    pidfile /var/run/haproxy.pid      # 存放HAProxy进程PID的文件
    user haproxy                      # HAProxy运行的用户
    group haproxy                     # HAProxy运行的组
    daemon                            # 以守护进程方式运行
    maxconn 4000                      # 最大并发连接数

# 默认参数（继承给所有frontend和backend）
defaults
    mode tcp                          # 运行模式为TCP，适合K8s API Server（TLS通信）
    log global                        # 日志继承自全局配置
    timeout connect 10s               # 连接建立超时时间
    timeout client 1m                 # 客户端请求超时时间
    timeout server 1m                 # 服务端响应超时时间

# 前端服务（HAProxy对外监听端口）
frontend apiserver
    bind *:8443                       # 监听所有网卡上的8443端口，外部访问API Server的入口
    default_backend apiserver         # 所有请求转发到名为apiserver的后端服务器组

# 后端服务（实际的Kubernetes API Server节点）
backend apiserver
    mode tcp                          # 后端同样运行在TCP模式
    balance roundrobin                # 使用轮询算法分发流量到后端节点（负载均衡策略）
    option ssl-hello-chk              # 通过SSL握手检查后端节点健康状态，适用于TLS场景
    server master01 10.100.23.51:6443 check  # 后端真实节点（master01的API Server，默认6443端口）

EOF


# 验证
root@master01:~# netstat -tulnp | grep 8443
tcp        0      0 0.0.0.0:8443            0.0.0.0:*               LISTEN      84657/haproxy       
root@master01:~# 
```

##  确认要点：

- **健康检查脚本必须存在并可执行**。
- VIP (`10.100.23.60`) 应为当前节点所在网段的未使用IP。
- 主备节点 `virtual_router_id` 和 `auth_pass` 必须保持一致。
- 主节点`priority`值比备节点高（例如主100，备90）。

```
客户端请求 -> VIP (10.100.23.60:8443)
                     │
        ┌────────────┴───────────┐
        │                        │
Keepalived(Master)        Keepalived(Backup)
(10.100.23.51)             (10.100.23.52)
        │                        │
HAProxy(8443端口)          HAProxy(8443端口)
        └───────────┬────────────┘
                    │
     ┌──────────────┴─────────────────┐
   kube-apiserver (6443端口，多个master节点)
```

### 4.1. 主节点配置（master）：(在HaProxy节点执行)

```
apt install -y keepalived

cat <<EOF > /etc/keepalived/keepalived.conf
# 全局定义部分：设置路由器标识（router_id）及其他全局参数
global_defs {
    router_id LVS_DEVEL   # 主节点标识（可自定义）
    # 此处还可以增加邮件通知、SNMP 等其他全局配置
}

# 定义健康检查脚本，用于检测 API Server 状态
vrrp_script check_apiserver {
    # 指定检测脚本的完整路径（请确保该脚本可执行）
    script "/etc/keepalived/check_apiserver.sh"
    # 每隔 3 秒运行一次检测
    interval 3
    # 检测失败时降低优先级 2 分
    weight -2
    # 连续 10 次失败后认定服务异常
    fall 10
    # 连续 2 次成功后恢复状态
    rise 2
}

# 定义 VRRP 实例，用于在主备节点间漂移 VIP
vrrp_instance VI_1 {
    # 主节点状态
    state MASTER
    # 指定用于发送 VRRP 包的网卡名称（根据实际情况修改）
    interface ens18
    # 虚拟路由 ID，主备节点必须一致
    virtual_router_id 51
    # 本节点的优先级（主节点设置较高，如 100）
    priority 100
    # 广播间隔（秒）
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456  # 主备节点认证密码必须保持一致
    }
    # 配置虚拟 IP 地址（VIP），将在主备节点间漂移
    virtual_ipaddress {
        10.100.23.60
    }
    # 使用上面定义的健康检测脚本来调整优先级
    track_script {
        check_apiserver
    }
}

EOF
```

### 4.2. 备节点配置（backup）：(在HaProxy节点执行)

```
apt install -y keepalived

cat <<EOF > /etc/keepalived/keepalived.conf
# 全局定义部分：设置路由器标识（router_id）及其他全局参数
global_defs {
    router_id LVS_DEVEL_BACKUP   # 备节点标识，可与主节点不同，便于区分
    # 此处还可以增加邮件通知、SNMP 等其他全局配置
}

# 定义健康检查脚本，用于检测 API Server 状态
vrrp_script check_apiserver {
    # 指定检测脚本的完整路径（请确保该脚本可执行）
    script "/etc/keepalived/check_apiserver.sh"
    # 每隔 3 秒运行一次检测
    interval 3
    # 检测失败时降低优先级 2 分
    weight -2
    # 连续 10 次失败后认定服务异常
    fall 10
    # 连续 2 次成功后恢复状态
    rise 2
}

# 定义 VRRP 实例，用于在主备节点间漂移 VIP
vrrp_instance VI_1 {
    # 备节点状态
    state BACKUP
    # 指定用于发送 VRRP 包的网卡名称（根据实际情况修改）
    interface ens18
    # 虚拟路由 ID，主备节点必须一致
    virtual_router_id 51
    # 本节点的优先级设置比主节点低（例如 90）
    priority 90
    # 广播间隔（秒）
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 123456  # 主备节点认证密码必须保持一致
    }
    # 配置虚拟 IP 地址（VIP），与主节点一致
    virtual_ipaddress {
        10.100.23.60
    }
    # 使用上面定义的健康检测脚本来调整优先级
    track_script {
        check_apiserver
    }
}

EOF

```

### 4.3. 在HaProxy节点执行

```
更新后的健康检测脚本(/etc/keepalived/check_apiserver.sh)

cat <EOF> /etc/keepalived/check_apiserver.sh

#!/bin/sh
#-----------------------------------------------------------
# Health Check Script for Kubernetes API Server
#-----------------------------------------------------------
# 此脚本用于检测本机上 API Server 的健康状态，
# 先通过 localhost 检查，如果发现配置了 VIP，则也通过 VIP 检查。
#-----------------------------------------------------------

# 定义错误处理函数
errorExit() {
    echo "*** $1" 1>&2
    exit 1
}

# 定义 API Server 的虚拟 IP 和目标端口
APISERVER_VIP=10.100.23.60
APISERVER_DEST_PORT=8443

# 检查本地 API Server 健康接口（此处假定健康接口为 /healthz）
curl --silent --max-time 2 --insecure "https://localhost:${APISERVER_DEST_PORT}/healthz" -o /dev/null || \
    errorExit "Error GET https://localhost:${APISERVER_DEST_PORT}/healthz"

# 如果本机上已配置了 VIP，则通过 VIP 进行检查
if ip addr | grep -q "${APISERVER_VIP}"; then
    curl --silent --max-time 2 --insecure "https://${APISERVER_VIP}:${APISERVER_DEST_PORT}/healthz" -o /dev/null || \
        errorExit "Error GET https://${APISERVER_VIP}:${APISERVER_DEST_PORT}/healthz"
fi

# 如果以上检测全部通过，则返回 0（正常）
exit 0

EOF
```

### 4.4  重启&开机自启，测试

```

systemctl enable haproxy --now
systemctl enable keepalived --now

# 启动后可以ping一下vip看是否存在
ping 10.100.23.60
```

## 5. 安装k8s

```

#!/bin/bash
set -e

# 安装依赖
sudo apt-get update && sudo apt-get install -y apt-transport-https ca-certificates curl gpg

# 添加 Kubernetes 的 key
curl -fsSL https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# 添加 Kubernetes apt 仓库（使用阿里云镜像源）
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 更新 apt 索引
sudo apt update

# 查看 kubeadm 版本列表（可选）
apt-cache madison kubeadm

# 安装 kubelet、kubeadm、kubectl（默认安装最新版本，本例安装版本为1.28.2）
sudo apt-get install -y kubelet kubeadm kubectl

# 锁定版本，不随 apt upgrade 更新
sudo apt-mark hold kubelet kubeadm kubectl

# 安装 kubectl 命令补全
sudo apt install -y bash-completion
kubectl completion bash | sudo tee /etc/profile.d/kubectl_completion.sh > /dev/null
source /etc/profile.d/kubectl_completion.sh

# 生成默认配置文件
kubeadm config print init-defaults

# 生成包含 KubeProxyConfiguration 和 KubeletConfiguration 的配置文件，并保存到 kubeadm-init.yaml
kubeadm config print init-defaults --component-configs 
KubeProxyConfiguration,KubeletConfiguration > kubeadm-init.yaml
```

### etcd高可用

https://github.com/etcd-io/etcd/releases

```
# 查看版本号
curl -s https://api.github.com/repos/etcd-io/etcd/releases/latest | grep tag_name
```



### 5.1完整示例

```
root@master01:~# cat kubeadm-init.yaml 
apiVersion: kubeadm.k8s.io/v1beta3
bootstrapTokens:
- groups:
  - system:bootstrappers:kubeadm:default-node-token
  token: abcdef.0123456789abcdef
  ttl: 24h0m0s
  usages:
  - signing
  - authentication
kind: InitConfiguration
localAPIEndpoint:
  advertiseAddress: 10.100.23.51           # 本机IP
  bindPort: 6443
nodeRegistration:
  criSocket: unix:///var/run/containerd/containerd.sock
  imagePullPolicy: IfNotPresent
  name: master01                               #本机主机名，要在hosts文件中有映射
  taints: null
---
apiServer:
  timeoutForControlPlane: 4m0s
apiVersion: kubeadm.k8s.io/v1beta3
certificatesDir: /etc/kubernetes/pki
clusterName: kubernetes
controlPlaneEndpoint: "10.100.23.60:8443"            #vip+端口
controllerManager: {}
dns: {}
etcd:
  external:                                    #外部ETCD集群IP+端口(高可用)
    endpoints:
      - http://10.100.23.51:2371
      - http://10.100.23.52:2371
imageRepository: registry.cn-hangzhou.aliyuncs.com/google_containers
kind: ClusterConfiguration
kubernetesVersion: 1.28.0  # 版本根据生成的版本走
networking:
  dnsDomain: cluster.local
  serviceSubnet: 10.96.0.0/12
  podSubnet: 10.244.0.0/16                       #添加pod网络
scheduler: {}
---
apiVersion: kubeproxy.config.k8s.io/v1alpha1
bindAddress: 0.0.0.0
bindAddressHardFail: false
clientConnection:
  acceptContentTypes: ""
  burst: 0
  contentType: ""
  kubeconfig: /var/lib/kube-proxy/kubeconfig.conf
  qps: 0
clusterCIDR: ""
configSyncPeriod: 0s
conntrack:
  maxPerCore: null
  min: null
  tcpCloseWaitTimeout: null
  tcpEstablishedTimeout: null
detectLocal:
  bridgeInterface: ""
  interfaceNamePrefix: ""
detectLocalMode: ""
enableProfiling: false
healthzBindAddress: ""
hostnameOverride: ""
iptables:
  masqueradeAll: false
  masqueradeBit: null
  minSyncPeriod: 0s
  syncPeriod: 0s
ipvs:
  excludeCIDRs: null
  minSyncPeriod: 0s
  scheduler: ""
  strictARP: false
  syncPeriod: 0s
  tcpFinTimeout: 0s
  tcpTimeout: 0s
  udpTimeout: 0s
kind: KubeProxyConfiguration
metricsBindAddress: ""
mode: "ipvs"                # 添加ipvs配置
nodePortAddresses: null
oomScoreAdj: null
portRange: ""
showHiddenMetricsForVersion: ""
udpIdleTimeout: 0s
winkernel:
  enableDSR: false
  forwardHealthCheckVip: false
  networkName: ""
  rootHnsEndpointName: ""
  sourceVip: ""
---
apiVersion: kubelet.config.k8s.io/v1beta1
authentication:
  anonymous:
    enabled: false
  webhook:
    cacheTTL: 0s
    enabled: true
  x509:
    clientCAFile: /etc/kubernetes/pki/ca.crt
authorization:
  mode: Webhook
  webhook:
    cacheAuthorizedTTL: 0s
    cacheUnauthorizedTTL: 0s
cgroupDriver: systemd
clusterDNS:
- 10.96.0.10
clusterDomain: cluster.local
cpuManagerReconcilePeriod: 0s
evictionPressureTransitionPeriod: 0s
fileCheckFrequency: 0s
healthzBindAddress: 127.0.0.1
healthzPort: 10248
httpCheckFrequency: 0s
imageMinimumGCAge: 0s
kind: KubeletConfiguration
logging:
  flushFrequency: 0
  options:
    json:
      infoBufferSize: "0"
  verbosity: 0
memorySwap: {}
nodeStatusReportFrequency: 0s
nodeStatusUpdateFrequency: 0s
resolvConf: /run/systemd/resolve/resolv.conf
rotateCertificates: true
runtimeRequestTimeout: 0s
shutdownGracePeriod: 0s
shutdownGracePeriodCriticalPods: 0s
staticPodPath: /etc/kubernetes/manifests
streamingConnectionIdleTimeout: 0s
syncFrequency: 0s
volumeStatsAggPeriod: 0s
```

```
 ## 查看所需镜像列表
kubeadm config images list --config kubeadm-init.yaml

## 预拉取镜像
kubeadm config images pull --config kubeadm-init.yaml

##镜像拉取完后我们查看本地镜像是否下载成功
crictl images

#初始化开始
kubeadm init --config=kubeadm-init.yaml | tee kubeadm-init.log


# 多master执行到  预拉取镜像 这一步就好了，其他master节点不需要初始化
# 配置api所需文件
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config



## 不全
apt install  -y bash-completion
echo 'source <(kubectl completion bash)' >>~/.bashrc
kubectl completion bash >/etc/bash_completion.d/kubectl
source ~/.bashrc
```

### 彻底重置Kubernetes

```
sudo kubeadm reset -f
sudo rm -rf ~/.kube
sudo rm -rf /etc/kubernetes/manifests/*
netstat -tulnp | grep -E '6443|10259|10257|10250'
```

初始化完成后会输出一段日志

```
.....
.....
[addons] Applied essential addon: CoreDNS
W0306 14:21:21.770192   85065 endpoint.go:57] [endpoint] WARNING: port specified in controlPlaneEndpoint overrides bindPort in the controlplane address
[addons] Applied essential addon: kube-proxy

Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

You can now join any number of control-plane nodes by copying certificate authorities
and service account keys on each node and then running the following as root:

  kubeadm join 10.100.23.60:8443 --token abcdef.0123456789abcdef \
	--discovery-token-ca-cert-hash sha256:c7b6a33517049969157f6a477907ce05df7dff513d74cf5623df8fb08bc0a999 \
	--control-plane 

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 10.100.23.60:8443 --token abcdef.0123456789abcdef \
	--discovery-token-ca-cert-hash sha256:c7b6a33517049969157f6a477907ce05df7dff513d74cf5623df8fb08bc0a999 
	
# 最后这个kubeadm json... 是node节点加入集群的命令，需要在要加入集群的node节点上执行
```

### 5.2添加master第二个节点，master02 

拷贝证书文件和admin.conf配置文件

```
cat << EOF >> /root/cpkey.sh

# ！/bin/bash
CONTROL_PLANE_IPS="master02" # mastername，如果有master03、04、05，就依次写master03 master04空格隔开

for host in \${CONTROL_PLANE_IPS}; do
ssh root@\${host} mkdir -p /etc/kubernetes/pki/etcd
scp /etc/kubernetes/admin.conf root@\${host}:/etc/kubernetes
scp /etc/kubernetes/pki/{ca.*,sa.*,front-proxy-ca.*} root@\${host}:/etc/kubernetes/pki
scp /etc/kubernetes/pki/etcd/ca.* root@\${host}:/etc/kubernetes/pki/etcd
done

EOF

sh /root/cpkey.sh
```

执行加入集群指令

```
  kubeadm join 10.100.23.60:8443 --token abcdef.0123456789abcdef \
	--discovery-token-ca-cert-hash sha256:c7b6a33517049969157f6a477907ce05df7dff513d74cf5623df8fb08bc0a999 \
	--control-plane 
```

3、node节点加入集群

```
kubeadm join k8svip:8443 --token abcdef.0123456789abcdef \
        --discovery-token-ca-cert-hash sha256:d9640fa8e47e7a598f20856a6f2237619b9a6d458626a76e72926fbba4c4407a 
```

## 六 、安装网络插件 Calico

```
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.29.2/manifests/tigera-operator.yaml
```

### 1. 第一种走本地，比较麻烦，没必要

```
# 查看当前最新版本
curl -s https://api.github.com/repos/projectcalico/calico/releases/latest | grep tag_name

## 下载配置文件
wget https://gh.llkk.cc/https://raw.githubusercontent.com/projectcalico/calico/v3.29.2/manifests/calico.yaml 
    
## 查看文件需要的镜像
root@master01:~# grep "image:" calico.yaml | awk -F": " '{print $2}'
docker.io/calico/cni:v3.29.2
docker.io/calico/cni:v3.29.2
docker.io/calico/node:v3.29.2
docker.io/calico/node:v3.29.2
docker.io/calico/kube-controllers:v3.29.2


## 下载
docker pull docker.io/calico/cni:v3.29.2
docker pull docker.io/calico/node:v3.29.2
docker pull docker.io/calico/kube-controllers:v3.29.2

## 登录 打标签 推送
docker login 10.100.20.230 -u admin -p Harbor12345

docker tag docker.io/calico/cni:v3.29.2 10.100.20.230/calico/cni:v3.29.2
docker tag docker.io/calico/node:v3.29.2 10.100.20.230/calico/node:v3.29.2
docker tag docker.io/calico/kube-controllers:v3.29.2 10.100.20.230/calico/kube-controllers:v3.29.2

docker push 10.100.20.230/calico/cni:v3.29.2
docker push 10.100.20.230/calico/node:v3.29.2
docker push 10.100.20.230/calico/kube-controllers:v3.29.2


# 拉
ctr image pull --plain-http --user admin:Harbor12345 10.100.20.230/calico/node:v3.29.2
```

### 6.1优化harbor拉取 

```
vim /etc/containerd/config.toml
# 添加文件夹
 [plugins."io.containerd.grpc.v1.cri".registry]
      config_path = "/etc/containerd/certs.d"


mkdir -p /etc/containerd/certs.d/10.100.20.230
cat > /etc/containerd/certs.d/10.100.20.230/hosts.toml << EOF
server = "http://10.100.20.230"
[host."http://10.100.20.230"]
  capabilities = ["pull", "resolve"]
  auth = { username = "admin", password = "Harbor12345" }
EOF

## 测试
ctr image pull --plain-http 10.100.20.230/calico/node:v3.29.2
```

### 6.2 使用

```
# harbor拉下来 打成docker.io
ctr image tag 10.100.20.230/calico/cni:v3.29.2 docker.io/calico/cni:v3.29.2
ctr image tag 10.100.20.230/calico/kube-controllers:v3.29.2 docker.io/calico/kube-controllers:v3.29.2
ctr image tag 10.100.20.230/calico/node:v3.29.2 docker.io/calico/node:v3.29.2


# 创建calico
 kubectl create -f calico.yaml 
# 实时观看
watch kubectl get pods -n calico-system
```

### 6.3 走鸟站

```

# 展示
grep "image:" calico.yaml | awk -F": " '{print $2}' | sed 's|^|swr.cn-north-4.myhuaweicloud.com/ddn-k8s/|'

# 替换
sed -i 's|image: docker.io/|image: swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/|g' calico.yaml
```


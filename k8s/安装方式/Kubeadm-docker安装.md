## CentOS 7.9 安装 KubeSphere v3.4.0 和 Kubernetes v1.26 实战

```
## dns
echo "nameserver 223.5.5.5" > /etc/resolv.conf
## 部署依赖
yum   update -y
yum install  update conntrack socat curl socat conntrack ebtables ipset ipvsadm  chrony  -y
## 服务器时区
timedatectl set-timezone Asia/Shanghai
## 时间同步
sed -i 's/^pool pool.*/pool cn.pool.ntp.org iburst/g' /etc/chrony.conf && systemctl enable chronyd --now && chronyc sourcestats -v
## 关闭系统防火墙
systemctl stop firewalld && systemctl disable firewalld
## 禁止selinux
sed -i 's/^SELINUX=enforcing/SELINUX=disabled/' /etc/selinux/config
## dislinux禁止
setenforce 0
#为所有节点添加网桥过滤和地址转发功能
cat > /etc/sysctl.d/k8s.conf << EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
vm.swappiness = 0
EOF
# 加载 br_netfilter 模块
modprobe br_netfilter
# 查看是否加载成功
lsmod | grep br_netfilter
# 应用新的配置
sysctl --system
# swap禁用
sudo sed -i '/swap/ s/^/#/' /etc/fstab && sudo swapoff -a


## hosts
cat >> /etc/hosts << EOF
10.0.0.100 master
10.0.0.101 node01
EOF
```

### 安装 Docker、kubeadm、kubelet 和 kubectl

```
# 添加 Docker 的 GPG 密钥和软件源
# step 1: 安装必要的一些系统工具
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
# Step 2: 添加软件源信息
sudo yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
# Step 3
sudo sed -i 's+download.docker.com+mirrors.aliyun.com/docker-ce+' /etc/yum.repos.d/docker-ce.repo
# Step 4: 更新并安装Docker-CE
sudo yum makecache fast
sudo yum -y install docker-ce

mkdir -pv /etc/docker
tee /etc/docker/daemon.json <<-'EOF'
{
  "exec-opts": [
    "native.cgroupdriver=systemd"
  ],
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.top"
  ],
  "live-restore": true,
  "log-driver": "json-file",
  "log-opts": { 
    "max-size": "100m",
    "max-file": "3"
  },
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5
}


EOF
systemctl daemon-reload \
    && systemctl restart docker

#验证加速器是否生效。使用以下命令验证 Docker 是否使用了国内镜像加速器：
docker info
```

#### 2. 安装 cri-docker

由于 1.24 以及更高版本的 Kubernetes 不支持 Docker，所以需要安装 cri-docker：

**注**：但如果使用containerd作为容器运行时，则不需要这部分配置。或者打算使用其他 CRI 插件，可以跳过 cri-docker 的安装部分

```
# 下载 cri-docker

wget gh.tryxd.cn/https://github.com/Mirantis/cri-dockerd/releases/download/v0.3.16/cri-dockerd-0.3.16.amd64.tgz
# 解压并安装
tar xvf cri-dockerd-0.3.16.amd64.tgz
sudo cp cri-dockerd/cri-dockerd /usr/bin/

# 配置 cri-docker 服务
cat > /usr/lib/systemd/system/cri-docker.service <<EOF 
[Unit]
Description=CRI Interface for Docker Application Container Engine
Documentation=https://docs.mirantis.com
After=network-online.target firewalld.service docker.service
Wants=network-online.target
Requires=cri-docker.socket

[Service]
Type=notify
ExecStart=/usr/bin/cri-dockerd --network-plugin=cni --pod-infra-container-image=registry.aliyuncs.com/google_containers/pause:3.7
ExecReload=/bin/kill -s HUP $MAINPID
TimeoutSec=0
RestartSec=2
Restart=always
StartLimitBurst=3
StartLimitInterval=60s
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
TasksMax=infinity
Delegate=yes
KillMode=process

[Install]
WantedBy=multi-user.target
EOF

# 配置 cri-docker socket
cat > /usr/lib/systemd/system/cri-docker.socket <<EOF 
[Unit]
Description=CRI Docker Socket for the API
PartOf=cri-docker.service

[Socket]
ListenStream=%t/cri-dockerd.sock
SocketMode=0660
SocketUser=root
SocketGroup=docker

[Install]
WantedBy=sockets.target
EOF

# 启动 cri-docker
systemctl daemon-reload 
systemctl enable cri-docker --now
```

#### 3. 安装 kubeadm、kubelet 和 kubectl

添加 Kubernetes 的 APT 软件源：

```
cat > /etc/yum.repos.d/kubernetes.repo << EOF
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

yum install -y kubelet kubeadm kubectl

# 启动 kubelet 并设置开机自启
sudo systemctl start kubelet
sudo systemctl enable kubelet
kubeadm version -o short

# 配置 kubectl 环境变量
echo 'export KUBECONFIG=/etc/kubernetes/admin.conf' >> ~/.bashrc
source ~/.bashrc


# -apiserver-advertise-address=192.168.1.100  master节点
# --service-cidr=10.96.0.0/12 Kubernetes 的默认值
# --pod-network-cidr
# Flannel 插件：通常使用 10.244.0.0/16，这是 Flannel 默认的网络范围。
# Calico 插件：通常使用 192.168.0.0/16，但你可以根据需要配置其他范围。
# Weave 插件：通常使用 10.32.0.0/12。


# 生成配置文件
kubeadm config print init-defaults > kubeadm-config.yaml

```

默认的config的配置文件

```
apiVersion: kubeadm.k8s.io/v1beta3
kind: InitConfiguration
localAPIEndpoint:
  advertiseAddress: "10.0.0.100"  # Master 节点的 IP
  bindPort: 6443
nodeRegistration:
  criSocket: "unix:///var/run/cri-dockerd.sock"  # 指定 CRI-Dockerd 的套接字
  name: "master"  # 节点名称
---
apiVersion: kubeadm.k8s.io/v1beta3
kind: ClusterConfiguration
kubernetesVersion: v1.28.2  # 使用适合的 Kubernetes 版本
controlPlaneEndpoint: "10.0.0.100:6443"  # API 服务器的外部访问地址
networking:
  podSubnet: "192.168.0.0/16"  # Calico 推荐的 Pod 网络 CIDR
  serviceSubnet: "10.96.0.0/12"  # 默认 Service 网络 CIDR
  dnsDomain: cluster.local
imageRepository: registry.aliyuncs.com/google_containers  # 阿里云镜像仓库
---
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
mode: ipvs
```

```
kubeadm config images pull --config kubeadm-config.yaml --image-repository registry.aliyuncs.com/google_containers --cri-socket unix:///var/run/cri-dockerd.sock --v=5


kubeadm config images pull  --image-repository registry.aliyuncs.com/google_containers
```

```
kubeadm init --config kubeadm-config.yaml 

--image-repository registry.aliyuncs.com/google_containers --cri-socket unix:///var/run/cri-dockerd.sock




kubeadm init \
--kubernetes-version v1.28.2 \
--image-repository registry.aliyuncs.com/google_containers \
--pod-network-cidr=192.168.0.0/16 \
 --cri-socket unix:///run/containerd/containerd.sock


kubeadm init --config=kubeadm-config.yaml --upload-certs | tee kubeadm-init.log
```

```
rm -rf /etc/kubernetes/manifests/*
kubeadm reset --cri-socket unix:///var/run/cri-dockerd.sock
# 删除 CNI 配置文件
rm -rf /etc/cni/net.d/

# 清理 iptables 规则
iptables -F && iptables -t nat -F && iptables -t mangle -F && iptables -X

# 如果使用 IPVS，清理 IPVS 规则
ipvsadm --clear

```






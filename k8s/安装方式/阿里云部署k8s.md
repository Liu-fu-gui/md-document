如果操作系统环境为CentOS、Red Hat、Rocky Linux、Alma Linux、Fedora等RHEL系列操作系统，使用以下步骤操作。

# 基础环境配置（所有节点）

## 修改主机名与hosts文件

```
[root@k8s-master ~]# hostnamectl set-hostname k8s-master

[root@k8s-master ~]# vim /etc/hosts
192.168.10.10   k8s-master
192.168.10.11   k8s-work1
192.168.10.12   k8s-work2
192.168.10.15   k8s-harbor
```

## 验证mac地址uuid

> 保证各节点mac和uuid唯一，避免克隆虚拟机后uuid一致导致加入集群异常。

```
[root@k8s-master ~]# cat /sys/class/net/ens160/address 
[root@k8s-master ~]# cat /sys/class/dmi/id/product_uuid 
```

## 时间同步

- master节点设置

```
[root@k8s-master ~]# dnf -y install chrony
[root@k8s-master ~]# vim /etc/chrony.conf

# Use public servers from the pool.ntp.org project.
# Please consider joining the pool (http://www.pool.ntp.org/join.html).
server ntp.aliyun.com iburst

# Allow NTP client access from local network.
#allow 192.168.10.0/24

[root@k8s-master ~]# systemctl start chronyd
[root@k8s-master ~]# systemctl enable chronyd
[root@k8s-master ~]# timedatectl set-timezone Asia/Shanghai
[root@k8s-master ~]# chronyc sources
210 Number of sources = 4
MS Name/IP address         Stratum Poll Reach LastRx Last sample               
===============================================================================
^* 202.118.1.81                  1   6   357   100   +124us[ +205us] +/- 7213us
^? 2a01:4f8:120:9224::2          0   6     0     -     +0ns[   +0ns] +/-    0ns
^+ 202.118.1.130                 1   6   316   184    -29us[  +33us] +/- 7479us
^- 119.28.206.193                2   6   316   192    +13ms[  +13ms] +/-   44ms
[root@k8s-master ~]#
```

- node节点配置

```
[root@node1  ~]# yum -y install chrony  
[root@node1  ~]# vim /etc/chrony.conf

# Use public servers from the pool.ntp.org project.
# Please consider joining the pool (http://www.pool.ntp.org/join.html).
server 192.168.10.100

[root@node1  ~]# systemctl start chronyd  
[root@node1  ~]# systemctl enable chronyd  
[root@node1  ~]# chronyc sources
```

## 设置防火墙规则

```
[root@master  ~]# systemctl stop firewalld  
[root@master  ~]# systemctl disable firewalld  
[root@master  ~]# yum -y install iptables-services  
[root@master  ~]# systemctl start iptables  
[root@master  ~]# systemctl enable iptables  
[root@master  ~]# iptables -F 
[root@master  ~]# service iptables save 
```

## 关闭selinux

```
[root@master  ~]# setenforce 0  
[root@master  ~]# sed -i 's/^SELINUX=.*/SELINUX=disabled/' /etc/selinux/config 
```

## 关闭swap分区

```
[root@master  ~]# swapoff -a  
[root@master  ~]# sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab  
```

# 其他配置（所有节点）

## 修改内核相关参数

vm.swappiness = 0 # 最大限度避免使用 swap

net.bridge.bridge-nf-call-ip6tables = 1 # 内核在桥接设备上让IPv6流量经过 Netfilter（iptables）过滤。

net.bridge.bridge-nf-call-iptables = 1 # 内核在桥接设备上让IPv4流量经过 Netfilter（iptables）过滤。

net.ipv4.ip_forward = 1 # 允许 IPv4 数据包从一个网络接口转发到另一个网络接口。

```
[root@master  ~]# cat > /etc/sysctl.d/kubernetes.conf << EOF
vm.swappiness = 0 
net.bridge.bridge-nf-call-ip6tables = 1  
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1 
EOF
[root@master  ~]# sysctl -p /etc/sysctl.d/kubernetes.conf 
```

- centos8会有如下报错

```
vm.swappiness = 0
sysctl: cannot stat /proc/sys/net/bridge/bridge-nf-call-ip6tables: 没有那个文件或目录
sysctl: cannot stat /proc/sys/net/bridge/bridge-nf-call-iptables: 没有那个文件或目录
net.ipv4.ip_forward = 1
```

- 临时解决，重启失效
  modprobe br_netfilter
- 开机加载上面这个模块

```
[root@master  ~]# cat > /etc/rc.sysinit << EOF
#!/bin/bash
for file in /etc/sysconfig/modules/*.modules ; do
[ -x $file ] && $file
done
EOF
[root@master  ~]# cat > /etc/sysconfig/modules/br_netfilter.modules << EOF
modprobe br_netfilter # 通过 br_netfilter，内核能够让网络包在经过桥接设备时被 iptables 规则处理
EOF
[root@master  ~]# chmod 755 /etc/sysconfig/modules/br_netfilter.modules 
[root@master  ~]# lsmod |grep br_netfilter
br_netfilter           24576  0
bridge                290816  1 br_netfilter
```

## kube-proxy开启ipvs的前置条件

```
[root@k8s-master ~]# yum -y install ipset ipvsadm
[root@k8s-master ~]# cat > /etc/sysconfig/modules/ipvs.modules <<EOF 
#!/bin/bash
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack
EOF
[root@k8s-master ~]# chmod 755 /etc/sysconfig/modules/ipvs.modules && bash  
[root@k8s-master ~]# /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack 
ip_vs_sh               16384  0
ip_vs_wrr              16384  0
ip_vs_rr               16384  0
ip_vs                 172032  6 ip_vs_rr,ip_vs_sh,ip_vs_wrr
nf_conntrack          172032  1 ip_vs
nf_defrag_ipv6         20480  2 nf_conntrack,ip_vs
nf_defrag_ipv4         16384  1 nf_conntrack
libcrc32c              16384  4 nf_conntrack,nf_tables,xfs,ip_vs
# 添加开机自动加载模块
[root@k8s-master ~]# echo "/etc/sysconfig/modules/ipvs.modules" >> /etc/rc.local
[root@k8s-master ~]# chmod +x /etc/rc.local
# 启用网桥过滤器模块
[root@k8s-master ~]# echo 1 > /proc/sys/net/bridge/bridge-nf-call-iptables
[root@k8s-master ~]# echo 1 > /proc/sys/net/ipv4/ip_forward
```

- linux kernel 4.19版本已经将nf_conntrack_ipv4 更新为 nf_conntrack

## 升级内核

> 可选，建议4.18及+以上即可

```
载入公钥
[root@master  ~]# rpm --import https://www.elrepo.org/RPM-GPG-KEY-elrepo.org 
升级安装ELRepo
[root@master  ~]# rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-3.el7.elrepo.noarch.rpm 
如果是centos8使用如下命令
[root@master  ~]#yum install https://www.elrepo.org/elrepo-release-8.0-2.el8.elrepo.noarch.rpm 
载入elrepo-kernel元数据
[root@master  ~]# yum --disablerepo=\* --enablerepo=elrepo-kernel repolist 
安装最新版本的kernel
[root@master  ~]# yum --disablerepo=\* --enablerepo=elrepo-kernel install kernel-ml.x86_64 -y 
删除旧版本工具包
[root@master  ~]# yum remove kernel-tools-libs.x86_64 kernel-tools.x86_64 -y 
安装新版本工具包
[root@master  ~]# yum --disablerepo=\* --enablerepo=elrepo-kernel install kernel-ml-tools.x86_64 -y 
查看内核插入顺序
[root@server-1  ~]# awk -F \' '$1=="menuentry " {print i++ " : " $2}'  /etc/grub2.cfg 
设置默认启动
[root@server-1  ~]# grub2-set-default 0 // 0代表当前第一行，也就是5.3版本  
[root@server-1  ~]# grub2-editenv list  
重启验证
```

## 配置阿里云yum源

k8s版本1.28前，使用如下命令配置yum源。

```
[root@k8s-master ~]# cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
```

k8s版本1.28以后，例如安装1.30，则修改对应的版本号即可。

```
[root@k8s-master ~]# cat <<EOF | tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.30/rpm/
enabled=1
gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.30/rpm/repodata/repomd.xml.key
EOF
```

## 安装kubeadm、kubectl、kubelet

```
yum install -y kubelet kubeadm kubectl
systemctl enable kubelet
systemctl start kubelet
```

kubelet 运行在集群所有节点上，用于启动Pod和容器等对象的工具
kubeadm 用于初始化集群，启动集群的命令工具
kubectl 用于和集群通信的命令行，通过kubectl可以部署和管理应用，查看各种资源，创建、删除和更新各种组件

- 默认安装最新版，也可以指定老版本安装

```
yum list kubeadm --showduplicates | sort -r
yum install -y kubelet-1.24.13 kubeadm-1.24.13 kubectl-1.24.13
```

# kubeadm部署集群(1.20之前)

> 以下操作在master节点执行

## 配置文件创建集群

- 获取默认的初始化参数文件

```
# kubeadm config print init-defaults > kubeadm-conf.yaml
```

- 配置kubeadm-conf.yaml初始化文件

```
apiVersion: kubeadm.k8s.io/v1beta2
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
  advertiseAddress: 192.168.10.10 # master节点ip地址，如果 Master 有多个interface，建议明确指定，
  bindPort: 6443
nodeRegistration:
  criSocket: /var/run/dockershim.sock
  name: k8s-master # master节点主机名
  taints:
  - effect: NoSchedule
    key: node-role.kubernetes.io/master
---
apiServer:
  timeoutForControlPlane: 4m0s
apiVersion: kubeadm.k8s.io/v1beta2
certificatesDir: /etc/kubernetes/pki
clusterName: kubernetes
controllerManager: {}
dns:
  type: CoreDNS
etcd:
  local:
    dataDir: /var/lib/etcd
imageRepository: k8s.gcr.io
kind: ClusterConfiguration
kubernetesVersion: v1.19.16 # k8s安装版本
imageRepository: "registry.aliyuncs.com/google_containers" # 将其指定为阿里云镜像地址
networking:
  dnsDomain: cluster.local
  podSubnet: "10.244.0.0/16" #Kubernetes 支持多种网络方案，而且不同网络方案对--pod-network-cidr 有自己的要求，这里设置为 10.244.0.0/16 是因为我们将使用flannel 网络方案，必须设置成这个 CIDR。
  serviceSubnet: 10.96.0.0/12
scheduler: {}
---
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
featureGates:
  SupportIPVSProxyMode: true
mode: ipvs
```

- 指定配置文件创建k8s集群

```
kubeadm init --config=kubeadm-conf.yaml
```

## 命令行创建k8s集群

```
[root@master ~]# kubeadm init --apiserver-advertise-address=192.168.10.100 --image-repository registry.aliyuncs.com/google_containers --kubernetes-version v1.19.15 --pod-network-cidr=10.244.0.0/16
```

- –apiserver-advertise-address
  指明用 Master 的哪个 interface 与 Cluster 的其他节点通信。如果 Master 有多个interface，建议明确指定，如果不指定，kubeadm 会自动选择有默认网关的interface。
- –pod-network-cidr
  指定 Pod 网络的范围。Kubernetes 支持多种网络方案，而且不同网络方案对–pod-network-cidr 有自己的要求，这里设置为 10.244.0.0/16 是因为我们将使用flannel 网络方案，必须设置成这个 CIDR。
- –image-repository
  Kubenetes默认Registries地址是 k8s.gcr.io，在国内并不能访问gcr.io，在1.13版本中我们可以增加–image-repository参数，默认值是k8s.gcr.io，将其指定为阿里云镜像地址：registry.aliyuncs.com/google_containers。
- –kubernetes-version=v1.19.15
  关闭版本探测，因为它的默认值是stable-1，会导致从https://dl.k8s.io/release/stable-1.txt下载最新的版本号

执行完毕后控制台打印以下信息：

```
Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
https://kubernetes.io/docs/concepts/cluster-administration/addons/

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.10.10:6443 --token abcdef.0123456789abcdef \
--discovery-token-ca-cert-hash sha256:1f0931588ac578637042e96ebede6c086a36105ceb4cdb65399b6f315650b996 
```

## 根据提示初始化kubectl

```
[root@k8s-master k8s-install]# mkdir -p $HOME/.kube
[root@k8s-master k8s-install]# cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
[root@k8s-master k8s-install]# chown $(id -u):$(id -g) $HOME/.kube/config
```

## 启用 kubectl 命令自动补全功能

```
[root@k8s-master k8s-install]# yum -y install bash-completion
[root@k8s-master k8s-install]# echo "source <(kubectl completion bash)" >> ~/.bash_profile 
[root@k8s-master k8s-install]# source ~/.bash_profile 
```

## 测试kubectl

```
[root@k8s-master k8s-install]# kubectl get node
NAME         STATUS     ROLES                  AGE   VERSION
k8s-master   NotReady   control-plane,master   46s   v1.19.16
```

# kubeadm部署集群(1.20之后)

## 变化说明

从1.20开始，开启ipvs配置字段发生了变化，访问官方查看最新版本ipvs开启的正确配置,通过https://github.com/kubernetes/kubernetes/blob/master/pkg/proxy/ipvs/README.md可以看到官方说明

从1.22开始，推荐使用containerd作为容器运行时。

## init配置文件

```
# cat kubeadm-conf.yaml 
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
  advertiseAddress: 192.168.10.10  #修改为控制节点IP（VIP）
  bindPort: 6443
nodeRegistration:
  criSocket: unix:///run/containerd/containerd.sock  #使用containerd为容器运行时
	# criSocket: /var/run/dockershim.sock  #使用docker为容器运行时
  imagePullPolicy: IfNotPresent
  name: k8s-master     #修改为控制节点主机名
  taints: null
---
apiServer:
  timeoutForControlPlane: 4m0s
apiVersion: kubeadm.k8s.io/v1beta3
certificatesDir: /etc/kubernetes/pki
clusterName: kubernetes
controllerManager: {}
dns: {}
etcd:
  local:
    dataDir: /var/lib/etcd
imageRepository: registry.aliyuncs.com/google_containers  #修改为阿里镜像地址
kind: ClusterConfiguration
kubernetesVersion: 1.24.13  #版本
networking:
  dnsDomain: cluster.local
  podSubnet: 10.244.0.0/16   #指定Pod网段
  serviceSubnet: 10.96.0.0/12  #指定Service网段
scheduler: {}
---
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind:  KubeProxyConfiguration
mode: ipvs
---
apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
cgroupDriver: systemd
```

- 如果初始化时出现以下报错，先停止master节点的kubelet

```
error execution phase preflight: [preflight] Some fatal errors occurred:
        [ERROR Port-10250]: Port 10250 is in use
```

```
[root@k8s-master k8s-install]# systemctl stop kubelet
```

# 初始化失败解决

如果因为配置文件异常导致集群初始化失败，可执行如下命令

```
# kubeadm reset 
# rm -rf $HOME/.kube/config 
```

# 启用基于flannel的Pod网络

项目地址：https://github.com/flannel-io/flannel

## 下载配置文件

```
[root@master ~]# wget https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml 
```

## 启用flannel

```
[root@master ~]# kubectl apply -f kube-flannel.yml  
# 如果机器有多块物理网卡，需要指定网卡名称
containers:
- name: kube-flannel
    - args:
    - --ip-masq
    - --kube-subnet-mgr
    - --iface=bond0 # 添加这行
```

## 验证操作

```
[root@k8s-master k8s-install]# kubectl get pods -A
NAMESPACE      NAME                                 READY   STATUS    RESTARTS   AGE
kube-flannel   kube-flannel-ds-5tjkb                1/1     Running   0          9m1s
kube-system    coredns-6d56c8448f-vnrqf             1/1     Running   0          19m
kube-system    coredns-6d56c8448f-x9q75             1/1     Running   0          19m
kube-system    etcd-k8s-master                      1/1     Running   0          20m
kube-system    kube-apiserver-k8s-master            1/1     Running   0          20m
kube-system    kube-controller-manager-k8s-master   1/1     Running   0          20m
kube-system    kube-proxy-9df97                     1/1     Running   0          19m
kube-system    kube-scheduler-k8s-master            1/1     Running   0          20m
```

# 其他node节点加入集群

## 将节点加入到集群

```
[root@k8s-work1 ~]# kubeadm join 192.168.10.10:6443 --token abcdef.0123456789abcdef --discovery-token-ca-cert-hash sha256:1f0931588ac578637042e96ebede6c086a36105ceb4cdb65399b6f315650b996 
```

## 查看集群信息

```
[root@k8s-master ~]# kubectl get node
NAME         STATUS   ROLES                  AGE     VERSION
k8s-master   Ready    control-plane,master   5m26s   v1.24.13
k8s-work1    Ready    <none>                 3m39s   v1.24.13
k8s-work2    Ready    <none>                 3m48s   v1.24.13
[root@k8s-master ~]# kubectl get pod -A
NAMESPACE      NAME                                 READY   STATUS    RESTARTS   AGE
kube-flannel   kube-flannel-ds-22hrr                1/1     Running   2          17m
kube-flannel   kube-flannel-ds-5tjkb                1/1     Running   2          36m
kube-flannel   kube-flannel-ds-kmtnk                1/1     Running   0          84s
kube-system    coredns-6d56c8448f-vnrqf             1/1     Running   3          47m
kube-system    coredns-6d56c8448f-x9q75             1/1     Running   2          47m
kube-system    etcd-k8s-master                      1/1     Running   2          47m
kube-system    kube-apiserver-k8s-master            1/1     Running   2          47m
kube-system    kube-controller-manager-k8s-master   1/1     Running   2          47m
kube-system    kube-proxy-6wmsl                     1/1     Running   2          17m
kube-system    kube-proxy-9df97                     1/1     Running   2          47m
kube-system    kube-proxy-fkkm6                     1/1     Running   0          84s
kube-system    kube-scheduler-k8s-master            1/1     Running   2          47m
```
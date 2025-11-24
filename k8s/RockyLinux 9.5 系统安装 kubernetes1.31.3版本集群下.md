kubernetes 1.31.3 版本集群安装完整篇幅太大了，无法在一篇文章里分享，今天接着分享下部分，上一篇可点击下面链接回看。

《 [RockyLinux 9.5 系统安装 kubernetes1.31.3版本集群上](https://mp.weixin.qq.com/s?__biz=MzkyNDU2ODAwOQ==&mid=2247485026&idx=1&sn=abb5dd69777730bbbf7bef0c8b560a39&scene=21#wechat_redirect) 》

《 [最新 Rocky Linux 9.5 系统的安装](https://mp.weixin.qq.com/s?__biz=MzkyNDU2ODAwOQ==&mid=2247484959&idx=1&sn=2c59c94608893db7279097f40f704339&scene=21#wechat_redirect) 》



**2 安装 Docker**

***\*2.1 安装必备依赖软件工具包。\****

```
执行下面的安装指令进行安装，选择一种安装方式即可。
dnf install -y wget jq psmisc vim net-tools telnet tar curl git sshpass rsyslog yum-utils device-mapper-persistent-data lvm2或yum install -y wget jq psmisc vim net-tools telnet tar curl git sshpass rsyslog yum-utils device-mapper-persistent-data lvm2
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hDiaLuu6BFLKdQRkAkbYA2GibFgA5gvXRtcx9gdg5GkspeWWCwic0mfbIA/640?wx_fmt=png&from=appmsg)

***\*Docker 镜像源\****

```
阿里云镜像仓库：https://cr.console.aliyun.com 清华镜像仓库： https://mirrors.tuna.tsinghua.edu.cn/docker-ce/华为镜像仓库： https://mirrors.huaweicloud.com/docker-ce/中科大镜像地址：http://mirrors.ustc.edu.cn/中科大github地址：https://github.com/ustclug/mirrorrequestAzure中国镜像地址：http://mirror.azure.cn/Azure中国github地址：https://github.com/Azure/container-service-for-azure-chinaDockerHub镜像仓库: https://hub.docker.com/ google镜像仓库： https://console.cloud.google.com/gcr/images/google-containers/GLOBAL （如果你本地可以翻墙的话是可以连上去的 ）coreos镜像仓库： https://quay.io/repository/ RedHat镜像仓库： https://access.redhat.com/containers
```

***\*选择阿里云镜像仓库\****

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9h4ZwcZAcqWeTJDeOXFyGmCou2Wo0vHOGFu83qBKw1R9SE3to0ibyicp7A/640?wx_fmt=png&from=appmsg)

***\*2.2 下载安装包\****

```
下载 docker-27.3.1 版本，我是使用二进制安装包安装的。
wget https://mirrors.aliyun.com/docker-ce/linux/static/stable/x86_64/docker-27.3.1.tgz
tar -xvf docker-27.3.1.tgzcp -rf docker/* /us/bin/
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hSEr4TL3Ych47howIuxsicdoQCMTGibo7oYzibn8CKscWmUxjXicvBFibiahw/640?wx_fmt=png&from=appmsg)

**也可以通过命令行直接进行安装，所有主机节点都配置安装源。**

```
dnf config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hxexE2vk5Hl5qquARibfGlh9bsKztlDg0HZNWyl9dsE7J1RjzZSc6PWw/640?wx_fmt=png&from=appmsg)

所有主机节点都执行安装 docker-ce containerd

```
dnf install docker-ce containerd -y
```

**2.3 创建 docker.service**



```
cat > /etc/systemd/system/docker.service <<EOF[Unit]Description=Docker Application Container EngineDocumentation=https://docs.docker.comAfter=network-online.target firewalld.service containerd.serviceWants=network-online.targetRequires=docker.socket containerd.service[Service]Type=notifyExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sockExecReload=/bin/kill -s HUP $MAINPIDTimeoutSec=0RestartSec=2Restart=alwaysStartLimitBurst=3StartLimitInterval=60sLimitNOFILE=infinityLimitNPROC=infinityLimitCORE=infinityTasksMax=infinityDelegate=yesKillMode=processOOMScoreAdjust=-500[Install]WantedBy=multi-user.targetEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hhEjNnaHZ5lxdGp2pLsglfIVjyL5hd7Pjv3zTE20eLxXSCf2biaP7nXg/640?wx_fmt=png&from=appmsg)

**2.4 创建 docker.socket**



```
cat > /etc/systemd/system/docker.socket <<EOF[Unit]Description=Docker Socket for the API[Socket]ListenStream=/var/run/docker.sockSocketMode=0660SocketUser=rootSocketGroup=docker[Install]WantedBy=sockets.targetEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hqLqFVjdicmmiaxth2cwiapU7iaGyNVP0UuVHbW7KgDwBHvicUdpOLhrwYOw/640?wx_fmt=png&from=appmsg)

**2.5 配置加速器 daemon.json**

```
mkdir -p /etc/docker
cat > /etc/docker/daemon.json << EOF{    "builder": {    "gc": {      "defaultKeepStorage": "20GB",      "enabled": true    }  },  "exec-opts": ["native.cgroupdriver=systemd"],  "registry-mirrors": [    "https://docker.hpcloud.cloud",    "https://docker.unsee.tech",    "http://mirrors.ustc.edu.cn",    "https://docker.chenby.cn",    "http://mirror.azure.cn",    "https://dockerpull.org",    "https://hub.rat.dev",    "https://docker.1panel.live",    "https://docker.m.daocloud.io",    "https://registry.dockermirror.com",    "https://docker.aityp.com/",    "https://docker.anyhub.us.kg",    "https://dockerhub.icu",    "https://docker.awsl9527.cn"  ],  "insecure-registries": ["https://harbor.flyfish.com"],  "max-concurrent-downloads": 10,  "log-driver": "json-file",  "log-level": "warn",  "log-opts": {    "max-size": "10m",    "max-file": "3"    },  "data-root": "/var/lib/docker"} EOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hkcjyIgm3NrnjicX7HEHaeiamPxkw0WOwM1NwaALcKF2icibeNHvSxURsicg/640?wx_fmt=png&from=appmsg)



```
groupadd dockersystemctl enable --now docker.socketsystemctl enable --now docker.servicesystemctl restart dockerdocker version
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hBsXxwTicibCfLDHbfiaSb1zlrWvjMbtnkL5C4heIdSkwicKwmN3sXHo8ibw/640?wx_fmt=png&from=appmsg)

**2.6 创建 containerd.service**



```
cat > /etc/systemd/system/containerd.service <<EOF[Unit]Description=containerd container runtimeDocumentation=https://containerd.ioAfter=network.target local-fs.target[Service]ExecStartPre=-/sbin/modprobe overlayExecStart=/usr/bin/containerdType=notifyDelegate=yesKillMode=processRestart=alwaysRestartSec=5LimitNPROC=infinityLimitCORE=infinityLimitNOFILE=1048576TasksMax=infinityOOMScoreAdjust=-999[Install]WantedBy=multi-user.targetEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9h5BlQm5GZUVib1xJhicd2AmoNxfSQOPWAJW97jibrV9jY0heKEiaCJUrOsQ/640?wx_fmt=png&from=appmsg)



```
systemctl enable --now containerd.service   # 配置开机启动containerd --version                        # 查看版本
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hT3vDhwWaVafGhdJXKuYDibDe14j1Dvup4kiaTKz2JKNibjMDSsL6V66jg/640?wx_fmt=png&from=appmsg)

**
**

***\*3 安装\** \**cri-dockerd\****

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9htRU78TY5OR6bdAa9ADSwNZmpe1safYTZopaia5krHHurJdApSwSD8pQ/640?wx_fmt=png&from=appmsg)

```
3.1 下载 v0.3.15 版本
```



```
下载 v0.3.15 版本https://github.com/Mirantis/cri-dockerd/releases/download/v0.3.15/cri-dockerd-0.3.15.amd64.tgz
tar -xvf cri-dockerd-0.3.15.amd64.tgzcp cri-dockerd/cri-dockerd /usr/binchmod +x /usr/bin/cri-dockerd
```

**3.2 写入启动配置文件**



```
cat > /usr/lib/systemd/system/cri-docker.service <<EOF[Unit]Description=CRI Interface for Docker Application Container EngineDocumentation=https://docs.mirantis.comAfter=network-online.target firewalld.service docker.serviceWants=network-online.targetRequires=cri-docker.socket[Service]Type=notifyExecStart=/usr/bin/cri-dockerd --network-plugin=cni --pod-infra-container-image=registry.aliyuncs.com/google_containers/pause:3.9ExecReload=/bin/kill -s HUP $MAINPIDTimeoutSec=0RestartSec=2Restart=alwaysStartLimitBurst=3StartLimitInterval=60sLimitNOFILE=infinityLimitNPROC=infinityLimitCORE=infinityTasksMax=infinityDelegate=yesKillMode=process[Install]WantedBy=multi-user.targetEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hntTjh7iahicOHibf36Mxzd06pTMgCJXTbZhiajxCEyl5rWISpTgX5BBZ6A/640?wx_fmt=png&from=appmsg)

**3.3 创建 cri-docker.socket**



```
cat > /usr/lib/systemd/system/cri-docker.socket <<EOF[Unit]Description=CRI Docker Socket for the API	Partof=cri-docker.service [Socket]ListenStream=%t/cri-dockerd.sockSocketMode=0660SocketUser=rootSocketGroup=docker [Install]WantedBy=sockets.targetEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hbTYxnPAuew1qPiclr9FSLvtP2zBd1iaeh3GfpO6PTvKica3QPmK7ibxAxg/640?wx_fmt=png&from=appmsg)

**3.4 启动 cri-docker**



```
systemctl daemon-reloadsystemctl enable cri-docker --now
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hcsqwn26cZhUyDdiaiaV6Y5dY4picRvDsfxHwSnjibTparPnGV2BMXbRMXw/640?wx_fmt=png&from=appmsg)



**4 安装 kubernetes v1.31.3**

***\*4.1 添加\** \**k8s 的\** \**YUM\** \**软件源\****



```
配置阿里云软件源
cat <<EOF | tee /etc/yum.repos.d/kubernetes.repo[kubernetes]name=Kubernetesbaseurl=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.31/rpmenabled=1gpgcheck=1gpgkey=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.31/rpm/repodata/repomd.xml.keyEOF
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hCR53vaJHAobwQBwNbR9CL1ibWnY32NwqZLnseZlicHpibxvDrnBcoHicMQ/640?wx_fmt=png&from=appmsg)

***\*4.2 查看所有的可用版本\****



```
dnf list kubeadm.x86_64 --showduplicates | sort -r或yum list kubelet.x86_64 --showduplicates | sort -r | grep 1.31
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hl0wGrrNIcFHshw78905veTDicV3FpJ4bOqGEJxUXDeJArrQlRb7wlsg/640?wx_fmt=png&from=appmsg)

***\*4.3 所有节点都\*******\*执行安装\** \**kubeadm\**\**、\**\**kubelet\** \**和\** \**kubectl\****



```
dnf install kubeadm-1.31* kubelet-1.31* kubectl-1.31* -y    #指定要安装的版本或yum install -y kubeadm kubelet kubectl                      #默认是最新的版本
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hI0icAulmvmQibPYtqCQgn2CmiaFNcXFrbnvE5IR7QJjPoccNDfE2oDxWQ/640?wx_fmt=png&from=appmsg)

**4.4 为了实现** **docker** **使用** **cgroupdriver** **与** **kubelet** **使用的** **cgroup** **的一致性，建议修改如下文件的内容。**



```
vim /etc/sysconfig/kubeletKUBELET_EXTRA_ARGS="--cgroup-driver=systemd"
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hr8V3C2nJd7c2frPSHsRZnHNaWDO9vY4eGIEXF3EU4DmMOFkcdibrHbA/640?wx_fmt=png&from=appmsg)



**4.5 设置** **kubelet** **为开机自启动，由于没有生成配置文件，集群初始化后自动启动。**



```
systemctl enable --now kubelet
kubeadm version
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hnfPub0ZYBJQXEYB13Br8btoDgugWiafEeFXbxUh6J5CWJTeqOARBhwQ/640?wx_fmt=png&from=appmsg)

**4.6 下载镜像**



```
下载相关的镜像kubeadm config images pull --image-repository registry.aliyuncs.com/google_containers --cri-socket=unix:///var/run/cri-dockerd.sock
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hRGuMtdmMW2WKhaHicRZXWoP4fMYXsiaeZ8ibCF9icDxvvkyzwGDSR0FoSg/640?wx_fmt=png&from=appmsg)

**4.7 集群初始化**



```
编辑个 shell 初始化的脚本，只需要在一个主节点上执行初始化即可。vim kubeinit.shkubeadm init --kubernetes-version=v1.31.3 --pod-network-cidr=10.244.0.0/16 --service-cidr=10.96.0.0/12 --apiserver-advertise-address=192.168.59.128 --image-repository registry.aliyuncs.com/google_containers --cri-socket=unix:///var/run/cri-dockerd.sock
--apiserver-advertise-address 	#集群通告地址--image-repository 		       #由于默认拉取镜像地址k8s.gcr.io国内无法访问，这里指定阿里云镜像仓库地址--kubernetes-version 	           #k8s版本，与上面安装的版本一致--service-cidr 			       #集群内部虚拟机网络，Pod统一访问入口--pod-network-cidr 		       #Pod网络，与下面部署的CNI网络组件yaml中保持一致
```

**![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hTlgcRXRUSxsICcdU9Pw76Peic6jmJVzTKbFV283ceyUJ7sAHAy70MyQ/640?wx_fmt=png&from=appmsg)**



```
# . ./kubeinit.sh[init] Using Kubernetes version: v1.31.3[preflight] Running pre-flight checks[preflight] Pulling images required for setting up a Kubernetes cluster[preflight] This might take a minute or two, depending on the speed of your internet connection[preflight] You can also perform this action beforehand using 'kubeadm config images pull'W1209 22:48:27.857278   23174 checks.go:846] detected that the sandbox image "registry.aliyuncs.com/google_containers/pause:3.9" of the           container runtime is inconsistent with that used by kubeadm.It is recommended to use "registry.aliyuncs.com/google_containers/pause:3.10          " as the CRI sandbox image.[certs] Using certificateDir folder "/etc/kubernetes/pki"[certs] Generating "ca" certificate and key[certs] Generating "apiserver" certificate and key[certs] apiserver serving cert is signed for DNS names [k8smaster1 kubernetes kubernetes.default kubernetes.default.svc kubernetes.defau          lt.svc.cluster.local] and IPs [10.96.0.1 192.168.59.128][certs] Generating "apiserver-kubelet-client" certificate and key[certs] Generating "front-proxy-ca" certificate and key[certs] Generating "front-proxy-client" certificate and key[certs] Generating "etcd/ca" certificate and key[certs] Generating "etcd/server" certificate and key[certs] etcd/server serving cert is signed for DNS names [k8smaster1 localhost] and IPs [192.168.59.128 127.0.0.1 ::1][certs] Generating "etcd/peer" certificate and key[certs] etcd/peer serving cert is signed for DNS names [k8smaster1 localhost] and IPs [192.168.59.128 127.0.0.1 ::1][certs] Generating "etcd/healthcheck-client" certificate and key[certs] Generating "apiserver-etcd-client" certificate and key[certs] Generating "sa" key and public key[kubeconfig] Using kubeconfig folder "/etc/kubernetes"[kubeconfig] Writing "admin.conf" kubeconfig file[kubeconfig] Writing "super-admin.conf" kubeconfig file[kubeconfig] Writing "kubelet.conf" kubeconfig file[kubeconfig] Writing "controller-manager.conf" kubeconfig file[kubeconfig] Writing "scheduler.conf" kubeconfig file[etcd] Creating static Pod manifest for local etcd in "/etc/kubernetes/manifests"[control-plane] Using manifest folder "/etc/kubernetes/manifests"[control-plane] Creating static Pod manifest for "kube-apiserver"[control-plane] Creating static Pod manifest for "kube-controller-manager"[control-plane] Creating static Pod manifest for "kube-scheduler"[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"[kubelet-start] Starting the kubelet[wait-control-plane] Waiting for the kubelet to boot up the control plane as static Pods from directory "/etc/kubernetes/manifests"[kubelet-check] Waiting for a healthy kubelet at http://127.0.0.1:10248/healthz. This can take up to 4m0s[kubelet-check] The kubelet is healthy after 1.001796103s[api-check] Waiting for a healthy API server. This can take up to 4m0s[api-check] The API server is healthy after 15.503446811s[upload-config] Storing the configuration used in ConfigMap "kubeadm-config" in the "kube-system" Namespace[kubelet] Creating a ConfigMap "kubelet-config" in namespace kube-system with the configuration for the kubelets in the cluster[upload-certs] Skipping phase. Please see --upload-certs[mark-control-plane] Marking the node k8smaster1 as control-plane by adding the labels: [node-role.kubernetes.io/control-plane node.kubernetes.io/exclude-from-external-load-balancers][mark-control-plane] Marking the node k8smaster1 as control-plane by adding the taints [node-role.kubernetes.io/control-plane:NoSchedule][bootstrap-token] Using token: g1gdjt.sbuuj62g21lpojwr[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to get nodes[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials[bootstrap-token] Configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token[bootstrap-token] Configured RBAC rules to allow certificate rotation for all node client certificates in the cluster[bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace[kubelet-finalize] Updating "/etc/kubernetes/kubelet.conf" to point to a rotatable kubelet client certificate and key[addons] Applied essential addon: CoreDNS[addons] Applied essential addon: kube-proxy
Your Kubernetes control-plane has initialized successfully!
To start using your cluster, you need to run the following as a regular user:
  mkdir -p $HOME/.kube  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config  sudo chown $(id -u):$(id -g) $HOME/.kube/config
Alternatively, if you are the root user, you can run:
  export KUBECONFIG=/etc/kubernetes/admin.conf
You should now deploy a pod network to the cluster.Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:  https://kubernetes.io/docs/concepts/cluster-administration/addons/
Then you can join any number of worker nodes by running the following on each as root:
kubeadm join 192.168.59.128:6443 --token g1gdjt.sbuuj62g21lpojwr \        --discovery-token-ca-cert-hash sha256:b6bf705ae83f8122caeceeabeb7c413990cc677c5cd3e55789c317faaf450466[root@K8smaster1 home]#
```

**Your Kubernetes control-plane has initialized successfully!**

**
**

**4.8 工作节点初始化**



```
编辑个 shell 初始化工作节点的脚本，每个工作节点都需要执行。vim kubeinit-join.sh
kubeadm join 192.168.59.128:6443 --token g1gdjt.sbuuj62g21lpojwr \        --discovery-token-ca-cert-hash sha256:b6bf705ae83f8122caeceeabeb7c413990cc677c5cd3e55789c317faaf450466 --cri-socket=unix:///var/run/cri-dockerd.sock
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9h8MNauZBtj1kmicEANyd3u0KQLZjOrBEpJibTFWOOOydNBNs0mdPoynSQ/640?wx_fmt=png&from=appmsg)

```
kubectl get node
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9h69YxBoGXqX0Zmk0vu8EgJpGqyTrbibG7ic3Lu0eibQuM3icwwvOibIxMJTw/640?wx_fmt=png&from=appmsg)

此时 get node 状态显示是 NotReady，因为**目前还没有安装网络插件。**

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzveYufYBWkXibjbfDm4OC4N9hiaTeRzJoPq6uiaptXpTaJD85j4pzJCcJL1aZEp0YRJUgDXhOu4icP6k3g/640?wx_fmt=png&from=appmsg)

***\*
\**

***\*5 安装 calico 网络插件\****

**5.1 官网介绍**

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L8Vh9GG6Aicn1dsZcnTjpxyHO6nDrhMicEu49ibBDGQ5Xf4ok2Vicxlh1gUA/640?wx_fmt=png&from=appmsg)

如上图绿框标注，进行安装操作即可。

***\*5.2 Install Calico\****

- 

```
1、Install the Tigera Calico operator and custom resource definitions.kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.29.1/manifests/tigera-operator.yaml
2、Install Calico by creating the necessary custom resourcekubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.29.1/manifests/custom-resources.yaml
```

**在本地会下载一个 \**calico.yaml\** 文件，如下图所示，增加下面4行内容，这需按自己实际配置修改即可。**

- 

```
- name: CALICO_IPV4POOL_CIDR  value: "10.244.0.0/16"- name: IP_AUTODETECTION_METHOD  value: "interface=ens160"
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L8iaknflPy5xAZ0Y1goia2D4Fdwbq61A1FrOWXQqRvVhaibarUsicC5iasXJg/640?wx_fmt=png&from=appmsg)

修改完成后保存退出，就可以执行安装了。

- 

```
kubectl apply -f calico.yaml
```

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L8sSibNqVzDD8oY8f07KaoIEibutq0mhQKPlKLGTiccib2UGECFEVZELdtxw/640?wx_fmt=png&from=appmsg)

查看 node，还是 NotReady 状态。查看 pod ，还是 Init:ImagePullBackOff 状态，说明网络插件还没安装好，还在下载安装镜像中，需要等待一会时间。

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L82Q2d9ZRHe09LPKnaJjvmexiboe49m3dGA1ecCULVEAgATJpXPMz98sg/640?wx_fmt=png&from=appmsg)

大概过了七~八分钟左右，查看一下状态。

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L8ibZzqTLtyHMy2k6icjampJS3osH4rtib7ratL4DMHBVOrC2uSAnLk2S4A/640?wx_fmt=png&from=appmsg)

node 已经都 Ready 状态，说明网络插件安装完成，各节点网络都已畅通准备就绪。

![img](https://mmbiz.qpic.cn/mmbiz_png/P0CeVMiavzvfMKjKWK4ibC8ggdWHjF49L8Elrz0eqlOPwSIZWjCiaPZcEeCR0Avw7oI2jUEg8k80INibgU4kicHMxKw/640?wx_fmt=png&from=appmsg)



此时，kubernetes 1.31.3 版本集群也算是成功安装完成。



到此，今天分享的主题 RockyLinux 9.5 系统安装 kubernetes1.31.3版本集群下 就结束了。
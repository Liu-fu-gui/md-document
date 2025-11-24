# etcd高可用

参考链接：

https://blog.csdn.net/MssGuo/article/details/134495748

## 一，初始化

```shell
#准备3台centos7服务器并做以下基础环境配置
#联网同步时间
yum install chrony -y && systemctl enable --now chronyd
#安装常见的依赖
yum install vim lsof net-tools zip unzip tree wget curl bash-completion pciutils gcc make lrzsz tcpdump bind-utils -y
#关闭防火墙
systemctl stop firewalld.service && systemctl disable firewalld.service
#将本地时间
#同步给硬件时间
hwclock -w	
#创建数据目录
mkdir /opt/etcd{1..3}
#设置主机名
hostnamectl  set-hostname etcd-1
hostnamectl  set-hostname etcd-2
hostnamectl  set-hostname etcd-3
#写入/etc/hosts文件
cat >> /etc/hosts <<EOF
192.168.2.41 etcd-1
192.168.2.42 etcd-2
192.168.2.43 etcd-3
EOF				                
```

## 二，下载版本

```
#下载etcd安装包
ETCD_VER=v3.5.10
# choose either URL
GOOGLE_URL=https://storage.googleapis.com/etcd
GITHUB_URL=https://github.com/etcd-io/etcd/releases/download
DOWNLOAD_URL=${GOOGLE_URL}
curl -L ${DOWNLOAD_URL}/${ETCD_VER}/etcd-${ETCD_VER}-linux-amd64.tar.gz -o  ./etcd-${ETCD_VER}-linux-amd64.tar.gz
tar xf etcd-${ETCD_VER}-linux-amd64.tar.gz
cd etcd-${ETCD_VER}-linux-amd64/
mv etcd* /usr/local/bin/ 
```

## 三，创建启动文件

```
#etcd-1节点
cat>/usr/lib/systemd/system/etcd.service<<'EOF'
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
ExecStart=/usr/local/bin/etcd \
  --name=etcd-1 \
  --data-dir=/opt/etcd \
  --listen-client-urls=http://192.168.2.41:2379 \
  --listen-peer-urls=http://192.168.2.41:2380 \
  --advertise-client-urls=http://192.168.2.41:2379 \
  --initial-advertise-peer-urls=http://192.168.2.41:2380 \
  --initial-cluster=etcd-1=http://192.168.2.41:2380,etcd-2=http://192.168.2.42:2380,etcd-3=http://192.168.2.43:2380 \
  --initial-cluster-token=etcd-cluster \
  --initial-cluster-state=new \
  --logger=zap
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF
```

```
#etcd-2节点
cat>/usr/lib/systemd/system/etcd.service<<'EOF'
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
ExecStart=/usr/local/bin/etcd \
  --name=etcd-2 \
  --data-dir=/opt/etcd \
  --listen-client-urls=http://192.168.2.42:2379 \
  --listen-peer-urls=http://192.168.2.42:2380 \
  --advertise-client-urls=http://192.168.2.42:2379 \
  --initial-advertise-peer-urls=http://192.168.2.42:2380 \
  --initial-cluster=etcd-1=http://192.168.2.41:2380,etcd-2=http://192.168.2.42:2380,etcd-3=http://192.168.2.43:2380 \
  --initial-cluster-token=etcd-cluster \
  --initial-cluster-state=new \
  --logger=zap
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF
```

```
#etcd-2节点
cat>/usr/lib/systemd/system/etcd.service<<'EOF'
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
ExecStart=/usr/local/bin/etcd \
  --name=etcd-3 \
  --data-dir=/opt/etcd \
  --listen-client-urls=http://192.168.2.43:2379 \
  --listen-peer-urls=http://192.168.2.43:2380 \
  --advertise-client-urls=http://192.168.2.43:2379 \
  --initial-advertise-peer-urls=http://192.168.2.43:2380 \
  --initial-cluster=etcd-1=http://192.168.2.41:2380,etcd-2=http://192.168.2.42:2380,etcd-3=http://192.168.2.43:2380 \
  --initial-cluster-token=etcd-cluster \
  --initial-cluster-state=new \
  --logger=zap
Restart=on-failure
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF
```

## 四，启动组成集群

```
# 三台主机尽量同时进行
systemctl daemon-reload
systemctl enable --now etcd
systemctl enable --now etcd
systemctl enable --now etcd
```

## 五，验证集群状态

```
etcdctl --endpoints="http://192.168.2.10:2379,http://192.168.2.20:2379,http://192.168.2.11:2379" endpoint status  --write-out=table

etcdctl --endpoints="http://aigc-k8s-id-master001:2379,http://aigc-k8s-id-master002:2379,http://aigc-k8s-id-master003:2379" endpoint status  --write-out=table
```

https://github.com/etcd-io/etcd/releases/download/v3.5.14/etcd-v3.5.14-linux-amd64.tar.gz

```
# 重启master节点之前，需要先剔除节点，再重启
etcdctl --endpoints="http://10.62.150.134:2379,http://10.62.150.194:2379,http://10.62.150.130:2379" member list   #查询出节点对应ID
# 1be00f02bdcd7ed9, started, etcd-3, http://10.62.150.130:2380, http://10.62.150.130:2379, false
# a7c51757a00dffbd, started, etcd-2, http://10.62.150.194:2380, http://10.62.150.194:2379, false
# dda5343b1fdd729f, started, etcd-1, http://10.62.150.134:2380, http://10.62.150.134:2379, false

# 移除节点
etcdctl --endpoints="http://10.62.150.134:2379,http://10.62.150.194:2379,http://10.62.150.130:2379" member remove <MemberID>
```


**一、下载** **RockyLinux9** **镜像**

```
# 官方下载地址
https://rockylinux.org/download
# 阿里云镜像下载地址
https://mirrors.aliyun.com/rockylinux/9/isos/x86_64/?spm=a2c6h.25603864.0.0.29696621VzJej5
```

**二、环境初始化**

```
# 网卡配置
# cat /etc/NetworkManager/system-connections/ens160.nmconnection
[ipv4]
method=manual
address1=192.168.66.12/24,192.168.66.200
dns=114.114.114.114;8.8.8.8
# cat /etc/NetworkManager/system-connections/ens192.nmconnection
[connection]
autoconnect=false
# 调用 nmcli 重启设备和连接配置
nmcli d d ens192
nmcli d r ens160 
nmcli c r ens160


# Rocky 系统软件源更换
sed -e 's|^mirrorlist=|#mirrorlist=|g' \
    -e
's|^#baseurl=http://dl.rockylinux.org/$contentdir|baseurl=https://mirrors.aliyun
.com/rockylinux|g' \
    -i.bak \
   /etc/yum.repos.d/[Rr]ocky*.repo
    
dnf makecache


# 防火墙修改 firewalld 为 iptables
systemctl stop firewalld
systemctl disable firewalld
yum -y install iptables-services
systemctl start iptables
iptables -F
systemctl enable iptables



# 禁用 Selinux
setenforce 0
sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config
grubby --update-kernel ALL --args selinux=0
# 查看是否禁用，grubby --info DEFAULT
# 回滚内核层禁用操作，grubby --update-kernel ALL --remove-args selinux


# 设置时区
timedatectl set-timezone Asia/Shanghai
```

保存系统快照为 Justinstall，保存系统快照为 Justinstall，保存系统快照为 Justinstall

**三、安装** **Docker** **环境**

```
# 加载 bridge
yum install -y epel-release
yum install -y bridge-utils
modprobe br_netfilter
echo 'br_netfilter' >> /etc/modules-load.d/bridge.conf
echo 'net.bridge.bridge-nf-call-iptables=1' >> /etc/sysctl.conf
echo 'net.bridge.bridge-nf-call-ip6tables=1' >> /etc/sysctl.conf
echo 'net.ipv4.ip_forward=1' >> /etc/sysctl.conf
sysctl -p

# 添加 docker-ce yum 源
# 中科大(ustc)
sudo dnf config-manager --add-repo https://mirrors.ustc.edu.cn/docker￾ce/linux/centos/docker-ce.repo
cd /etc/yum.repos.d
# 切换中科大源
sed -e 's|download.docker.com|mirrors.ustc.edu.cn/docker-ce|g' docker-ce.repo
# 安装 docker-ce
yum -y install docker-ce
# 配置 daemon.
cat > /etc/docker/daemon.json <<EOF
{
  "data-root": "/data/docker",
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "100"
 },
  "insecure-registries": ["harbor.xinxainghf.com"],
  "registry-mirrors": ["https://kfp63jaj.mirror.aliyuncs.com"]
}
EOF
mkdir -p /etc/systemd/system/docker.service.d
# 重启docker服务
systemctl daemon-reload && systemctl restart docker && systemctl enable docker

```


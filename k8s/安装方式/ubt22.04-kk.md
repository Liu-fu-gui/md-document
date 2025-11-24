````



# 更新
apt update && apt-get -y install ipset ipvsadm
# 关闭swap分区
swapoff -a
sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab


基础包安装

```
apt install -y vim tree wget bash-completion lsof lrzsz net-tools sysstat iotop iftop htop unzip nmap telnet bc psmisc bind9-utils netcat expect ntpdate conntrack socat curl ebtables ipset ipvsadm chrony
```

# ssh速度优化
sed -i '/^GSSAPIAuthentication/s@^@#@g' /etc/ssh/sshd_config
cat >>/etc/ssh/sshd_config<<EOF
UseDNS no
GSSAPIAuthentication no
EOF
systemctl restart sshd
egrep '^(GSSAPIAuthentication|UseDNS)' /etc/ssh/sshd_config


# 停止 ufw 服务
sudo ufw disable

# 检查 ufw 状态（确保已关闭）
sudo ufw status

# 停止 AppArmor 服务
sudo systemctl stop apparmor

# 禁用 AppArmor（重启后不启动）
sudo systemctl disable apparmor

# 命令行颜色
 echo export PS1="'[\[\e[34;1m\]\u@\[\e[0m\]\[\e[32;1m\]\h\[\e[0m\]\[\e[31;1m\] \w\[\e[0m\]]\$ '">> /etc/profile 
source /etc/profile

# 时间同步
sed -i 's/^pool pool.*/pool cn.pool.ntp.org iburst/g' /etc/chrony/chrony.conf
timedatectl set-timezone Asia/Shanghai
systemctl enable chrony --now
chronyc sourcestats -v
````

## 常用kk命令

```
## 删除集群
./kk delete cluster -f config-sample.yaml
## 创建集群
./kk create cluster -f config-sample.yaml
## 根据需求创建文件
./kk create config --name ksp-v131 -f ksp-v131.yaml --with-kubernetes v1.31.2
## 安装ks k8s
./kk create cluster --with-kubernetes v1.22.12 --with-kubesphere v3.4.1
# 默认存储
./kk create cluster -f config-sample.yaml  --with-local-storage
```

## 补全kubectl

```
yum install  -y bash-completion
echo 'source <(kubectl completion bash)' >>~/.bashrc
kubectl completion bash >/etc/bash_completion.d/kubectl
source ~/.bashrc
```

# 安装kk

```
mkdir ~/kubekey
cd ~/kubekey/

# 选择中文区下载(访问 GitHub 受限时使用)
export KKZONE=cn
curl -sfL https://get-kk.kubesphere.io | sh -

# 查看 KubeKey 支持的 Kubernetes 版本列表 
./kk version --show-supported-k8s
```

# 创建配置文件

```
./kk create config --name ksp-v131 -f ksp-v131.yaml --with-kubernetes v1.31.0
```


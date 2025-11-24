# 模板机制作



借鉴

https://www.yuque.com/buxiandongfeng/wuu5zr/5fc78f3897886794e04b54f8bc4a0734

**1) 关闭防火墙和selinux**

```
关闭防火墙
systemctl stop firewalld
永久关闭防火墙
systemctl disable firewalld
setenforce 0
禁用selinux
 sed -i 's#SELINUX=enforcing#SELINUX=disabled#g' /etc/selinux/config
```

**2) yum源**

```
#修改base源 为阿里云
curl -o /etc/yum.repos.d/CentOS-Base.repo https://mirrors.aliyun.com/repo/Centos-7.repo
#增加epel源
curl -o /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-7.repo
```

**3) 安装常用工具**

```
# 这就是我们一次下载多个常用的工具，就像给windows系统安装多个软件一样
yum install -y vim tree  wget bash-completion bash-completion-extras lsof lrzsz net-tools sysstat iotop iftophtop unzip nc nmap telnet bc psmisc httpd-tools bind-utils nethogs expect ntpdate
mkdir -p /app/tools  /app/code
```

**4) 优化ssh连接速度**

```
 sed -i '/^GSSAPIAuthentication/s@^@#@g' /etc/ssh/sshd_config

cat >>/etc/ssh/sshd_config<<EOF
UseDNS no
GSSAPIAuthentication no
EOF
systemctl restart sshd
#检查
 egrep '^(GSSAPIAuthentication|UseDNS)' /etc/ssh/sshd_config
```

**5) 时间同步**

```
## 时间同步

yum install -y chrony  
timedatectl set-timezone Asia/Shanghai
## 时间同步
sed -i 's/^pool pool.*/pool cn.pool.ntp.org iburst/g' /etc/chrony.conf && systemctl enable chronyd --now && chronyc sourcestats -v
```

**6) 命令行颜色**

```
echo export PS1="'[\[\e[34;1m\]\u@\[\e[0m\]\[\e[32;1m\]\h\[\e[0m\]\[\e[31;1m\] \w\[\e[0m\]]\$ '">> /etc/profile 
source /etc/profile
```

**7) 书写修改ip和主机名的脚本**





入门demo

```
mkdir -p /server/scripts
cat > /server/scripts/change.sh << 'EOF'
#!/bin/bash
#author: 作者
#desc:  描述
#version: v6.0 final
#模板机ip地址
ip=`hostname -I |awk '{print $1}'|sed 's#.*\.##g'`
#新的ip
ip_new=`echo $2 |sed 's#^.*\.##g'`
#新的主机名
hostname=$1
#修改ip,可以查看sed的替换
sed -i "s#10.0.0.$ip#10.0.0.$ip_new#g" /etc/sysconfig/network-scripts/ifcfg-eth0
# sed -i "s#172.16.1.$ip#172.16.1.$ip_new#g" /etc/sysconfig/network-scripts/ifcfg-eth1
#重启网卡
systemctl restart network
#修改主机名
hostnamectl set-hostname $hostname
EOF
```



```

#!/bin/bash

# 日志文件
LOG_FILE="/root/change1.log"
echo "脚本开始执行: $(date)" > "$LOG_FILE"

# 检查参数
if [ -z "$1" ] || [ -z "$2" ]; then
  echo "用法: $0 <新主机名> <新IP地址>" | tee -a "$LOG_FILE"
  exit 1
fi

# 获取网络接口名
interface=$(ip -o link show | awk -F': ' '{print $2}' | grep -v lo | head -n 1)
if [ -z "$interface" ]; then
  echo "错误: 无法找到网络接口" | tee -a "$LOG_FILE"
  exit 1
fi
echo "检测到的网络接口: $interface" | tee -a "$LOG_FILE"

# 获取当前 IP 地址和前缀
ip_full=$(hostname -I | awk '{print $1}')
if [ -z "$ip_full" ]; then
  echo "错误: 无法获取当前 IP 地址" | tee -a "$LOG_FILE"
  exit 1
fi
ip_prefix=$(echo "$ip_full" | sed 's#\.[0-9]*$##g')
ip=$(echo "$ip_full" | sed 's#.*\.##g')
echo "当前 IP: $ip_full, 前缀: $ip_prefix, 最后部分: $ip" | tee -a "$LOG_FILE"

# 获取新的 IP 地址
ip_new=$(echo "$2" | sed 's#^.*\.##g')
echo "新 IP 参数: $2, 提取的最后部分: $ip_new" | tee -a "$LOG_FILE"

# 新的主机名
hostname=$1

# 备份网络配置文件
config_file="/etc/sysconfig/network-scripts/ifcfg-$interface"
if [ ! -f "$config_file" ]; then
  echo "错误: 网络配置文件 $config_file 不存在" | tee -a "$LOG_FILE"
  exit 1
fi
cp "$config_file" "$config_file.bak"
echo "已备份网络配置文件: $config_file.bak" | tee -a "$LOG_FILE"

# 修改 IP 地址
sed -i "s#$ip_prefix.$ip#$ip_prefix.$ip_new#g" "$config_file"
echo "已修改配置文件 $config_file，替换 $ip_prefix.$ip 为 $ip_prefix.$ip_new" | tee -a "$LOG_FILE"

# 确保使用 network-scripts 管理
echo "NM_CONTROLLED=no" >> "$config_file"

# 重启网络（使用 ifdown/ifup，避免 service network 的潜在问题）
echo "重启网络接口 $interface..." | tee -a "$LOG_FILE"
ifdown "$interface" && ifup "$interface"
if [ $? -ne 0 ]; then
  echo "错误: 网络重启失败，请手动检查" | tee -a "$LOG_FILE"
  exit 1
fi
echo "网络重启完成" | tee -a "$LOG_FILE"

# 修改主机名
hostnamectl set-hostname "$hostname"
if [ $? -ne 0 ]; then
  echo "错误: 主机名修改失败" | tee -a "$LOG_FILE"
  exit 1
fi

# 输出结果
echo "主机名已修改为: $hostname" | tee -a "$LOG_FILE"
echo "IP 地址已修改为: $ip_prefix.$ip_new" | tee -a "$LOG_FILE"
echo "脚本执行完成: $(date)" | tee -a "$LOG_FILE"

# 提示用户查看日志
echo "请查看日志文件 $LOG_FILE 以获取完整执行信息"
```


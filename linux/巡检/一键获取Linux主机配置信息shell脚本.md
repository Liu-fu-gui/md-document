## 脚本

```
  1cat > /tmp/get_os_info.sh <<"EOF"
  2#!/bin/bash
  3
  4export LANG=en_US.UTF-8
  5
  6
  7# 如果 cat /proc/1/cgroup | grep docker | wc -l  大于0 或 systemd-detect-virt 返回 docker，则为 docker容器，
  8# 如果 virt-what 返回 kvm或vmware或hyperv或xen、xen-hvm、lxc 或 systemd-detect-virt 返回 vmware、kvm  则为虚拟机
  9# 如果 cat /sys/class/dmi/id/product_name 返回 Bochs 、OpenStack Nova则为虚拟机 若返回Alibaba Cloud ECS，则代表阿里云主机
 10# 如果 systemd-detect-virt 返回 none 则表示物理机
 11
 12# 判断当前主机类型
 13if [ "$(cat /proc/1/cgroup | grep -c docker)" -gt 0 ] || systemd-detect-virt | grep -q docker; then
 14    host_type="Docker容器"
 15elif [[ -f /sys/class/dmi/id/product_name && $(cat /sys/class/dmi/id/product_name) == "Alibaba Cloud ECS" ]]; then
 16    host_type="阿里云主机"
 17elif [[ $(virt-what) =~ (kvm|vmware|hyperv|xen|xen-hvm|lxc) ]] || [[ $(systemd-detect-virt) =~ (vmware|kvm) ]]; then
 18    host_type="虚拟机"
 19elif [[ $(cat /sys/class/dmi/id/product_name) =~ (Bochs|OpenStack Nova) ]]; then
 20    host_type="虚拟机"
 21else
 22    host_type="物理机"
 23fi
 24
 25echo "- **当前主机类型**: $host_type"
 26
 27# 获取并显示系统制造商、产品名称和版本
 28echo "  - 当前主机系统硬件信息:"
 29dmidecode -t 1 | awk -F: '/Manufacturer/{printf "    - 制造商: %s\n", $2}
 30                            /Product Name/{printf "    - 产品名称: %s\n", $2}'
 31
 32
 33# 获取操作系统信息
 34os_info=$(cat /etc/os-release | grep 'PRETTY_NAME' | cut -d '"' -f 2)
 35# 如果操作系统是 CentOS Linux 7 (Core)，查询 /etc/redhat-release
 36if [[ "$os_info" == "CentOS Linux 7 (Core)" ]]; then
 37    os_info=$(cat /etc/redhat-release)
 38fi
 39echo "- **操作系统信息**: $os_info"
 40
 41
 42# 显示当前内核版本
 43kernel_version=$(uname -r)
 44echo "- **内核版本**: $kernel_version"
 45
 46
 47echo "- **硬件配置**："
 48# 获取CPU信息
 49cpu_model=$(lscpu | grep "Model name" | awk -F: '{print $2}' | xargs)
 50cpu_cores=$(lscpu | grep "^CPU(s):" | awk '{print $2}')
 51
 52# 获取每核的线程数
 53threads_per_core=$(lscpu | grep -i "Thread(s) per core" | awk '{print $4}')
 54
 55# 如果每核线程数大于1，则说明开启了超线程
 56if [[ $threads_per_core -gt 1 ]]; then
 57    HyperThreading="超线程已开启 (每核线程数: $threads_per_core)"
 58else
 59    HyperThreading="超线程未开启 (每核线程数: $threads_per_core)"
 60fi
 61
 62
 63echo "  - **CPU**: $cpu_model ，**共 $cpu_cores 核** ，$HyperThreading "
 64
 65
 66
 67
 68
 69# 获取内存信息
 70memory=$(free -h | awk '/^Mem:/{print $2}')
 71echo "  - **内存**: $memory"
 72
 73
 74# 虚拟化平台（如 VMware、KVM、Hyper-V 等）可能会将虚拟硬盘映射为虚拟的 HDD 类型，即使宿主机的物理硬盘是 SSD。
 75# 如果 TRAN 列显示为 sata，则硬盘是 SATA 接口。
 76# 如果 TRAN 列显示为 pcie 或 nvme，则硬盘是 NVMe 接口。
 77# 如果 TRAN 列显示为 fc，则表示该硬盘使用的是 Fibre Channel 接口。
 78# 获取指定磁盘信息
 79echo "  - **磁盘**: $disk_info"
 80# lsblk -d -o NAME,ROTA,TRAN,SIZE | grep -E '^(sd|vd)' | awk '{print "    -", $1,$2,$3,$4, ($2 == 1 ? "HDD机械硬盘" : "SSD固态硬盘")}'
 81lsblk -d -o NAME,ROTA,TRAN,SIZE | grep -E '^(sd|vd)' | awk -v host_type="$host_type" '{ 
 82    if (host_type == "物理机") {
 83        print "    -", $1, $2, $3, $4, ($2 == 0 ? "SSD固态硬盘" : "HDD机械硬盘")
 84    } else {
 85        print "    -", $1, $2, $3, $4, ($2 == 0 ? "SSD固态硬盘" : "疑似HDD机械硬盘")
 86    }
 87}'
 88
 89
 90echo '    ```'
 91
 92if command -v lshw &> /dev/null; then
 93  lshw -short | grep disk | grep -v "cdrom"  | awk '{$1=""; print "    "$0}'
 94fi
 95
 96lsscsi  | grep -v "cd/dvd" | grep "disk" | awk '{$1=""; print "    "$0}'
 97
 98echo '    ```'
 99
100
101# 显示文件系统类型
102echo "- **文件系统**:"
103# df -Th 2>/dev/null | grep -v "loop"  | grep -v "tmpfs" | awk '{print "  -", $1, "->", $2}'
104echo '    ```'
105df -Th 2>/dev/null | grep -v "loop"  | grep -v "tmpfs" | grep -v "iso"  | grep -v "/var/lib/docker/overlay2" | grep -v "/boot"   | awk '{print "    "$0}'
106echo '    ```'
107
108
109
110
111# 1. 对闪存（SSD）等存储介质，优先使用noop或none调度算法
112# 2. 对IO压力比较重且非SSD存储设备，且功能比较单一的场景，例如数据库服务器，使用deadline或mq-deadline调度算法
113
114# for dev in $(lsblk -dno NAME | grep -E '^(sd|vd)'); do echo "$dev: $(cat /sys/block/$dev/queue/scheduler)"; done
115# echo noop | sudo tee /sys/block/sda/queue/scheduler
116# for dev in $(lsblk -dno NAME  | grep -E '^(sd|vd)'); do echo "echo noop | sudo tee /sys/block/$dev/queue/scheduler"; done
117
118
119# 获取I/O调度器信息
120echo "- **I/O 调度器**:"
121for dev in $(lsblk -dno NAME | grep -E '^(sd|vd)'); do
122    # 提取中括号中的当前调度器
123    scheduler=$(cat /sys/block/$dev/queue/scheduler 2>/dev/null | grep -o '\[[^]]*\]' | tr -d '[]')
124    echo "  - $dev: $scheduler"
125done
126
127
128# 查看时区
129timezone=$(timedatectl | grep "Time zone:" | awk '{print $3,$4,$5}')
130echo "- **当前主机时区信息**：$(echo $timezone)"
131
132
133# 查看IP地址
134ip_add=`hostname -I`
135hostname1=`hostname`
136echo "- **当前主机IP地址**：$ip_add"
137echo "- **当前主机IP地址**：$hostname1"
138
139
140
141
142
143EOF
144
145
146sleep 1
147
148sh /tmp/get_os_info.sh
```

## 结果

可直接粘贴到markdown的文档中进行解析即可：

```
 1[root@alldb ~]# sh /tmp/get_os_info.sh
 2- **当前主机类型**: 虚拟机
 3  - 当前主机系统硬件信息:
 4    - 制造商:  Bochs
 5    - 产品名称:  Bochs
 6- **操作系统信息**: CentOS Linux release 7.9.2009 (Core)
 7- **内核版本**: 3.10.0-1160.95.1.el7.x86_64
 8- **硬件配置**：
 9  - **CPU**: Intel(R) Xeon(R) Gold 6342 CPU @ 2.80GHz ，**共 32 核**
10  - **内存**: 62G
11  - **磁盘**: 
12    - vda : HDD机械硬盘
13    - vdb : HDD机械硬盘
14    \```
15     /dev/vda disk 4294GB Virtual I/O device
16     /dev/vdb disk 2147MB Virtual I/O device
17    \```
18- **文件系统**:
19    \```
20    Filesystem              Type        Size  Used Avail Use% Mounted on
21    /dev/mapper/centos-root xfs         3.9T  2.3T  1.7T  58% /
22    root@192.164.7.163:/bk   fuse.sshfs  2.0T  796G  1.2T  40% /163_bk
23    \```
24- **I/O 调度器**:
25  - vda: mq-deadline
26  - vdb: mq-deadline
27- **当前主机时区信息**：Asia/Shanghai (CST, +0800)
28- **当前主机IP地址**：192.16.17.162 192.168.188.162
29- **当前主机IP地址**：alldb  
30
```



**解析后的效果：**



![img](https://mmbiz.qpic.cn/mmbiz_png/4gVG5XyyUw3a3zzpYnNPQ8foeNPQkW4Np7RoYsYGribpFYFJZBJKPw7gIyrDQ7Lg1XlzvSDwV4uqR9AN1rdzcFg/640?wx_fmt=png&from=appmsg)
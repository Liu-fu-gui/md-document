## 1.1 初始化role
创建相关的目录存放对应的yml清单文件。

```
mkdir -p keepalived-install-role/{files,tasks,vars,templates}
```


## 1.2 设置环境变量
在keepalived-install-role/vars/main.yml文件中设定相关的环境变量，使用时只需要修改值即可。


```
# cat keepalived-install-role/vars/main.yml

$ cat keepalived-install-role/vars/main.yml
---
#Keepalived版本
keepalived_version: 2.2.7
#安装目录
install_path: /etc/keepalived
#VIP地址：
virtual_ipaddress: 192.168.2.88/24
#虚拟路由ID号（主备必须一致）,每个实例唯一
virtual_router_id: 216
```
## 1.3 下载文件
从官网下载源码包，放入keepalived-install-role/files/目录下。

```
cd keepalived-install-role/files/
wget https://keepalived.org/software/keepalived-2.2.7.tar.gz
md5sum keepalived-2.2.7.tar.gz

5f310b66a043a1fb31acf65af15e95bc  keepalived-2.2.7.tar.gz
```
## 1.4 编写tasks
在keepalived-install-role/tasks目录下创建对应的tasks任务文件。

在main.yml中导入两个tasks文件

```
$  cat keepalived-install/tasks/main.yml
---
- include_tasks: host-init.yml
- include_tasks: install-keepalived.yml
```
定义主机初始化任务

```
$ cat keepalived-install/tasks/host-init.yml 
---
#配置主机环境，如安装依赖包等

- name: "安装依赖包(Debian系统)"
  when: ansible_os_family == "Debian"
  apt:
    name:
      - build-essential   #编译所需的基本工具
      - libssl-dev        #可能需要的 SSL 库
      - libnl-3-dev       #libnl 库
      - libnl-genl-3-dev  #libnl-genl 库
    state: present
  
- name: "安装依赖包(CentOS系统)"
  when: ansible_distribution == "CentOS"
  yum:
    name:
      - gcc
      - gcc-c++
      - make
      - openssl
      - openssl-devel
      - iproute
      - libnl
      - libnl-devel
      - libnfnetlink-devel
    state: present

#设置环境变量
- name: "收集目标主机IP清单"
  set_fact:
    target_hosts: "{{ ansible_play_hosts | map('extract', hostvars, 'ansible_host') | list }}"
- debug:
    msg: "{{ target_hosts }}"

- name: "生成环境变量（集群列表）"
  set_fact:
    nacos_servers: "{{ target_hosts | map('regex_replace', '^(.*)$', '\\1:8848') | join(',') }}"

#设定MASTER（第一台host）
- name: "设定MASTER（第一台host）"
  set_fact:
    master_host:  "{{ target_hosts[0] }}"
- debug:
    msg: "MASTER节点为：{{ master_host }}"

#查找 internal IP 所在网卡并设置为环境变量
- name: "查找 internal IP 所在网卡"
  shell: "ip a s to {{ ansible_host }} | awk -F ': ' 'NF > 1 {print $2}'"
  register: ip_address_result
- name: "设置变量"
  set_fact:
    network_interface: "{{ ip_address_result.stdout | regex_replace('@.*', '') }}"
- debug:
    msg: "IP对应的网卡为：{{ network_interface }}"
```

```
$ tree keepalived-install/
keepalived-install/
├── files
│   ├── keepalived-2.2.7.tar.gz
│   └── keepalived.service
├── tasks
│   ├── host-init.yml
│   ├── install-keepalived.yml
│   └── main.yml
├── templates
│   └── keepalived.conf.j2
└── vars
    └── main.yml

4 directories, 7 files
```
![20241212105259](https://liu-fu-gui.github.io/myimg/halo/20241212105259.png)
定义安装install任务 在keepalived-install/tasks/install-keepalived.yml文件中定义部署keepalived服务的相关任务。

```
$ cat keepalived-install/tasks/install-keepalived.yml
---
#源码编译安装keepalived
#若已经安装，则跳过安装
- name: "检查服务是否安装 <keepalived>"
  command: which keepalived
  register: keepalived_check
  ignore_errors: true

- name: "判断服务安装则进行安装操作"
  when: keepalived_check.rc != 0
  block:
  - name: "创建安装目录 <{{ install_path }}>"
    file:
      name: "{{ install_path }}"
      state: directory
  - name: "分发安装包"
    unarchive: 
      src: "keepalived-{{ keepalived_version }}.tar.gz"
      dest: "{{ install_path }}"
  - name: "编译并安装Keepalived"
    shell:
      cmd: "./configure --prefix=/usr/local/keepalived"
      chdir: "{{ install_path }}/keepalived-{{ keepalived_version }}"
  - name: "制作并安装Keepalive"
    shell:
      cmd: "make && make install"
      chdir: "{{ install_path }}/keepalived-{{ keepalived_version }}"

- name: "分发配置文件"
  template: 
    src: keepalived.conf.j2
    dest: /etc/keepalived/keepalived.conf

- name: "启动服务<keepalived>"
  systemd: 
    name: keepalived
    state: started
    enabled: true
    daemon_reload: yes
```
## 1.5 定制配置文件模板
基于Jinja2模板文件，根据变量自动配置MASTER和BACKUP节点的配置参数，默认将第一个host作为MASTER，其余的均为BACKUP。

```
$ cat keepalived-install/templates/keepalived.conf.j2
! Configuration File for keepalived

global_defs {
   smtp_server localhost
   smtp_connect_timeout 30
   router_id {{ ansible_host }}
   vrrp_skip_check_adv_addr
   vrrp_strict
   vrrp_garp_interval 0
   vrrp_gna_interval 0
}

# VRRP 实例定义
vrrp_instance VI_1 {
{% if ansible_host == master_host %}
    state MASTER
    priority 100
{% else %}
    state BACKUP
    priority 80
{% endif %}

    nopreempt
    interface {{ network_interface }}
    virtual_router_id {{ virtual_router_id }}

    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }

    virtual_ipaddress {
        {{ virtual_ipaddress }}
    }
    track_script {
        check_nginx
    }
}

#健康检查
vrrp_script check_nginx {
    script "/etc/keepalived/check_nginx.sh"
    interval 5
    weight -60
    fall 2
    rise 2
}
```
## 3 调用role部署任务
在编写上述role完成后，就可以调用roles进行实际的部署应用了。

定义hosts 在hosts中定义目标主机有哪些，即安装keepalived的主机。

```
$ cat /etc/ansible/hosts
[lidabai]
192.168.2.61
192.168.2.62
192.168.2.63

[lidabai:vars]
ansible_ssh_user=root
ansible_ssh_pass=1
ansible_ssh_port=22
#ansible_python_interpreter=/usr/bin/python2
```
调用role
```
$ cat run-keepalived.yml
---
- hosts: lidabai
  become: yes
  roles:
  - keepalived-install
```
运行
```
$ ansible-plabook run-keepalived.yml
```

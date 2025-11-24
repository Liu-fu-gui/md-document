# ansible脚本
<!-- more -->
## 检查 s-course-server 和 s-gateway 的句柄数，并保存日志

## hosts

```
[root@ecs-564e ~/cron/ansible_jubing]$ cat hosts 
[servers]
192.168.xx.xx
```
## playbook.yml 第一版随便优化

```
---
- name: 检查 s-course-server 和 s-gateway 的句柄数，并保存日志
  hosts: servers  # 替换为目标主机的组名或 IP 地址
  gather_facts: yes
  tasks:
    - name: 获取 s-course-server 进程的 PID
      shell: "pgrep -f 's-course-server'"
      register: course_pid
      failed_when: false
      changed_when: false

    - name: 输出 s-course-server PID
      debug:
        msg: "s-course-server PID: {{ course_pid.stdout }}"

    - name: 获取 s-course-server 进程的句柄数
      shell: "lsof -p {{ course_pid.stdout }} | wc -l"
      register: course_handles
      when: course_pid.stdout != ""
      failed_when: false
      changed_when: false

    - name: 确保 s-course-server 日志目录存在
      file:
        path: "/home/mydata/s-course-server"
        state: directory
        mode: '0755'

    - name: 查看 s-course-server 的 lsof 输出
      shell: "lsof -p {{ course_pid.stdout }}"
      register: lsof_output_course
      failed_when: false
      changed_when: false

    - name: 输出 s-course-server lsof 输出
      debug:
        msg: "{{ lsof_output_course.stdout }}"

    - name: 保存 s-course-server 进程的 lsof 输出到日志文件
      shell: "lsof -p {{ course_pid.stdout }} > /home/mydata/s-course-server/s-course-server-{{ ansible_date_time.iso8601 | regex_replace('[:\\-]', '') }}.log"
      when: course_pid.stdout != ""
      failed_when: false
      changed_when: false

    - name: 获取 s-gateway 进程的 PID
      shell: "pgrep -f 's-gateway'"
      register: gateway_pid
      failed_when: false
      changed_when: false

    - name: 输出 s-gateway PID
      debug:
        msg: "s-gateway PID: {{ gateway_pid.stdout }}"

    - name: 获取 s-gateway 进程的句柄数
      shell: "lsof -p {{ gateway_pid.stdout }} | wc -l"
      register: gateway_handles
      when: gateway_pid.stdout != ""
      failed_when: false
      changed_when: false

    - name: 确保 s-gateway 日志目录存在
      file:
        path: "/home/mydata/s-gateway"
        state: directory
        mode: '0755'

    - name: 查看 s-gateway 的 lsof 输出
      shell: "lsof -p {{ gateway_pid.stdout }}"
      register: lsof_output_gateway
      failed_when: false
      changed_when: false

    - name: 输出 s-gateway lsof 输出
      debug:
        msg: "{{ lsof_output_gateway.stdout }}"

    - name: 保存 s-gateway 进程的 lsof 输出到日志文件
      shell: "lsof -p {{ gateway_pid.stdout }} > /home/mydata/s-gateway/s-gateway-{{ ansible_date_time.iso8601 | regex_replace('[:\\-]', '') }}.log"
      when: gateway_pid.stdout != ""
      failed_when: false
      changed_when: false

    - name: 检查并发送钉钉通知 - s-course-server
      when: course_handles.stdout | int >= 300
      uri:
        url: "https://oapi.dingtalk.com/robot/send?access_token=xx"
        method: POST
        headers:
          Content-Type: "application/json"
        body: |
          {
            "msgtype": "text",
            "text": {
              "content": "警告：s-course-server 进程的句柄数已达到 {{ course_handles.stdout }}，请注意！机器 IP 地址：{{ inventory_hostname }},日志目录：/home/mydata/s-course-server"
            }
          }
        body_format: json
        status_code: 200

    - name: 检查并发送钉钉通知 - s-gateway
      when: gateway_handles.stdout | int >= 300
      uri:
        url: "https://oapi.dingtalk.com/robot/send?access_token=xxx"
        method: POST
        headers:
          Content-Type: "application/json"
        body: |
          {
            "msgtype": "text",
            "text": {
              "content": "警告：s-gateway 进程的句柄数已达到 {{ gateway_handles.stdout }}，请注意！机器 IP 地址：{{ inventory_hostname }},日志目录：/home/mydata/s-gateway"
            }
          }
        body_format: json
        status_code: 200
```
## 执行命令

```
ansible-playbook -i hosts playbook.yml
```


## 22 端口ping

```
---
- name: 用ping测试连通性
  hosts: servers
  gather_facts: no
  tasks:
    - name: Ping test
      ping:
```

## add hosts

```
[root@ecs-564e ~/cron/ansible_allsshd]$ cat allsshd.yml 
---
- name: 管理 hosts.allow 文件
  hosts: all
  become: yes
  gather_facts: no  # 禁用收集事实
  vars:
    action: "add"
  tasks:
    - name: "使用 shell 命令添加 ALL: ALL"
      shell: echo 'ALL:ALL' >> /etc/hosts.allow
      when: action == "add"

```
## edr脚本

```
[root@ecs-564e ~/ansbile]$ cat edr.yaml 
- name: 下载并执行 agent_setup.sh 并检查进程
  hosts: all
  gather_facts: no
  tasks:
    - name: 检查主机的 22 端口
      ansible.builtin.wait_for:
        port: 22
        timeout: 5
      register: port_check
      ignore_errors: yes

    - name: 下载并执行 agent_setup.sh
      ansible.builtin.shell: >
        wget --no-check-certificate http://192.168.1.245:10571/download/linux/KFDX2023/192.168.1.245_10571/1/agent_setup.sh -O agent_setup.sh && chmod +x agent_setup.sh && ./agent_setup.sh
      when: port_check is succeeded
      register: setup_result
      ignore_errors: yes

    - name: 输出下载和执行的日志
      debug:
        var: setup_result.stdout

    - name: 检查 edr 进程
      ansible.builtin.shell: ps -ef | grep edr
      register: edr_process
      ignore_errors: yes

    - name: 输出 edr 进程状态
      debug:
        var: edr_process.stdout

[root@ecs-564e ~/ansbile]$ 
```

## 日志

```
[root@ecs-564e ~/ansbile]$ cat rizhi.yaml 
- name: 修改 rsyslog 配置并重启服务
  hosts: all
  gather_facts: no
  tasks:
    - name: 检查主机的 22 端口
      ansible.builtin.wait_for:
        port: 22
        timeout: 5
      register: port_check
      ignore_errors: yes

    - name: 检查 /etc/rsyslog.conf 是否包含指定内容
      ansible.builtin.command: cat /etc/rsyslog.conf
      register: check_result
      ignore_errors: yes

    - name: 修改 /etc/rsyslog.conf 配置文件
      ansible.builtin.shell: |
        echo '*.*    @192.168.1.105' >> /etc/rsyslog.conf
      when: port_check is succeeded and "'*.*    @192.168.1.105' not in check_result.stdout"
      register: modify_result
      ignore_errors: yes

    - name: 重启 rsyslog 服务
      ansible.builtin.systemd:
        name: rsyslog
        state: restarted
      when: modify_result is succeeded

    - name: 输出修改结果
      debug:
        var: modify_result.stdout

    - name: 输出检查结果
      debug:
        var: check_result.stdout

[root@ecs-564e ~/ansbile]$ 
```

##  批量ssh认证

```
[root@localhost ansible]# cat hosts_renzheng.ini 
[centos]
10.100.23.121 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.122 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.123 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.124 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.125 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.126 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.127 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.128 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.129 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.130 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.131 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.132 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.133 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.134 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
10.100.23.136 ansible_ssh_user=root ansible_ssh_pass=密码 ansible_ssh_common_args='-o StrictHostKeyChecking=no'
```

```
[root@localhost ansible]# cat ssh_copy_id.yml 
---
- name: 批量通过 ssh-copy-id 实现密钥认证
  hosts: centos
  become: yes
  tasks:
    - name: 将控制节点的公钥复制到目标主机
      authorized_key:
        user: "{{ ansible_ssh_user }}"
        state: present
        key: "{{ lookup('file', '/root/.ssh/id_rsa.pub') }}"
```
执行

```
ansible-playbook -i hosts_renzheng.ini ssh_copy_id.yml
```
四层tcp转发

```
yum install haproxy -y
```



连接192.168.7.152 9999

```
global
    log /dev/log  local0 warning
    chroot      /var/lib/haproxy
    pidfile     /var/run/haproxy.pid
    maxconn     4000
    user        haproxy
    group       haproxy
    daemon

    stats socket /var/lib/haproxy/stats

defaults
    log global
    option  httplog
    option  dontlognull
    timeout connect 5000
    timeout client 50000
    timeout server 50000

frontend rserve-frontend
    bind *:9999  # 监听 9999 端口
    mode tcp     # 使用 TCP 模式
    option tcplog
    default_backend rserve-backend

backend rserve-backend
    mode tcp     # 使用 TCP 模式
    option tcp-check
    balance roundrobin  # 使用轮询负载均衡算法
    default-server inter 10s downinter 5s rise 2 fall 2 slowstart 60s maxconn 250 maxqueue 256 weight 100
    server rserve-server-1 192.168.7.152:9999 check  # 替换为 Rserve 服务器的 IP 和端口
```

```
systemctl enable haproxy --now
```

```
# python 测试


pip3 install pyRserve
import pyRserve
try:
    conn = pyRserve.connect(host='ip', port=9999)  # 替换为 HAProxy 服务器的 IP
    result = conn.eval('1+1')  # 执行 R 代码
    print(f"Result: {result}")
except Exception as e:  
    print(f"Error: {e}")
finally:
    if 'conn' in locals() and not conn.isClosed:  
        conn.close() 
```


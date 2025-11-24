
<!-- more -->
## 默认nginx.conf
```
user nginx;
worker_processes auto;  # 根据 CPU 核心数自动调整 worker 数量

error_log /var/log/nginx/error.log notice;
pid /var/run/nginx.pid;

events {
    worker_connections 4096;  # 设置每个 worker 进程允许的最大连接数
}

http {
    include /etc/nginx/mime.types;  # 包含 MIME 类型映射文件
    default_type application/octet-stream;  # 设置默认 MIME 类型

    log_format main '$remote_addr - $remote_user [$time_local] '
                      '"$request" $status $body_bytes_sent '
                      '"$http_referer" "$http_user_agent" '
                      '"$http_x_forwarded_for"'
                      '$request_time $upstream_response_time';  # 定义日志格式

    access_log /var/log/nginx/access.log main;  # 指定访问日志文件和使用的日志格式

    sendfile on;  # 启用 sendfile，提高文件传输效率
    tcp_nopush on;  # 减少网络延迟，提高传输效率
    tcp_nodelay on;  # 启用 TCP_NODELAY，减少延迟
    keepalive_timeout 65;  # 设置 keep-alive 超时时间

    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;  # 支持的 SSL/TLS 协议版本
    ssl_prefer_server_ciphers on;   # 优先使用服务器端的加密算法
    ssl_session_timeout 1d;         # SSL 会话超时时间
    ssl_session_cache shared:SSL:50m;  # SSL 会话缓存大小

    # gzip 压缩配置
    gzip on;  # 启用 gzip 压缩
    gzip_types text/plain text/css application/json application/javascript;  # 指定压缩的 MIME 类型

    # 安全和性能相关设置
    server_tokens off;  # 禁止显示 nginx 版本信息

    include /etc/nginx/conf.d/*.conf;  # 包含额外的配置文件
}
```
## 规则
只有10.100.10.100才能访问网站，其他ip访问是本地静态页面
```

    server {
        listen       80;
        server_name  xxxx;
        
        client_max_body_size 1024m;
        
        return 301 https://$server_name$request_uri;
        error_page 520 /index.html;
   location / {
        if ($http_x_forwarded_for != "10.100.10.100") {
            return 520;
      }     
      
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    
    add_header X-Route-Ip $upstream_addr;
    add_header X-Route-Status $upstream_status;
    
    proxy_connect_timeout 300s;
    proxy_send_timeout 300s;
    proxy_read_timeout 300s;
    proxy_pass http://gxpt;
    }
        location = /index.html {
        root /usr/local/nginx/html/dist;
        }
        

}

```
![d0dc3c4fa4f593e4cbf6ec40634ad167.image](https://liu-fu-gui.github.io/myimg/halo/d0dc3c4fa4f593e4cbf6ec40634ad167.image.webp)


配置文件示例：

```
### 全局配置（Main模块）
user nginx;  # 指定运行worker进程的用户和组
worker_processes auto;  # worker进程的个数，auto表示自动检测CPU核心数
error_log /var/log/nginx/error.log warn;  # 错误日志存放路径及日志级别
pid /var/run/nginx.pid;  # nginx的pid文件存放路径
worker_rlimit_nofile 65535;  # 一个worker进程所能够打开的最大文件句柄数

### 事件处理（Events模块）

events {
    worker_connections 1024;  # 每个worker进程支持的最大连接数
    use epoll;  # 使用epoll事件驱动模型，Linux推荐
    multi_accept on;  # 是否允许一次性地响应多个用户请求
}

### HTTP服务配置（Http模块）
http {
    include       /etc/nginx/mime.types;  # 引入MIME类型文件
    default_type  application/octet-stream;  # 默认MIME类型

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';  # 定义日志格式

    access_log  /var/log/nginx/access.log  main;  # 访问日志存放路径及格式

    sendfile        on;  # 是否使用sendfile传输文件
    tcp_nopush     on;  # 启用TCP_NOPUSH或TCP_CORK选项

    keepalive_timeout  65;  # 长连接超时时间

    # 虚拟主机配置
    server {
        listen       80;  # 监听端口
        server_name  localhost;  # 服务名称

        location / {
            root   /usr/share/nginx/html;  # 资源存放的根目录
            index  index.html index.htm;  # 默认访问页面
        }

        # 错误页面配置
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   /usr/share/nginx/html;
        }

        # 反向代理配置示例
        location /app {
            proxy_pass http://127.0.0.1:8080;  # 转发请求到后端服务器
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }

        # 负载均衡配置示例
        upstream myapp1 {
            server backend1.example.com weight=5;
            server backend2.example.com;
        }

        server {
            listen 80;
            server_name myapp.example.com;

            location / {
                proxy_pass http://myapp1;  # 使用上面定义的upstream
            }
        }
    }
}
```
## 一、全局块
### worker_processes
- 作用：设置工作进程的数量。
- 配置示例：worker_processes auto; 或 worker_processes 4;
- 说明：通常建议设置为机器的CPU核心数或核心数减1，也可以设置为auto让Nginx自动检测。

### worker_rlimit_nofile
- 作用：设置每个worker进程可以打开的最大文件描述符数（文件句柄）。
- 配置示例：worker_rlimit_nofile 65535;
- 说明：此值应足够大以支持预期的并发连接数。

## 二、Events块
### worker_connections
- 作用：设置每个worker进程的最大并发连接数。
- 配置示例：worker_connections 1024;
- 说明：Nginx服务器的最大连接数等于worker_processes * worker_connections。

### use
- 作用：指定Nginx使用的事件驱动模型。
- 配置示例：use epoll;
- 说明：Linux系统推荐使用epoll，它是完全的事件机制，可以实现多路IO复用。
### multi_accept
- 作用：控制是否启用“接收新连接”的多路复用。
- 配置示例：multi_accept on;
- 说明：当设置为on时，Nginx允许多个worker进程同时接受新连接，提高并发处理能力。

## 三、Http块

### client_header_buffer_size
- 作用：设置客户端请求的请求行+请求头缓冲区大小。
- 配置示例：client_header_buffer_size 4k;
- 说明：默认值是1k或4k，取决于操作系统。
### large_client_header_buffers
- 作用：设置Nginx服务器接收和缓存客户端请求头的缓冲区的大小。
- 配置示例：large_client_header_buffers 4 8k;
- 说明：用于处理大型或包含大量请求头的客户端请求。
### client_header_timeout
- 作用：定义Nginx读取客户端请求头部的超时时间。
- 配置示例：client_header_timeout 60s;
- 说明：如果客户端在这段时间内没有发送完请求头，则连接将被关闭。
### keepalive_timeout
- 作用：设置长连接的超时时间。
- 配置示例：keepalive_timeout 65;
- 说明：如果设置为0，则表示禁用长连接。
### gzip
- 作用：开启或关闭gzip压缩功能。
- 配置示例：gzip on;
- 说明：开启gzip可以减小传输数据量，提高网页加载速度。
### proxy_pass
- 作用：将请求转发到后端服务器。
- 配置示例：location / { proxy_pass http://backend; }
- 说明：常用于反向代理和负载均衡场景。

## 四、其他重要配置

### server_name
- 作用：定义虚拟主机的名称。
- 配置示例：server_name www.example.com;
- 说明：Nginx根据server_name来区分不同的虚拟主机。
### listen
- 作用：指定Nginx监听的端口。
- 配置示例：listen 80;
- 说明：Nginx默认监听80端口。
### error_log
- 作用：设置错误日志文件的路径和级别。
- 配置示例：error_log /var/log/nginx/error.log warn;
- 说明：日志级别有debug、info、notice、warn、error、crit、alert、emerg等。

### access_log
- 作用：设置访问日志文件的路径和格式。
- 配置示例：access_log /var/log/nginx/access.log main;
- 说明：通过访问


:::note{title="注"}
配置文件结构：Nginx的配置文件（nginx.conf）通常包含全局块、events块、http块等，其中http块可以包含多个server块，每个server块又可以包含多个location块。

日志和错误处理：合理配置日志和错误处理对于监控和调试Nginx服务至关重要。

性能优化：通过调整worker_processes、worker_connections等参数，可以优化Nginx的性能。

安全性：合理配置SSL/TLS、限制请求速率、设置访问控制等，可以提高Nginx服务的安全性。



## Nginx 正则匹配


![7fec8ad7f81ae108cab198f0656fc08c.image](https://liu-fu-gui.github.io/myimg/halo/7fec8ad7f81ae108cab198f0656fc08c.image.webp)

### 一、Nginx正则匹配规则
Nginx在配置文件中使用location指令来定义请求的匹配规则，其中正则匹配主要通过在location指令后添加特定的修饰符（如~、~*、^~等）来实现。
1. **精确匹配**：使用=修饰符，表示请求的URI必须与指定的字符串完全相等才能匹配。

2. **普通字符串前缀匹配**：如果请求URI以指定的字符串为开头，则使用该location模块处理请求。如果没有修饰符，则默认为普通字符串前缀匹配。
3. **正则表达式匹配**
-  ~：表示区分大小写的正则表达式匹配
-  ~*：表示不区分大小写的正则表达式匹配。
-  ^~：表示普通字符串匹配，但如果匹配成功，则不再进行正则表达式的匹配。 
4. **内部跳转**：
  使用@修饰符，用于Nginx内部跳转。


### 二、Nginx正则匹配优先级
当多个location块同时匹配请求URI时，Nginx按照以下规则选择location块处理请求：

1. 精确匹配（=）的优先级最高。
2. 如果存在多个普通字符串前缀匹配，则使用字符串长度最长的location模块处理请求。
3. 如果存在多个正则表达式匹配，则使用第一个匹配成功的location模块处理请求。
4. 如果正则匹配和普通字符串前缀匹配同时存在，且正则匹配成功，则使用正则匹配的location模块处理请求（除非使用了^~修饰符）。
5. 
### 三、Nginx正则匹配案例
#### 精确匹配案例：

```
location = / {
   # 处理对根目录的请求
}
```
这个配置只会匹配对根目录（即/）的请求。


#### 普通字符串前缀匹配案例：
```
location /images/ {
   # 处理所有以/images/开头的请求
}
```
这个配置会匹配所有以/images/开头的请求，但会继续向下搜索更具体的匹配规则。
#### 正则表达式匹配案例：
```
location ~ \.(gif|jpg|jpeg)$ {
   # 处理所有以gif、jpg或jpeg结尾的请求
}
```
这个配置使用正则表达式匹配所有以.gif、.jpg或.jpeg结尾的请求，且不区分大小写（但这里使用的是~，实际上应使用~*来明确表示不区分大小写）。
#### ^~修饰符案例：

```
location ^~ /images/ {
   # 匹配所有以/images/开头的请求，并停止搜索正则
}
```
这个配置会匹配所有以/images/开头的请求，并且一旦匹配成功，就不会再进行正则表达式的匹配。
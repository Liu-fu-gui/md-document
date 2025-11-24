<!-- more -->
参考
https://www.cnblogs.com/studyjobs/p/18014237

# 使用 docker-compose 部署 nacos（单机和集群）
## 单机（测镜像能不能起飞）
nacos 默认使用自带的 derby 数据库（类似于 sqlite 的文件型数据库），在开发环境部署单机版 nacos ，可以直接使用。

在虚拟机上创建 /app/nacos-single1 目录，里面放置一个 logs 目录和 docker-compose.yml
编写 docker-compose.yml 文件内容如下：
```
version: '3.5'
services:
  nacos:
    image: nacos/nacos-server:v2.2.2
    container_name: nacos
    restart: always
    ports:
      # web 界面访问端口
      - 8848:8848
      # 程序使用 grpc 连接的端口
      - 9848:9848
    environment:
      - MODE=standalone
    volumes:
      - /home/nacos-single/logs:/home/nacos/logs
```
## 单机（链接本地mysql，测试数据库连接）
首先需要在 mysql 中随便创建一个数据库（比如数据库名叫做 nacos），然后运行 nacos 提供的 sql 脚本初始化数据库，具体细节可参考上面贴出来的之前博客内容。
在虚拟机上创建 /app/nacos-single2 目录，里面放置一个 logs 目录和 docker-compose.yml

编写 docker-compose.yml 文件内容如下：

```
services:
  nacos:
    image: nacos/nacos-server:v2.2.2
    container_name: nacos
    restart: always
    ports:
      # web 界面访问端口
      - 8848:8848
      # 程序使用 grpc 连接的端口
      - 9848:9848
    environment:
      - MODE=standalone
      # 使用 mysql 作为数据库
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=192.168.136.128
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=root
      # 设置连接 mysql 的连接参数
      - MYSQL_DB_PARAM="characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&serverTimezone=Asia/Shanghai"
    volumes:
      - /home/nacos-single/logs:/home/nacos/logs
```
## 集群版部署
集群版部署主要满足以下几个条件即可：（注意：nacos 集群至少需要 3 个节点）

必须使用 mysql 数据库，每个节点需要连接同一个 mysql 数据库
每个节点都需要把所有节点的访问 ip 和端口配置上
如果启用了用户名和密码验证，则必须每个节点的安全配置内容一致
在虚拟机上创建 /app/nacos-cluster 目录，里面放置 3 个 logs 目录、nginx.conf、docker-compose.yml

编写 docker-compose.yml 文件内容如下：

```
services:
  nacos1:
    image: nacos/nacos-server:v2.0.4
    container_name: nacos1
    hostname: nacos1
    restart: always
    ports:
      # Web 界面访问端口
      - 8841:8848
      # 程序使用 grpc 连接的端口
      - 9841:9848
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848,nacos2:8848,nacos3:8848
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=
      - MYSQL_SERVICE_PASSWORD=
      - MYSQL_DB_PARAM="characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC"
    volumes:
      - /home/nacos-cluster/logs1:/home/nacos/logs
    networks:
      - nacos_net

  nacos2:
    image: nacos/nacos-server:v2.0.4
    container_name: nacos2
    hostname: nacos2
    restart: always
    ports:
      # Web 界面访问端口
      - 8842:8848
      # 程序使用 grpc 连接的端口
      - 9842:9848
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848,nacos2:8848,nacos3:8848
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=
      - MYSQL_SERVICE_PASSWORD=
      - MYSQL_DB_PARAM="characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC"
    volumes:
      - /home/nacos-cluster/logs2:/home/nacos/logs
    networks:
      - nacos_net

  nacos3:
    image: nacos/nacos-server:v2.0.4
    container_name: nacos3
    hostname: nacos3
    restart: always
    ports:
      # Web 界面访问端口
      - 8843:8848
      # 程序使用 grpc 连接的端口
      - 9843:9848
    environment:
      - MODE=cluster
      - PREFER_HOST_MODE=hostname
      - NACOS_SERVERS=nacos1:8848,nacos2:8848,nacos3:8848
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=
      - MYSQL_SERVICE_PASSWORD=
      - MYSQL_DB_PARAM="characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC"
    volumes:
      - /home/nacos-cluster/logs3:/home/nacos/logs
    networks:
      - nacos_net

  nginx:
    image: registry.cn-hangzhou.aliyuncs.com/imagessync/nginx:latest
    container_name: nginx
    privileged: true
    restart: always
    volumes:
      - /home/nacos-cluster/nginx.conf:/etc/nginx/nginx.conf
    ports:
      - 80:80
      - 1080:1080
    networks:
      - nacos_net
    depends_on:
      - nacos1
      - nacos2
      - nacos3

# 网络配置
networks:
  nacos_net:
    driver: bridge
```
下面列出 nginx 的配置文件内容，由于 nginx 跟 3 个 nacos 节点在相同的网络 nacos_net，因此可以直接通过服务名称访问 3 个 nacos 节点，并直接对 3 个容器内部的 8848 和 9848 端口进行转发，其中 8848 采用 http 转发，9848 采用 tcp 转发，nginx 在 1.19 版本开始支持 tcp 的转发。

```
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;
 
events {
    worker_connections 1024;
}
 
# 配置 http 服务
log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                  '$status $body_bytes_sent "$http_referer" '
                  '"$http_user_agent" "$http_x_forwarded_for" '
                  'upstream_addr=$upstream_addr upstream_response_time=$upstream_response_time';

 
    access_log  /var/log/nginx/access.log  main;
 
    sendfile            on;
    tcp_nopush          on;
    tcp_nodelay         on;
    keepalive_timeout   65;
    types_hash_max_size 4096;
 
    include             /etc/nginx/mime.types;
    default_type        application/octet-stream;
 
    upstream nacos-cluster {
        server nacos1:8848;
        server nacos2:8848;
        server nacos3:8848;
    }
 
    server {
        listen       80;
        server_name  localhost;
 
        location / {
            proxy_pass http://nacos-cluster;
        }
    }
}
 
# 配置 tcp 服务
stream {
    upstream grpc-cluster {
        server nacos1:9848;
        server nacos2:9848;
        server nacos3:9848;
    }
 
    server {
        listen     1080;
        proxy_pass grpc-cluster;
    }
}
```
然后运行 docker-compose up -d 命令启动服务，由于我们既把每个节点的端口都映射出来，也采用 nginx 对容器内部的端口进行了转发，因此以下 4 个地址都可以访问 nacos 网站：

```
nginx 转发统一后的地址：http://192.168.136.128/nacos
nacos1 的访问地址：http://192.168.136.128:8841/nacos
nacos2 的访问地址：http://192.168.136.128:8842/nacos
nacos3 的访问地址：http://192.168.136.128:8843/nacos
```


三、启用账号密码访问
默认情况下，nacos 不需要用户名和密码验证，以上部署后的成果，直接访问就会进入 nacos 配置界面中。

对于 nacos 来说，一般情况下都是在内网使用，所以可以不启用账号密码验证。

如果想要启动账号密码访问，只需要在上面的 docker-compose.yml 中的 environment 下面增加以下环境变量配置即可：

```
# 设置以下环境变量时，启用账号密码登录（默认的账号和密码都是 nacos）
environment:
  # 启用账号密码验证
  - NACOS_AUTH_ENABLE=true
  # 随便使用一个32个字符组成的字符串，生成 base64 字符串，填写到这里即可
  - NACOS_AUTH_TOKEN=VGhpc0lzTXlDdXN0b21TZWNyZXRLZXkwMTIzNDU2Nzg=
  # 随便填写
  - NACOS_AUTH_IDENTITY_KEY=JobsKey
  # 随便填写
  - NACOS_AUTH_IDENTITY_VALUE=JobsValue
```

对于 nacos 集群来说，以上环境变量的配置内容必须保持一致。


OK，以上就是采用 docker-compose 部署单机 nacos 和 集群 nacos 的全部内容，有关 java 的连接操作，可以参考我之前发布的博客。



# 一包双机

## 下载tar

```
https://github.com/alibaba/nacos/releases/download/2.0.4/nacos-server-2.0.4.tar.gz
tar -zxvf  nacos-server-2.0.4.tar.gz
```
## 登录mysql 执行

```
 mysql -u 账号 -p密码 -h ip -P 3306 nacos > source {安装地址}/nacos/conf/mysql-schema.sql
```
## 修改conf/application.properties

```
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://ip:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=
db.password.0=
```
## 单节点启动就直接

```
sh startup.sh -m standalone
```
## 多机器ip

```
vim conf/cluster.conf
格式为
ip:端口

然后执行
sh startup.sh
```


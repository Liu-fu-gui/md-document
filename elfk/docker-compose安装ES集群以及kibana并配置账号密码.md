# docker-compose安装ES集群以及kibana并配置账号密码 
参考：
https://www.cnblogs.com/wangqq1217/p/17551255.html
<!-- more -->
## 一、安装不带密码的es集群
### 1.1 目录结构

```
-home
 -elasticsearch
       -node1
         -data
         -logs
         -config
           -elasticsearch.yml
       -node2
         -data
         -logs
         -config
           -elasticsearch.yml
       -node3
         -data
         -logs
         -config
           -elasticsearch.yml
```
### 1.2 docker-compse.yml配置文件

```
[root@docker ~/elk]$ cat es3-ceshi.yaml 
version: '3.8'

services:
  elasticsearch1:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2          
    container_name: es01
    volumes:
      - /home/elasticsearch/node1/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node1/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node1/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
     
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - elastic

  elasticsearch2:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es02
    volumes:
      - /home/elasticsearch/node2/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node2/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node2/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml

    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

  elasticsearch3:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es03
    volumes:
      - /home/elasticsearch/node3/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node3/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node3/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
    
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

networks:
  elastic:
    driver: bridge
```
### 1.3 elasticsearch.yml配置文件
#### es01中的elasticsearch.yml配置文件
```
# 集群名称[三个容器保持一致]
cluster.name: es-cluster
# 节点名称
node.name: es01
# 绑定host，0.0.0.0代表当前节点的ip
network.host: 0.0.0.0
# 表示这个节点是否可以充当主节点
node.master: true
# 是否充当数据节点
node.data: true
# 所有主从节点
discovery.seed_hosts: ["es01", "es02", "es03"]
# 这个参数决定了在选主过程中需要 有多少个节点通信  预防脑裂 N/2+1
discovery.zen.minimum_master_nodes: 3
#初始化主节点
cluster.initial_master_nodes: es01
# 单节点上可以开启的ES存储实例的个数,没配置的话会报一个错误
node.max_local_storage_nodes: 3
```
#### es02中的elasticsearch.yml配置文件

```
# 集群名称[三个容器保持一致]
cluster.name: es-cluster
# 节点名称
node.name: es02
# 绑定host，0.0.0.0代表当前节点的ip
network.host: 0.0.0.0
# 表示这个节点是否可以充当主节点
node.master: false
# 是否充当数据节点
node.data: true
# 所有主从节点
discovery.seed_hosts: ["es01", "es02", "es03"]
# 这个参数决定了在选主过程中需要 有多少个节点通信  预防脑裂 N/2+1
discovery.zen.minimum_master_nodes: 3
#初始化主节点
cluster.initial_master_nodes: es01
# 单节点上可以开启的ES存储实例的个数,没配置的话会报一个错误
node.max_local_storage_nodes: 3
```
#### es03中的elasticsearch.yml配置文件

```
# 集群名称[三个容器保持一致]
cluster.name: es-cluster
# 节点名称
node.name: es03
# 绑定host，0.0.0.0代表当前节点的ip
network.host: 0.0.0.0
# 表示这个节点是否可以充当主节点
node.master: false
# 是否充当数据节点
node.data: true
# 所有主从节点
discovery.seed_hosts: ["es01", "es02", "es03"]
# 这个参数决定了在选主过程中需要 有多少个节点通信  预防脑裂 N/2+1
discovery.zen.minimum_master_nodes: 3
#初始化主节点
cluster.initial_master_nodes: es01
# 单节点上可以开启的ES存储实例的个数,没配置的话会报一个错误
node.max_local_storage_nodes: 3
```
### 1.4 启动es并校验

```
docker-compose up -d
```
在浏览器上使用http://宿主机ip:port/_cat/nodes查看集群，正常情况能看到如下的情况，即表示es安装成功【注意，默认的9200的端口需要开放】：

```
192.168.192.2 19 41 4 0.07 0.64 1.21 dilm * es01
192.168.192.3 20 41 4 0.07 0.64 1.21 dilm - es03
192.168.192.4 17 41 4 0.07 0.64 1.21 dilm - es02
```
## 二、添加集群的密码设置
### 2.1 修改elasticsearch.yml配置文件

在三个es对应的elasticsearch.yml配置文件的最后，分别添加如下内容，每个里面都添加

```
# 开启x-pack功能
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate
```
### 2.2 重启es集群服务

```
docker-compose down
docker-compose up -d
```
### 2.3 生成证书文件
执行以下命令进入es01的容器内部【其他两个容器也可以】

```
docker exec -it es01 /bin/bash
```
执行命令生成证书文件，根据提示输入y

```
./bin/elasticsearch-certutil cert -out config/elastic-certificates.p12 -pass ""
```
执行完毕后，记录证书文件的位置，一般情况下是： /usr/share/elasticsearch/config/elastic-certificates.p12，输入exit或者ctrl+D退出容器；
复制容器内部的证书文件到本地磁盘中来；

```
docker cp es01:/usr/share/elasticsearch/config/elastic-certificates.p12 ./
```
将复制出来的证书文件，分别复制在三个es的config目录一份

```
cp elastic-certificates.p12 /home/elasticsearch/node1/config
cp elastic-certificates.p12 /home/elasticsearch/node2/config
cp elastic-certificates.p12 /home/elasticsearch/node3/config
```
给当前文件夹授权，让可访问密钥文件

```
chmod -R 777 /home/elasticsearch
```

### 2.4 修改docker-compse.yml配置文件
在docker-compse.yml配置文件中，添加证书文件的地址映射配置

```
[root@docker ~/elk]$ cat es3-ceshi.yaml 
version: '3.8'

services:
  elasticsearch1:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2          
    container_name: es01
    volumes:
      - /home/elasticsearch/node1/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node1/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node1/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node1/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - elastic

  elasticsearch2:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es02
    volumes:
      - /home/elasticsearch/node2/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node2/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node2/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node2/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

  elasticsearch3:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es03
    volumes:
      - /home/elasticsearch/node3/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node3/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node3/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node3/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

networks:
  elastic:
    driver: bridge
```
### 2.5 再次修改elasticsearch.yml配置文件
在三个elasticsearch.yml配置文件中，分别添加以下两行配置，用来指定证书文件的路径【此处指定的内容为容器内部的地址，而非磁盘路径】

```
xpack.security.transport.ssl.keystore.path: /usr/share/elasticsearch/config/elastic-certificates.p12
xpack.security.transport.ssl.truststore.path: /usr/share/elasticsearch/config/elastic-certificates.p12
```
### 2.6 再次重启es集群服务#
在docker-compose.yml文件的同级目录，执行以下命令，关闭并删除es的容器

```
docker-compose down
docker-compose up -d 
```
### 2.7 设置es集群的各种密码
进入容器内部【三个容器任意一个都行】
docker exec -it es01 /bin/bash
执行手动设置密码的命令【也可以使用自动生成的密码，但是不好记忆，在此忽略】

```
./bin/elasticsearch-setup-passwords interactive
### 先输入y
### 然后依次给es预设的账号设置密码
### ps:有七八个账号要设置,要么一一记录下来,要么设置成一样的
```


设置完成后，执行exit命令或者ctrl+D退出容器，访问es就需要账号密码了，默认账号是【elastic】

```
http://宿主机ip:port/_cat/nodes
输入账号:elastic
输入密码: ***********

能看到类似以下内容即可：
192.168.192.2 19 41 4 0.07 0.64 1.21 dilm * es01
192.168.192.3 20 41 4 0.07 0.64 1.21 dilm - es03
192.168.192.4 17 41 4 0.07 0.64 1.21 dilm - es02
```



## 三、安装kibana并配置账号密码
### 3.1 目录结构

```
-home
 -kibana
      -config
         -kibana.yml
         mkdir
```
### 3.2 docker-compse.yml配置文件

```
version: '3.7'

services:
  kibana:
    image: docker.elastic.co/kibana/kibana:7.10.2
    container_name: kibana
    restart: unless-stopped
    privileged: true
    volumes:
      - ./kibana/config/kibana.yml:/usr/share/kibana/config/kibana.yml
    ports:
      - 5601:5601
    networks:
      - es
networks:
  es:
```
### 3.3 kibana.yml配置文件

```
server.name: kibana
server.host: "0"
# 可以填容器名加端口,也可以用宿主机ip和映射的端口，此处填写主节点的容器路径即可
elasticsearch.hosts: [ "http://127.0.0.1:9200/"]
xpack.monitoring.ui.container.elasticsearch.enabled: true
i18n.locale: zh-CN
# 设置用户名和密码，用户名固定不用改，密码是2.7步骤中设置的密码
elasticsearch.username: "elastic"
elasticsearch.password: "xxxxxxx"
# kibana默认端口是5601
server.port: 5601
```
### 3.4 启动es并访问

```
docker-compose up -d
```
## 四、遇到的问题
### 4.1 证书文件没有执行权限

```
# 使用该命令添加证书的执行权限
chmod +x elastic-certificates.p12
```

## ik插件安装--地图

进入到每个es01 es02 es03 执行
```
bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/7.10.2
```
安装完之后记得三个容器都重启
验证下就好了

```
GET /_cat/plugins?v
```
![20241129231144](https://liu-fu-gui.github.io/myimg/halo/20241129231144.png)
### 4.2 kibana无法访问，提示"Kibana server is not ready yet"

1.核验kibana.yml配置文件中的es的地址能被访问

2.检查用户名密码是否正确

3.kibana启动后需要稍等1分钟左右才能被访问


## 五.logstash
### 5.1 目录结构

```
-home
 -logstash
      -config
         -logstash.yml
         -logstash.conf
```
logstash.yml内容

```
http.host: "0.0.0.0"

xpack.monitoring.elasticsearch.hosts: [ 
    "http://10.100.20.206:9200",
]
xpack.monitoring.elasticsearch.username: "elastic"
xpack.monitoring.elasticsearch.password: "ptMF0bT5jMErJnmi5HWy"
```
logstash.conf内容

```
input {
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4560
    codec => json_lines
    type => "debug"
  }
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4561
    codec => json_lines
    type => "error"
  }
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4562
    codec => json_lines
    type => "business"
  }
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4563
    codec => json_lines
    type => "record"
  }
}
filter{
  if [type] == "record" {
    mutate {
      remove_field => "port"
      remove_field => "host"
      remove_field => "@version"
    }
    json {
      source => "message"
      remove_field => ["message"]
    }
  }
}
#output {
#  elasticsearch {
#    hosts => "http://elasticsearch-node1:9200"
#    index => "open-course-%{type}-%{+YYYY.MM.dd}"
#    user => "root"
#    password => "zjtvu_zst1"
#  }
#}
output {
  elasticsearch {
    hosts => "http://10.100.20.206:9200"
    index => "open-%{type}-%{+YYYY.MM.dd}"
    user => "elastic"
    password => "ptMF0bT5jMErJnmi5HWy"
  }
}
```
docker-compste

```
version: '3.7'

services:
  logstash:
    image: docker.elastic.co/logstash/logstash:7.10.2
    container_name: logstash
    ports:
      - "4560:4560"
      - "4561:4561"
      - "4562:4562"
      - "4563:4563"
    depends_on:
      - es01
      - es02
    volumes:
      - /home/logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
      - /home/logstash/logstash.yml:/usr/share/logstash/config/logstash.yml
    environment:
      - LOGSTASH_JAVA_OPTS=-Xms1g -Xmx1g
    restart: unless-stopped
```











# 总结

## es3.yaml

```
version: '3.8'

services:
  elasticsearch1:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2          
    container_name: es01
    volumes:
      - /home/elasticsearch/node1/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node1/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node1/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node1/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
      - ELASTIC_PASSWORD=ptMF0bT5jMErJnmi5HWy
    ulimits:
      memlock:
        soft: -1
        hard: -1
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - elastic

  elasticsearch2:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es02
    volumes:
      - /home/elasticsearch/node2/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node2/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node2/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node2/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
      - ELASTIC_PASSWORD=ptMF0bT5jMErJnmi5HWy
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

  elasticsearch3:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: es03
    volumes:
      - /home/elasticsearch/node3/data:/usr/share/elasticsearch/data
      - /home/elasticsearch/node3/logs:/usr/share/elasticsearch/logs
      - /home/elasticsearch/node3/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/node3/config/elastic-certificates.p12:/usr/share/elasticsearch/config/elastic-certificates.p12
    environment:
      - bootstrap.memory_lock=true
      - TZ=Asia/Shanghai
      - LANG=en_US.UTF-8
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m"
      - TAKE_FILE_OWNERSHIP=true
      - ELASTIC_PASSWORD=ptMF0bT5jMErJnmi5HWy
    ulimits:
      memlock:
        soft: -1
        hard: -1
    networks:
      - elastic

networks:
  elastic:
    driver: bridge

```

## logstash-kibana.yaml

```
version: '3.8'

services:
  logstash:
    image: docker.elastic.co/logstash/logstash:7.10.2
    container_name: logstash
    mem_limit: 2g
    ports:
      - "4560:4560"
      - "4561:4561"
      - "4562:4562"
      - "4563:4563"
    volumes:
      - /home/logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf:ro
      - /home/logstash/logstash.yml:/usr/share/logstash/config/logstash.yml:ro
    environment:
      - LOGSTASH_JAVA_OPTS=-Xms1g -Xmx1g
      - TZ=Asia/Shanghai
    restart: unless-stopped
    networks:
      - elk_elastic  # 修改此处以匹配实际网络名称

  kibana:
    image: docker.elastic.co/kibana/kibana:7.10.2
    container_name: kibana
    mem_limit: 1g
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://es01:9200
      - ELASTICSEARCH_USERNAME=elastic
      - ELASTICSEARCH_PASSWORD=ptMF0bT5jMErJnmi5HWy
      - TZ=Asia/Shanghai
    volumes:
      - /home/kibana/config/kibana.yml:/usr/share/kibana/config/kibana.yml:ro
    restart: unless-stopped
    networks:
      - elk_elastic  # 修改此处以匹配实际网络名称

networks:
  elk_elastic:  # 修改此处以匹配实际网络名称
    external: true

```


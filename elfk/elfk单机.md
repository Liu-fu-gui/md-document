
<!-- more -->
参考
https://www.elastic.co/guide/en/elasticsearch/reference/7.10/docker.html


## docker-com-elk脚本
```
[root@docker ~/elk]$ cat es-logstash-kibana.yaml 
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - cluster.name=elasticsearch
      - ES_JAVA_OPTS=-Xms4096m -Xmx4096m
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - /home/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml
      - /home/elasticsearch/plugins:/usr/share/elasticsearch/plugins
      - /home/elasticsearch/data:/usr/share/elasticsearch/data
    restart: unless-stopped
    networks:
      - elk_network

  logstash:
    image: docker.elastic.co/logstash/logstash:7.10.2
    container_name: logstash
    ports:
      - "4560:4560"
      - "4561:4561"
      - "4562:4562"
      - "4563:4563"
    depends_on:
      - elasticsearch
    volumes:
      - /home/logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
      - /home/logstash/logstash.yml:/usr/share/logstash/config/logstash.yml
    environment:
      - LOGSTASH_JAVA_OPTS=-Xms1g -Xmx1g
    restart: unless-stopped
    networks:
      - elk_network

  kibana:
    image: docker.elastic.co/kibana/kibana:7.10.2
    container_name: kibana
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    environment:
      - elasticsearch.hosts=http://elasticsearch:9200
    volumes:
      - /home/kibana/config/kibana.yml:/usr/share/kibana/config/kibana.yml
    restart: unless-stopped
    networks:
      - elk_network

networks:
  elk_network:
    driver: bridge

```

持久化存储的地址
```
cd /home/
mkdir elasticsearch kibana logstash

## elasticsearch

[root@docker /home]$ tree elasticsearch/config/
elasticsearch/config/
└── elasticsearch.yml

vim elasticsearch.yml 

http.host: 0.0.0.0
xpack.security.enabled: true
cluster.max_shards_per_node: 10000

## logstash 结构为
[root@docker /home/logstash]$ tree
.
├── logstash.conf
└── logstash.yml



[root@docker /home/logstash]$ cat logstash.conf 
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
#    hosts => "http://192.168.7.90:9200"
#    index => "open-course-%{type}-%{+YYYY.MM.dd}"
#    user => "root"
#    password => "zjtvu_zst1"
#  }
#}
output {
  elasticsearch {
    hosts => "http://elasticsearch:9200"
    index => "open-%{type}-%{+YYYY.MM.dd}"
    user => "elastic"
    password => "a123456"
  }
}


vim cat logstash.yml 

http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
xpack.monitoring.elasticsearch.username: "elastic"
xpack.monitoring.elasticsearch.password: "a123456"


## kibana
[root@docker /home]$ tree kibana/
kibana/
└── config
    └── kibana.yml

vim  kibana/config/kibana.yml 
server.host: "0.0.0.0"
server.shutdownTimeout: "5s"
elasticsearch.hosts: [ "http://elasticsearch:9200" ]
elasticsearch.username: elastic
elasticsearch.password: csbpP148ztxa9SQBxUf6 

```


只设置 elastic 用户密码

```
docker exec -it elasticsearch /bin/bash
bin/elasticsearch-setup-passwords auto
```
输入y自动生成
## filebeat 源

```
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
sudo tee /etc/yum.repos.d/elastic.repo <<EOF
[elastic-7.x]
name=Elastic repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
EOF

sudo yum clean all
sudo yum makecache   
                                             
sudo yum install filebeat
```

                                 
```
[root@java /etc/filebeat]$ cat filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /root/zjxxw/logs/*.log

output.elasticsearch:
  hosts: ["http://10.100.20.206:9200"]
  username: "elastic"
  password: "csbpP148ztxa9SQBxUf6"
  index: "10.100.20.204-java-%{[agent.version]}-%{[agent.hostname]}-%{+yyyy.MM.dd}"
#setup.ilm.enabled: auto
#setup.ilm.rollover_alias: "filebeat"
setup.ilm.policy_name: "7-days-default"
setup.template.name: "10.100.20.204-java"
setup.template.pattern: "10.100.20.204-java-*"
#setup.template.overwrite: true

```
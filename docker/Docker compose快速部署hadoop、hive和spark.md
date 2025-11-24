# 一、配置文件yml

## 1.docker-compose.yml

```
version: '3.4'
services:
namenode:
image:test/hadoop-namenode:1.1.0-hadoop2.8-java8
container_name:namenode
volumes:
-./data/namenode:/hadoop/dfs/name
environment:
-CLUSTER_NAME=test
env_file:
-./hadoop-hive.env
ports:
-50070:50070
-8020:8020
-5005:5005

resourcemanager:
image:test/hadoop-resourcemanager:1.1.0-hadoop2.8-java8
container_name:resourcemanager
environment:
-CLUSTER_NAME=test
env_file:
-./hadoop-hive.env
ports:
-8088:8088
depends_on:
-datanode

historyserver:
image:test/hadoop-historyserver:1.1.0-hadoop2.8-java8
container_name:historyserver
environment:
-CLUSTER_NAME=test
env_file:
-./hadoop-hive.env
ports:
-8188:8188

datanode:
image:test/hadoop-datanode:1.1.0-hadoop2.8-java8
container_name:datanode
depends_on:
-namenode
volumes:
-./data/datanode:/hadoop/dfs/data
env_file:
-./hadoop-hive.env
ports:
-50075:50075
-50010:50010

nodemanager:
image:test/hadoop-nodemanager:1.1.0-hadoop2.8-java8
container_name:nodemanager
hostname:nodemanager
environment:
-CLUSTER_NAME=test
env_file:
-./hadoop-hive.env
ports:
-8042:8042

hive-server:
image:test/hive:2.1.0-postgresql-metastore
container_name:hive-server
env_file:
-./hadoop-hive.env
environment:
-"HIVE_CORE_CONF_javax_jdo_option_ConnectionURL=jdbc:postgresql://hive-metastore/metastore"
ports:
-"10000:10000"

hive-metastore:
image:test/hive:2.1.0-postgresql-metastore
container_name:hive-metastore
env_file:
-./hadoop-hive.env
command:/opt/hive/bin/hive--servicemetastore
ports:
-9083:9083

#hive的元数据存储到postgresql
hive-metastore-postgresql:
image:test/hive-metastore-postgresql:2.1.0
container_name:hive-metastore-postgresql
ports:
-5432:5432
#win10直接挂载存在权限问题，只能使用name volume
volumes:
-hive-metastore-postgresql:/var/lib/postgresql/data

spark-master:
image:test/spark-master:2.1.0-hadoop2.8-hive-java8
container_name:spark-master
hostname:spark-master
volumes:
-./copy-jar.sh:/copy-jar.sh
ports:
-8888:8080
-7077:7077
env_file:
-./hadoop-hive.env
volumes:
-./hive-site.xml:/spark/conf/hive-site.xml#spark连接hive的配置文件

spark-worker:
image:test/spark-worker:2.1.0-hadoop2.8-hive-java8
container_name:spark-worker
depends_on:
-spark-master
environment:
-SPARK_MASTER=spark://spark-master:7077
ports:
-"8181:8081"
env_file:
-./hadoop-hive.env

mysql-server:
image:mysql:5.7
container_name:mysql-server
ports:
-"3606:3306"
environment:
-MYSQL_ROOT_PASSWORD=123456
volumes:
-./data/mysql:/var/lib/mysql#数据挂载到当前目录下的data/mysql文件夹

elasticsearch:
image:elasticsearch:7.5.2
container_name:elasticsearch
environment:
#      - discovery.type=single-node
-"ES_JAVA_OPTS=-Xms512m -Xmx512m"
ports:
-"9200:9200"
-"9300:9300"
volumes:
-es1:/usr/share/elasticsearch/data
-./conf/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml

kibana:
image:kibana:7.5.2
container_name:kibana
ports:
-"5601:5601"

redis:
image:redis
container_name:redis
ports:
-"6379:6379"

server:
image:openjdk:8-jre-slim
command:java-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005-jar/app.jar
ports:
-"8000:8080"
-"5555:5005"
volumes:
-./application.yml:/application.yml:ro
-./spark-itags-1.0-SNAPSHOT.jar:/app.jar:ro
environment:
-TZ=Asia/Shanghai#配置容器时区，默认UTC
-spring.profiles.active=development
restart:"no"
depends_on:
-redis

volumes:
hive-metastore-postgresql:
driver:local
es1:
driver: local
```

# 二、执行脚本

## 1.启动脚本run.sh

```
#!/bin/bash

docker-compose -f docker-compose.yml up -d namenode hive-metastore-postgresql
docker-compose -f docker-compose.yml up -d datanode hive-metastore
docker-compose -f docker-compose.yml up -d resourcemanager
docker-compose -f docker-compose.yml up -d nodemanager
docker-compose -f docker-compose.yml up -d historyserver
sleep 5
docker-compose -f docker-compose.yml up -d hive-server
docker-compose -f docker-compose.yml up -d spark-master spark-worker
docker-compose -f docker-compose.yml up -d mysql-server
docker-compose -f docker-compose.yml up -d elasticsearch
docker-compose -f docker-compose.yml up -d kibana
#选取并输出本机ip
my_ip=`ip route get 1|awk '{print $NF;exit}'`#$NF 表示的最后一个Field（列），即输出最后一个字段的内容
echo "Namenode: http://${my_ip}:50070"
echo "Datanode: http://${my_ip}:50075"
echo "Spark-master: http://${my_ip}:8080"
docker-compose exec spark-master bash -c "./copy-jar.sh && exit"
```

## 2.关闭脚本stop.sh

```
#!/bin/bash
docker-compose stop
```
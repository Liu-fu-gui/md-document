# kafka部署方式
## 基于kraft的kafka集群部署（裸机部署）
官方网站
https://kafka.apache.org/



参考

https://mp.weixin.qq.com/s/l_edIxq1orCPaTJ5jsbDqA

## 先入门
### 1.1配置hosts
```
echo -e "10.0.0.100 kafka01\n10.0.0.101 kafka02\n10.0.0.102 kafka03" |  tee -a /etc/hosts > /dev/null
```
![20241203161055](https://liu-fu-gui.github.io/myimg/halo/20241203161055.png)

### 1.2做免密
```
ssh-keygen -t rsa -b 4096
```

#### 1.2.1 轮询
这条命令会将本机的 SSH 公钥复制到 10.0.0.100、10.0.0.101 和 10.0.0.102 上，设置免密登录。替换 user 为你实际使用的用户名。
```
for host in 10.0.0.100 10.0.0.101 10.0.0.102; do ssh-copy-id user@$host; done
```

### 1.3下载安装包
```
wget https://archive.apache.org/dist/kafka/3.9.0/kafka_2.13-3.9.0.tgz
tar -zxvf kafka_2.13-3.9.0.gz -C /usr/local/
```
#### 1.4 安装Java 8 
```
yum install java-1.8.0-openjdk -y
yum install java-1.8.0-openjdk-devel -y
```
#### 1.5备份文件
```
cd /usr/local/kafka_2.13-3.9.0/config/kraft
cp server.properties{,.bak}
```
## 修改配置文件
node01 配置文件修改

```
##角色可同时为broker和controller
process.roles=broker,controller 
##node.id为当前服务器作为节点的id
node.id=1
##定义投票节点，用于选举Master,每个节点都必须配置

controller.quorum.voters=1@kafka01:9093,2@kafka02:9093,3@kafka03:9093

##9092为每个broker的通信端口，9093为controller节点的通信端口，如果一个节点是混合节点那就需要同时监听两个端口

listeners=PLAINTEXT://:9092,CONTROLLER://:9093 

##broker内部监听协议
inter.broker.listener.name=PLAINTEXT
##对外公开的端口

advertised.listeners=PLAINTEXT://kafka01:9092
controller.listener.names=CONTROLLER 

listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL,EXTERNAL:PLAINTEXT
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
##kafka数据默认存储的地方
log.dirs=/opt/kafka/kraft-combined-logs 
##每一个topic默认的分区
num.partitions=6
##恢复线程
num.recovery.threads.per.data.dir=3
##用于存储消费者组的消费偏移量信息的特殊主题，用于在发生故障时或者重新加入时能够恢复到之前的消费位置
offsets.topic.replication.factor=3
##用于存储事务状态信息的特殊主题，kafka支持事务性写入，当生产者使用事务模式写入数据时，信息会写入这个主题
transaction.state.log.replication.factor=3
transaction.state.log.min.isr=3
## 关闭自动创建主题的功能，避免由于误操作或错误配置创建无用的主题。
auto.create.topics.enable=false 
## 设置新主题的默认复制因子为 3，以确保数据有更高的可靠性和容错性。
default.replication.factor=3 

##数据的存储时间
log.retention.hours=168
##设置kafka一个数据段最大值1G
log.segment.bytes=1073741824
##检查数据过期时间300s一次
log.retention.check.interval.ms=300000
```
格式化

```
cd /usr/local/kafka_2.13-3.9.0
# 生成uuid
bin/kafka-storage.sh random-uuid
## 加入集群
bin/kafka-storage.sh format -t 1Ue3IZbaSKyXtlfQn3jBvA -c config/kraft/server.properties
## 前端启动
bin/kafka-server-start.sh config/kraft/server.properties
## 后端启动
bin/kafka-server-start.sh -daemon config/kraft/server.properties
```


## 验证

```
bin/kafka-broker-api-versions.sh --bootstrap-server kafka01:9092
```

```
kafka01:9092 (id: 1 rack: null) -> (
	Produce(0): 0 to 11 [usable: 11],
	Fetch(1): 0 to 17 [usable: 17],
	ListOffsets(2): 0 to 9 [usable: 9],
	Metadata(3): 0 to 12 [usable: 12],
	LeaderAndIsr(4): UNSUPPORTED,
	StopReplica(5): UNSUPPORTED,
	UpdateMetadata(6): UNSUPPORTED,
	ControlledShutdown(7): UNSUPPORTED,
	OffsetCommit(8): 0 to 9 [usable: 9],
	OffsetFetch(9): 0 to 9 [usable: 9],
	FindCoordinator(10): 0 to 6 [usable: 6],
	JoinGroup(11): 0 to 9 [usable: 9],
	Heartbeat(12): 0 to 4 [usable: 4],
	LeaveGroup(13): 0 to 5 [usable: 5],
	SyncGroup(14): 0 to 5 [usable: 5],
	DescribeGroups(15): 0 to 5 [usable: 5],
	ListGroups(16): 0 to 5 [usable: 5],
	SaslHandshake(17): 0 to 1 [usable: 1],
	ApiVersions(18): 0 to 4 [usable: 4],
	CreateTopics(19): 0 to 7 [usable: 7],
	DeleteTopics(20): 0 to 6 [usable: 6],
	DeleteRecords(21): 0 to 2 [usable: 2],
	InitProducerId(22): 0 to 5 [usable: 5],
	OffsetForLeaderEpoch(23): 0 to 4 [usable: 4],
	AddPartitionsToTxn(24): 0 to 5 [usable: 5],
	AddOffsetsToTxn(25): 0 to 4 [usable: 4],
	EndTxn(26): 0 to 4 [usable: 4],
	WriteTxnMarkers(27): 0 to 1 [usable: 1],
	TxnOffsetCommit(28): 0 to 4 [usable: 4],
	DescribeAcls(29): 0 to 3 [usable: 3],
	CreateAcls(30): 0 to 3 [usable: 3],
	DeleteAcls(31): 0 to 3 [usable: 3],
	DescribeConfigs(32): 0 to 4 [usable: 4],
	AlterConfigs(33): 0 to 2 [usable: 2],
	AlterReplicaLogDirs(34): 0 to 2 [usable: 2],
	DescribeLogDirs(35): 0 to 4 [usable: 4],
	SaslAuthenticate(36): 0 to 2 [usable: 2],
	CreatePartitions(37): 0 to 3 [usable: 3],
	CreateDelegationToken(38): 0 to 3 [usable: 3],
	RenewDelegationToken(39): 0 to 2 [usable: 2],
	ExpireDelegationToken(40): 0 to 2 [usable: 2],
	DescribeDelegationToken(41): 0 to 3 [usable: 3],
	DeleteGroups(42): 0 to 2 [usable: 2],
	ElectLeaders(43): 0 to 2 [usable: 2],
	IncrementalAlterConfigs(44): 0 to 1 [usable: 1],
	AlterPartitionReassignments(45): 0 [usable: 0],
	ListPartitionReassignments(46): 0 [usable: 0],
	OffsetDelete(47): 0 [usable: 0],
	DescribeClientQuotas(48): 0 to 1 [usable: 1],
	AlterClientQuotas(49): 0 to 1 [usable: 1],
	DescribeUserScramCredentials(50): 0 [usable: 0],
	AlterUserScramCredentials(51): 0 [usable: 0],
	DescribeQuorum(55): 0 to 2 [usable: 2],
	AlterPartition(56): UNSUPPORTED,
	UpdateFeatures(57): 0 to 1 [usable: 1],
	Envelope(58): UNSUPPORTED,
	DescribeCluster(60): 0 to 1 [usable: 1],
	DescribeProducers(61): 0 [usable: 0],
	UnregisterBroker(64): 0 [usable: 0],
	DescribeTransactions(65): 0 [usable: 0],
	ListTransactions(66): 0 to 1 [usable: 1],
	AllocateProducerIds(67): UNSUPPORTED,
	ConsumerGroupHeartbeat(68): 0 [usable: 0],
	ConsumerGroupDescribe(69): 0 [usable: 0],
	GetTelemetrySubscriptions(71): UNSUPPORTED,
	PushTelemetry(72): UNSUPPORTED,
	ListClientMetricsResources(74): 0 [usable: 0],
	DescribeTopicPartitions(75): 0 [usable: 0],
	ShareGroupHeartbeat(76): UNSUPPORTED,
	ShareGroupDescribe(77): UNSUPPORTED,
	ShareFetch(78): UNSUPPORTED,
	ShareAcknowledge(79): UNSUPPORTED,
	AddRaftVoter(80): 0 [usable: 0],
	RemoveRaftVoter(81): 0 [usable: 0],
	InitializeShareGroupState(83): UNSUPPORTED,
	ReadShareGroupState(84): UNSUPPORTED,
	WriteShareGroupState(85): UNSUPPORTED,
	DeleteShareGroupState(86): UNSUPPORTED,
	ReadShareGroupStateSummary(87): UNSUPPORTED
)
kafka03:9092 (id: 3 rack: null) -> (
	Produce(0): 0 to 11 [usable: 11],
	Fetch(1): 0 to 17 [usable: 17],
	ListOffsets(2): 0 to 9 [usable: 9],
	Metadata(3): 0 to 12 [usable: 12],
	LeaderAndIsr(4): UNSUPPORTED,
	StopReplica(5): UNSUPPORTED,
	UpdateMetadata(6): UNSUPPORTED,
	ControlledShutdown(7): UNSUPPORTED,
	OffsetCommit(8): 0 to 9 [usable: 9],
	OffsetFetch(9): 0 to 9 [usable: 9],
	FindCoordinator(10): 0 to 6 [usable: 6],
	JoinGroup(11): 0 to 9 [usable: 9],
	Heartbeat(12): 0 to 4 [usable: 4],
	LeaveGroup(13): 0 to 5 [usable: 5],
	SyncGroup(14): 0 to 5 [usable: 5],
	DescribeGroups(15): 0 to 5 [usable: 5],
	ListGroups(16): 0 to 5 [usable: 5],
	SaslHandshake(17): 0 to 1 [usable: 1],
	ApiVersions(18): 0 to 4 [usable: 4],
	CreateTopics(19): 0 to 7 [usable: 7],
	DeleteTopics(20): 0 to 6 [usable: 6],
	DeleteRecords(21): 0 to 2 [usable: 2],
	InitProducerId(22): 0 to 5 [usable: 5],
	OffsetForLeaderEpoch(23): 0 to 4 [usable: 4],
	AddPartitionsToTxn(24): 0 to 5 [usable: 5],
	AddOffsetsToTxn(25): 0 to 4 [usable: 4],
	EndTxn(26): 0 to 4 [usable: 4],
	WriteTxnMarkers(27): 0 to 1 [usable: 1],
	TxnOffsetCommit(28): 0 to 4 [usable: 4],
	DescribeAcls(29): 0 to 3 [usable: 3],
	CreateAcls(30): 0 to 3 [usable: 3],
	DeleteAcls(31): 0 to 3 [usable: 3],
	DescribeConfigs(32): 0 to 4 [usable: 4],
	AlterConfigs(33): 0 to 2 [usable: 2],
	AlterReplicaLogDirs(34): 0 to 2 [usable: 2],
	DescribeLogDirs(35): 0 to 4 [usable: 4],
	SaslAuthenticate(36): 0 to 2 [usable: 2],
	CreatePartitions(37): 0 to 3 [usable: 3],
	CreateDelegationToken(38): 0 to 3 [usable: 3],
	RenewDelegationToken(39): 0 to 2 [usable: 2],
	ExpireDelegationToken(40): 0 to 2 [usable: 2],
	DescribeDelegationToken(41): 0 to 3 [usable: 3],
	DeleteGroups(42): 0 to 2 [usable: 2],
	ElectLeaders(43): 0 to 2 [usable: 2],
	IncrementalAlterConfigs(44): 0 to 1 [usable: 1],
	AlterPartitionReassignments(45): 0 [usable: 0],
	ListPartitionReassignments(46): 0 [usable: 0],
	OffsetDelete(47): 0 [usable: 0],
	DescribeClientQuotas(48): 0 to 1 [usable: 1],
	AlterClientQuotas(49): 0 to 1 [usable: 1],
	DescribeUserScramCredentials(50): 0 [usable: 0],
	AlterUserScramCredentials(51): 0 [usable: 0],
	DescribeQuorum(55): 0 to 2 [usable: 2],
	AlterPartition(56): UNSUPPORTED,
	UpdateFeatures(57): 0 to 1 [usable: 1],
	Envelope(58): UNSUPPORTED,
	DescribeCluster(60): 0 to 1 [usable: 1],
	DescribeProducers(61): 0 [usable: 0],
	UnregisterBroker(64): 0 [usable: 0],
	DescribeTransactions(65): 0 [usable: 0],
	ListTransactions(66): 0 to 1 [usable: 1],
	AllocateProducerIds(67): UNSUPPORTED,
	ConsumerGroupHeartbeat(68): 0 [usable: 0],
	ConsumerGroupDescribe(69): 0 [usable: 0],
	GetTelemetrySubscriptions(71): UNSUPPORTED,
	PushTelemetry(72): UNSUPPORTED,
	ListClientMetricsResources(74): 0 [usable: 0],
	DescribeTopicPartitions(75): 0 [usable: 0],
	ShareGroupHeartbeat(76): UNSUPPORTED,
	ShareGroupDescribe(77): UNSUPPORTED,
	ShareFetch(78): UNSUPPORTED,
	ShareAcknowledge(79): UNSUPPORTED,
	AddRaftVoter(80): 0 [usable: 0],
	RemoveRaftVoter(81): 0 [usable: 0],
	InitializeShareGroupState(83): UNSUPPORTED,
	ReadShareGroupState(84): UNSUPPORTED,
	WriteShareGroupState(85): UNSUPPORTED,
	DeleteShareGroupState(86): UNSUPPORTED,
	ReadShareGroupStateSummary(87): UNSUPPORTED
)
kafka02:9092 (id: 2 rack: null) -> (
	Produce(0): 0 to 11 [usable: 11],
	Fetch(1): 0 to 17 [usable: 17],
	ListOffsets(2): 0 to 9 [usable: 9],
	Metadata(3): 0 to 12 [usable: 12],
	LeaderAndIsr(4): UNSUPPORTED,
	StopReplica(5): UNSUPPORTED,
	UpdateMetadata(6): UNSUPPORTED,
	ControlledShutdown(7): UNSUPPORTED,
	OffsetCommit(8): 0 to 9 [usable: 9],
	OffsetFetch(9): 0 to 9 [usable: 9],
	FindCoordinator(10): 0 to 6 [usable: 6],
	JoinGroup(11): 0 to 9 [usable: 9],
	Heartbeat(12): 0 to 4 [usable: 4],
	LeaveGroup(13): 0 to 5 [usable: 5],
	SyncGroup(14): 0 to 5 [usable: 5],
	DescribeGroups(15): 0 to 5 [usable: 5],
	ListGroups(16): 0 to 5 [usable: 5],
	SaslHandshake(17): 0 to 1 [usable: 1],
	ApiVersions(18): 0 to 4 [usable: 4],
	CreateTopics(19): 0 to 7 [usable: 7],
	DeleteTopics(20): 0 to 6 [usable: 6],
	DeleteRecords(21): 0 to 2 [usable: 2],
	InitProducerId(22): 0 to 5 [usable: 5],
	OffsetForLeaderEpoch(23): 0 to 4 [usable: 4],
	AddPartitionsToTxn(24): 0 to 5 [usable: 5],
	AddOffsetsToTxn(25): 0 to 4 [usable: 4],
	EndTxn(26): 0 to 4 [usable: 4],
	WriteTxnMarkers(27): 0 to 1 [usable: 1],
	TxnOffsetCommit(28): 0 to 4 [usable: 4],
	DescribeAcls(29): 0 to 3 [usable: 3],
	CreateAcls(30): 0 to 3 [usable: 3],
	DeleteAcls(31): 0 to 3 [usable: 3],
	DescribeConfigs(32): 0 to 4 [usable: 4],
	AlterConfigs(33): 0 to 2 [usable: 2],
	AlterReplicaLogDirs(34): 0 to 2 [usable: 2],
	DescribeLogDirs(35): 0 to 4 [usable: 4],
	SaslAuthenticate(36): 0 to 2 [usable: 2],
	CreatePartitions(37): 0 to 3 [usable: 3],
	CreateDelegationToken(38): 0 to 3 [usable: 3],
	RenewDelegationToken(39): 0 to 2 [usable: 2],
	ExpireDelegationToken(40): 0 to 2 [usable: 2],
	DescribeDelegationToken(41): 0 to 3 [usable: 3],
	DeleteGroups(42): 0 to 2 [usable: 2],
	ElectLeaders(43): 0 to 2 [usable: 2],
	IncrementalAlterConfigs(44): 0 to 1 [usable: 1],
	AlterPartitionReassignments(45): 0 [usable: 0],
	ListPartitionReassignments(46): 0 [usable: 0],
	OffsetDelete(47): 0 [usable: 0],
	DescribeClientQuotas(48): 0 to 1 [usable: 1],
	AlterClientQuotas(49): 0 to 1 [usable: 1],
	DescribeUserScramCredentials(50): 0 [usable: 0],
	AlterUserScramCredentials(51): 0 [usable: 0],
	DescribeQuorum(55): 0 to 2 [usable: 2],
	AlterPartition(56): UNSUPPORTED,
	UpdateFeatures(57): 0 to 1 [usable: 1],
	Envelope(58): UNSUPPORTED,
	DescribeCluster(60): 0 to 1 [usable: 1],
	DescribeProducers(61): 0 [usable: 0],
	UnregisterBroker(64): 0 [usable: 0],
	DescribeTransactions(65): 0 [usable: 0],
	ListTransactions(66): 0 to 1 [usable: 1],
	AllocateProducerIds(67): UNSUPPORTED,
	ConsumerGroupHeartbeat(68): 0 [usable: 0],
	ConsumerGroupDescribe(69): 0 [usable: 0],
	GetTelemetrySubscriptions(71): UNSUPPORTED,
	PushTelemetry(72): UNSUPPORTED,
	ListClientMetricsResources(74): 0 [usable: 0],
	DescribeTopicPartitions(75): 0 [usable: 0],
	ShareGroupHeartbeat(76): UNSUPPORTED,
	ShareGroupDescribe(77): UNSUPPORTED,
	ShareFetch(78): UNSUPPORTED,
	ShareAcknowledge(79): UNSUPPORTED,
	AddRaftVoter(80): 0 [usable: 0],
	RemoveRaftVoter(81): 0 [usable: 0],
	InitializeShareGroupState(83): UNSUPPORTED,
	ReadShareGroupState(84): UNSUPPORTED,
	WriteShareGroupState(85): UNSUPPORTED,
	DeleteShareGroupState(86): UNSUPPORTED,
	ReadShareGroupStateSummary(87): UNSUPPORTED
)
```
## 创建主题

```
bin/kafka-topics.sh --bootstrap-server kafka01:9092 --create --topic test-topic --partitions 3 --replication-factor 3
```
## 查看副本

```
bin/kafka-topics.sh --bootstrap-server kafka01:9092 --describe |grep test
```
```

Topic: new-topic	TopicId: AoU-P3g3QYid9Ba5oi_ijQ	PartitionCount: 3	ReplicationFactor: 3	Configs: segment.bytes=1073741824
	Topic: new-topic	Partition: 0	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3	Elr: 	LastKnownElr: 
	Topic: new-topic	Partition: 1	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1	Elr: 	LastKnownElr: 
	Topic: new-topic	Partition: 2	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2	Elr: 	LastKnownElr: 
```
## 创建并消费一个主题消息示例：
单broker
```
bin/kafka-console-producer.sh --broker-list kafka01:9092 --topic test-topic
```

> test message 1
> test message 2

集群broker

```
bin/kafka-console-producer.sh --broker-list kafka01:9092,kafka02:9092,10.0.0.103:9092 --topic test-topic
```
##  kafka-console-consumer.sh 来消费消息：

```
bin/kafka-console-consumer.sh --bootstrap-server 10.0.0.100:9092 --topic test-topic --from-beginning
```

## 删除top主题

```
bin/kafka-topics.sh --bootstrap-server kafka01:9092 --delete --topic test-topic
```
## kafka zook模式
### 

## docker单机 Kafka KRaft 模式
### cat docker-compose.yml
```
version: '3'
services:
  kafka-kraft:
    image: apache/kafka:latest
    hostname: kafka-kraft
    container_name: kafka-kraft
    ports:
      - '9092:9092'
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT_HOST://10.0.0.130:9092,PLAINTEXT://kafka-kraft:29092'
      KAFKA_PROCESS_ROLES: 'broker,controller'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka-kraft:29093'
      KAFKA_LISTENERS: 'CONTROLLER://kafka-kraft:29093,PLAINTEXT_HOST://0.0.0.0:9092,PLAINTEXT://kafka-kraft:29092'
      KAFKA_INTER_BROKER_LISTENER_NAME: 'PLAINTEXT'
      KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_LOG_DIRS: '/tmp/kraft-combined-logs'
```
在Docker镜像官方镜像有部署教程，但均不支持外网访问。如果需要支持外网访问，配置属性PLAINTEXT_HOST必须以公网IP：端口形式表示，不能使用0.0.0.0:9092或//:9092，否则都会无法支持外网访问。
### 使用Python连接kafka实现生产者和消费者功能
#### 生产者

```

import json
from kafka import KafkaProducer
from kafka.admin import KafkaAdminClient, NewTopic
# 创建主题
try:
    admin_client = KafkaAdminClient(bootstrap_servers='43.130.118.124:9092')
    new_topic = NewTopic(
        name='test_topic',
        num_partitions=1,
        replication_factor=1  # 副本因子，应小于或等于Kafka集群中的broker数量
    )
    # 创建 topic
    admin_client.create_topics([new_topic])
except: pass
# 发送消息
producer = KafkaProducer(bootstrap_servers=['43.130.118.124:9092'],
                         value_serializer=lambda v: json.dumps(v).encode('utf-8'))
producer.send('test_topic', 'aaa')
producer.flush()
```
#### 消费者

```

from kafka import KafkaConsumer
consumer = KafkaConsumer('test_topic',
                         bootstrap_servers='43.130.118.124:9092')
for message in consumer:
    print(message.value.decode())
```
## 在K8S中运行KRaft模式Kafka集群 （helm）
KRaft kafka on K8S的部署方案: Bitnami Kafka Helm chart

https://github.com/bitnami/charts/tree/main/bitnami/kafka
### 添加源
```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update bitnami 
helm search repo bitnami/kafka -l|more
# 本次安装kafka3.8.0版本
```
### 修改配置和说明
```
# vi kafka.yaml 
image:
  registry: registry.cn-shenzhen.aliyuncs.com
  repository: starsl/kafka #国内可使用仓库与镜像
  tag: 3.8
listeners:
  client:
    protocol: PLAINTEXT #关闭访问认证
  controller:
    protocol: PLAINTEXT #关闭访问认证
  interbroker:
    protocol: PLAINTEXT #关闭访问认证
  external:
    protocol: PLAINTEXT #关闭访问认证
controller:
  replicaCount: 3 #副本数
  controllerOnly: false #controller+broker共用模式
  heapOpts: -Xmx4096m -Xms2048m #KAFKA JVM
  resources:
    limits:
      cpu: 4 
      memory: 8Gi
    requests:
      cpu: 500m
      memory: 512Mi
  affinity: #仅部署在master节点,不限制可删除
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
          - matchExpressions:
              - key: node-role.kubernetes.io/master
                operator: Exists
          - matchExpressions:
              - key: node-role.kubernetes.io/control-plane
                operator: Exists
  tolerations: #仅部署在master节点,不限制可删除
    - operator: Exists
      effect: NoSchedule
    - operator: Exists
      effect: NoExecute
  persistence:
    storageClass: "local-path" #存储卷类型
    size: 100Gi #每个pod的存储大小
externalAccess:
  enabled: true #开启外部访问
  controller:
    service:
      type: NodePort #使用NodePort方式
      nodePorts:
        - 30091 #对外端口
        - 30092 #对外端口
        - 30093 #对外端口
      useHostIPs: true #使用宿主机IP
```
### 使用helm部署KAFKA

```
helm install kafka bitnami/kafka -f kafka.yaml --dry-run
helm install kafka bitnami/kafka -f kafka.yaml
```
### 调用

```
K8S内部访问

kafka-controller-headless.default:9092

kafka-controller-0.kafka-controller-headless.default:9092
kafka-controller-1.kafka-controller-headless.default:9092
kafka-controller-2.kafka-controller-headless.default:9092
K8S外部访问

# node ip +设置的nodeport端口,注意端口对应的节点的ip
10.118.70.93:30091    
10.118.70.92:30092    
10.118.70.91:30093
# 从pod的配置中查找外部访问信息
kubectl exec -it kafka-controller-0 -- cat /opt/bitnami/kafka/config/server.properties | grep advertised.listeners
```
### 测试

```
创建测试pod
kubectl run kafka-client --restart='Never' --image registry.cn-shenzhen.aliyuncs.com/starsl/kafka:3.8 --namespace default --command -- sleep infinity

生产消息
# 进入pod
kubectl exec --tty -i kafka-client --namespace default -- bash
kafka-console-producer.sh \
  --broker-list kafka-controller-0.kafka-controller-headless.default.svc.cluster.local:9092,kafka-controller-1.kafka-controller-headless.default.svc.cluster.local:9092,kafka-controller-2.kafka-controller-headless.default.svc.cluster.local:9092 \
  --topic test

消费消息
# 进入pod
kubectl exec --tty -i kafka-client --namespace default -- bash
kafka-console-consumer.sh \
  --bootstrap-server kafka.default.svc.cluster.local:9092 \
  --topic test \
  --from-beginning
```
## 在 k8s 上手撸 KRaft 模式 Kafka 集群
https://mp.weixin.qq.com/s/49MgoQHW73eY1tdHspTTpA
### 1 开始前准备
使用 NFS 作为 k8s 集群的持久化存储
Kafka 集群所有资源部署在命名空间opsxlab内

### 2.1 创建 Secret
#### 2.1.1创建管理 Kafka 集群各 Listener 所需密码的保密字典
明文密码必须使用 base64 加密，
```
echo -n "PleaseChangeMe" | base64 -w0
```
生产环境请生成不同的密码。

```
# vim kafka-sasl-passwords-secret.yaml

kind: Secret
apiVersion: v1
metadata:
  name: kafka-sasl-passwords
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
data:
  client-passwords: UGxlYXNlQ2hhbmdlTWU=
  controller-password: UGxlYXNlQ2hhbmdlTWU=
  inter-broker-password: UGxlYXNlQ2hhbmdlTWU=
type: Opaque
```


#### 2.1.2创建 Kafka 集群 UUID 保密字典

```
# 使用下面的命令，创建一个临时 Pod，生成 UUID 后自动删除。

kubectl run app-kafka-client --rm -i --image registry.opsxlab.cn:8443/bitnami/kafka:3.6.2 -n opsxlab -- /opt/bitnami/kafka/bin/kafka-storage.sh random-uuid

RpOTPIfMRTiPpmCYJHF9KQ
```
将生成的明文 UUID 使用 base64 加密，echo -n "RpOTPIfMRTiPpmCYJHF9KQ" | base64 -w0

```
# vim kafka-kraft-cluster-id.yaml
kind: Secret
apiVersion: v1
metadata:
  name: kafka-kraft-cluster-id
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
data:
  kraft-cluster-id: UnBPVFBJZk1SVGlQcG1DWUpIRjlLUQ==
type: Opaque
```
#### 2.1.3 创建资源

```
kubectl apply -f kafka-sasl-passwords-secret.yaml -n opsxlab
kubectl apply -f kafka-kraft-cluster-id.yaml -n opsxlab
```
#### 2.1.4 验证资源

```
$ kubectl get secret -n opsxlab
NAME                     TYPE     DATA   AGE
kafka-kraft-cluster-id   Opaque   1      5s
kafka-sasl-passwords     Opaque   3      6s
```

### 2.2 创建服务
服务规划说明：

3个 Kafka 节点，采用 NodePort 方式在 Kubernetes 集群外发布 Kafka 服务
3个 Kafka 节点，共用一个 Headless 服务，作用是给 Internal 和 Controller 两个 LISTENERS 提供内部域名。
```
# 创建 HeadLess 服务
# vim kafka-controller-headless.yaml
kind: Service
apiVersion: v1
metadata:
  name: kafka-controller-hs
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
spec:
  ports:
    - name: tcp-internal
      protocol: TCP
      port: 9092
      targetPort: internal
    - name: tcp-controller
      protocol: TCP
      port: 9093
      targetPort: controller
  selector:
    app.kubernetes.io/instance: app-kafka
  clusterIP: None
  type: ClusterIP
```

```
#创建 kafka-controller节点1的 NodePort 服务
#vim kafka-controller-0-external.yaml
kind: Service
apiVersion: v1
metadata:
  name: kafka-controller-0-external
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
spec:
  ports:
    - name: tcp-external
      protocol: TCP
      port: 9094
      targetPort: 9094
      nodePort: 31211
  selector:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-0
  type: NodePort
```

```
# 创建 kafka-controller节点2 的 NodePort 服务
# vim kafka-controller-1-external.yaml

kind: Service
apiVersion: v1
metadata:
  name: kafka-controller-1-external
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
spec:
  ports:
    - name: tcp-external
      protocol: TCP
      port: 9094
      targetPort: 9094
      nodePort: 31212
  selector:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-1
  type: NodePort

```
```
# 创建 kafka-controller节点3 的 NodePort 服务
# vim kafka-controller-2-external.yaml

kind: Service
apiVersion: v1
metadata:
  name: kafka-controller-2-external
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka
spec:
  ports:
    - name: tcp-external
      protocol: TCP
      port: 9094
      targetPort: 9094
      nodePort: 31213
  selector:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-2
  type: NodePort

```
#### 2.2.1 验证

```
kubectl apply -f kafka-controller-headless.yaml -n opsxlab
kubectl apply -f kafka-controller-0-external.yaml -n opsxlab
kubectl apply -f kafka-controller-1-external.yaml -n opsxlab
kubectl apply -f kafka-controller-2-external.yaml -n opsxlab
```

```
$ kubectl get svc -n opsxlab
NAME                          TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)             AGE
kafka-controller-0-external   NodePort    10.233.1.92    <none>        9094:31211/TCP      8s
kafka-controller-1-external   NodePort    10.233.18.62   <none>        9094:31212/TCP      8s
kafka-controller-2-external   NodePort    10.233.38.37   <none>        9094:31213/TCP      8s
kafka-controller-hs           ClusterIP   None           <none>        9092/TCP,9093/TCP   8s
```

## 2.3 创建 Kafka 集群
使用 StatefulSet 部署 Kafka 集群，3个 Kafka 节点使用内容大部分相同的配置文件，必须修改的参数如下：

KAFKA_CFG_ADVERTISED_LISTENERS： 修改 EXTERNAL 对应的 IP 地址
KAFKA_HEAP_OPTS：根据资源和并发需求调整

```
# 创建节点1 资源清单
# vim kafka-controller-0-sts.yaml

kind: StatefulSet
apiVersion: apps/v1
metadata:
  name: kafka-controller-0
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-0
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/instance: app-kafka
      app.kubernetes.io/name: kafka-controller-0
  template:
    metadata:
      labels:
        app.kubernetes.io/instance: app-kafka
        app.kubernetes.io/name: kafka-controller-0
    spec:
      containers:
        - name: kafka
          image: 'registry.opsxlab.cn:8443/bitnami/kafka:3.6.2'
          ports:
            - name: intelrnal
              containerPort: 9092
              protocol: TCP
            - name: controller
              containerPort: 9093
              protocol: TCP
            - name: external
              containerPort: 9094
              protocol: TCP
          env:
            - name: BITNAMI_DEBUG
              value: 'false'
            - name: HOST_IP
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: status.hostIP
            - name: KAFKA_HEAP_OPTS
              value: '-Xmx2048m -Xms1024m'
            - name: KAFKA_KRAFT_CLUSTER_ID
              valueFrom:
                secretKeyRef:
                  name: kafka-kraft-cluster-id
                  key: kraft-cluster-id
            - name: KAFKA_CLIENT_USERS
              value: user1
            - name: KAFKA_CLIENT_PASSWORDS
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: client-passwords
            - name: KAFKA_INTER_BROKER_USER
              value: inter_broker_user
            - name: KAFKA_INTER_BROKER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: inter-broker-password
            - name: KAFKA_CONTROLLER_USER
              value: controller_user
            - name: KAFKA_CONTROLLER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: controller-password
            - name: KAFKA_CFG_SASL_MECHANISM_CONTROLLER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_SASL_MECHANISM_INTER_BROKER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_NODE_ID
              value: '0'
            - name: KAFKA_CFG_PROCESS_ROLES
              value: 'controller,broker'
            - name: KAFKA_CFG_CONTROLLER_QUORUM_VOTERS
              value: >-
                0@kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,1@kafka-controller-1-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,2@kafka-controller-2-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093
            - name: KAFKA_CFG_LISTENERS
              value: 'INTERNAL://:9092,CONTROLLER://:9093,EXTERNAL://:9094'
            - name: KAFKA_CFG_ADVERTISED_LISTENERS
              value: >-
                INTERNAL://kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092,EXTERNAL://192.168.9.121:31211
            - name: KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP
              value: >-
                INTERNAL:SASL_PLAINTEXT,CONTROLLER:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT
            - name: KAFKA_CFG_CONTROLLER_LISTENER_NAMES
              value: CONTROLLER
            - name: KAFKA_CFG_INTER_BROKER_LISTENER_NAME
              value: INTERNAL
            - name: KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR
              value: '2'
          resources:
            limits:
              cpu: '1'
              memory: 2Gi
            requests:
              cpu: 50m
              memory: 512Mi
          volumeMounts:
            - name: data
              mountPath: /bitnami/kafka
          livenessProbe:
            exec:
              command:
                - pgrep
                - '-f'
                - kafka
            initialDelaySeconds: 10
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 3
          readinessProbe:
            tcpSocket:
              port: controller
            initialDelaySeconds: 5
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 6
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          imagePullPolicy: IfNotPresent
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 1
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app.kubernetes.io/instance: app-kafka
                    app.kubernetes.io/name: kafka
                topologyKey: kubernetes.io/hostname
  volumeClaimTemplates:
    - kind: PersistentVolumeClaim
      apiVersion: v1
      metadata:
        name: data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 10Gi
        storageClassName: nfs-sc
        volumeMode: Filesystem
  serviceName: kafka-controller-hs
```

```
# 创建节点2 资源清单
vim kafka-controller-1-sts.yaml

kind: StatefulSet
apiVersion: apps/v1
metadata:
  name: kafka-controller-1
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-1
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/instance: app-kafka
      app.kubernetes.io/name: kafka-controller-1
  template:
    metadata:
      labels:
        app.kubernetes.io/instance: app-kafka
        app.kubernetes.io/name: kafka-controller-1
    spec:
      containers:
        - name: kafka
          image: 'registry.opsxlab.cn:8443/bitnami/kafka:3.6.2'
          ports:
            - name: intelrnal
              containerPort: 9092
              protocol: TCP
            - name: controller
              containerPort: 9093
              protocol: TCP
            - name: external
              containerPort: 9094
              protocol: TCP
          env:
            - name: BITNAMI_DEBUG
              value: 'false'
            - name: HOST_IP
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: status.hostIP
            - name: KAFKA_HEAP_OPTS
              value: '-Xmx2048m -Xms1024m'
            - name: KAFKA_KRAFT_CLUSTER_ID
              valueFrom:
                secretKeyRef:
                  name: kafka-kraft-cluster-id
                  key: kraft-cluster-id
            - name: KAFKA_CLIENT_USERS
              value: user1
            - name: KAFKA_CLIENT_PASSWORDS
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: client-passwords
            - name: KAFKA_INTER_BROKER_USER
              value: inter_broker_user
            - name: KAFKA_INTER_BROKER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: inter-broker-password
            - name: KAFKA_CONTROLLER_USER
              value: controller_user
            - name: KAFKA_CONTROLLER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: controller-password
            - name: KAFKA_CFG_SASL_MECHANISM_CONTROLLER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_SASL_MECHANISM_INTER_BROKER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_NODE_ID
              value: '1'
            - name: KAFKA_CFG_PROCESS_ROLES
              value: 'controller,broker'
            - name: KAFKA_CFG_CONTROLLER_QUORUM_VOTERS
              value: >-
                0@kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,1@kafka-controller-1-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,2@kafka-controller-2-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093
            - name: KAFKA_CFG_LISTENERS
              value: 'INTERNAL://:9092,CONTROLLER://:9093,EXTERNAL://:9094'
            - name: KAFKA_CFG_ADVERTISED_LISTENERS
              value: >-
                INTERNAL://kafka-controller-1-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092,EXTERNAL://192.168.9.121:31212
            - name: KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP
              value: >-
                INTERNAL:SASL_PLAINTEXT,CONTROLLER:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT
            - name: KAFKA_CFG_CONTROLLER_LISTENER_NAMES
              value: CONTROLLER
            - name: KAFKA_CFG_INTER_BROKER_LISTENER_NAME
              value: INTERNAL
            - name: KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR
              value: '2'
          resources:
            limits:
              cpu: '1'
              memory: 2Gi
            requests:
              cpu: 50m
              memory: 512Mi
          volumeMounts:
            - name: data
              mountPath: /bitnami/kafka
          livenessProbe:
            exec:
              command:
                - pgrep
                - '-f'
                - kafka
            initialDelaySeconds: 10
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 3
          readinessProbe:
            tcpSocket:
              port: controller
            initialDelaySeconds: 5
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 6
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          imagePullPolicy: IfNotPresent
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 1
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app.kubernetes.io/instance: app-kafka
                    app.kubernetes.io/name: kafka
                topologyKey: kubernetes.io/hostname
  volumeClaimTemplates:
    - kind: PersistentVolumeClaim
      apiVersion: v1
      metadata:
        name: data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 10Gi
        storageClassName: nfs-sc
        volumeMode: Filesystem
  serviceName: kafka-controller-hs

```

```
# 创建节点3 资源清单
vim kafka-controller-2-sts.yaml

kind: StatefulSet
apiVersion: apps/v1
metadata:
  name: kafka-controller-2
  labels:
    app.kubernetes.io/instance: app-kafka
    app.kubernetes.io/name: kafka-controller-2
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/instance: app-kafka
      app.kubernetes.io/name: kafka-controller-2
  template:
    metadata:
      labels:
        app.kubernetes.io/instance: app-kafka
        app.kubernetes.io/name: kafka-controller-2
    spec:
      containers:
        - name: kafka
          image: 'registry.opsxlab.cn:8443/bitnami/kafka:3.6.2'
          ports:
            - name: intelrnal
              containerPort: 9092
              protocol: TCP
            - name: controller
              containerPort: 9093
              protocol: TCP
            - name: external
              containerPort: 9094
              protocol: TCP
          env:
            - name: BITNAMI_DEBUG
              value: 'false'
            - name: HOST_IP
              valueFrom:
                fieldRef:
                  apiVersion: v1
                  fieldPath: status.hostIP
            - name: KAFKA_HEAP_OPTS
              value: '-Xmx2048m -Xms1024m'
            - name: KAFKA_KRAFT_CLUSTER_ID
              valueFrom:
                secretKeyRef:
                  name: kafka-kraft-cluster-id
                  key: kraft-cluster-id
            - name: KAFKA_CLIENT_USERS
              value: user1
            - name: KAFKA_CLIENT_PASSWORDS
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: client-passwords
            - name: KAFKA_INTER_BROKER_USER
              value: inter_broker_user
            - name: KAFKA_INTER_BROKER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: inter-broker-password
            - name: KAFKA_CONTROLLER_USER
              value: controller_user
            - name: KAFKA_CONTROLLER_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kafka-sasl-passwords
                  key: controller-password
            - name: KAFKA_CFG_SASL_MECHANISM_CONTROLLER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_SASL_MECHANISM_INTER_BROKER_PROTOCOL
              value: PLAIN
            - name: KAFKA_CFG_NODE_ID
              value: '2'
            - name: KAFKA_CFG_PROCESS_ROLES
              value: 'controller,broker'
            - name: KAFKA_CFG_CONTROLLER_QUORUM_VOTERS
              value: >-
                0@kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,1@kafka-controller-1-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093,2@kafka-controller-2-0.kafka-controller-hs.opsxlab.svc.cluster.local:9093
            - name: KAFKA_CFG_LISTENERS
              value: 'INTERNAL://:9092,CONTROLLER://:9093,EXTERNAL://:9094'
            - name: KAFKA_CFG_ADVERTISED_LISTENERS
              value: >-
                INTERNAL://kafka-controller-2-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092,EXTERNAL://192.168.9.121:31213
            - name: KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP
              value: >-
                INTERNAL:SASL_PLAINTEXT,CONTROLLER:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT
            - name: KAFKA_CFG_CONTROLLER_LISTENER_NAMES
              value: CONTROLLER
            - name: KAFKA_CFG_INTER_BROKER_LISTENER_NAME
              value: INTERNAL
            - name: KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: '3'
            - name: KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR
              value: '2'
          resources:
            limits:
              cpu: '1'
              memory: 2Gi
            requests:
              cpu: 50m
              memory: 512Mi
          volumeMounts:
            - name: data
              mountPath: /bitnami/kafka
          livenessProbe:
            exec:
              command:
                - pgrep
                - '-f'
                - kafka
            initialDelaySeconds: 10
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 3
          readinessProbe:
            tcpSocket:
              port: controller
            initialDelaySeconds: 5
            timeoutSeconds: 5
            periodSeconds: 10
            successThreshold: 1
            failureThreshold: 6
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          imagePullPolicy: IfNotPresent
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 1
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app.kubernetes.io/instance: app-kafka
                    app.kubernetes.io/name: kafka
                topologyKey: kubernetes.io/hostname
  volumeClaimTemplates:
    - kind: PersistentVolumeClaim
      apiVersion: v1
      metadata:
        name: data
      spec:
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 10Gi
        storageClassName: nfs-sc
        volumeMode: Filesystem
  serviceName: kafka-controller-hs
```
### 2.3.1 验证pod


```
kubectl apply -f kafka-controller-0-sts.yaml -n opsxlab
kubectl apply -f kafka-controller-1-sts.yaml -n opsxlab
kubectl apply -f kafka-controller-2-sts.yaml -n opsxlab
```

```

$ kubectl get sts,pod -n opsxlab
NAME                                  READY   AGE
statefulset.apps/kafka-controller-0   1/1     25s
statefulset.apps/kafka-controller-1   1/1     25s
statefulset.apps/kafka-controller-2   1/1     24s

NAME                       READY   STATUS    RESTARTS   AGE
pod/kafka-controller-0-0   1/1     Running   0          24s
pod/kafka-controller-1-0   1/1     Running   0          24s
pod/kafka-controller-2-0   1/1     Running   0          23s
```

### 2.3.1 验证测试 Kafka 服务可用性
###  2.3.1.2 k8s 集群内部验证
```
kubectl run opsxlab-kafka-client --restart='Never' --image registry.opsxlab.cn:8443/bitnami/kafka:3.6.2 --namespace opsxlab --command -- sleep infinity

# 生成 client.properties
cat << EOF > /tmp/client.properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="user1" password="PleaseChangeMe";
EOF

# 复制到测试容器app-kafka-client内部
kubectl cp --namespace opsxlab /tmp/client.properties opsxlab-kafka-client:/tmp/client.properties
打开测试 Pod 终端
kubectl exec --tty -i opsxlab-kafka-client --namespace opsxlab -- bash


# 创建主题
kafka-topics.sh --bootstrap-server kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 --create --topic test-topic --partitions 3 --replication-factor 3 --command-config /tmp/client.properties

# 查看副本
$ kafka-topics.sh --bootstrap-server kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 --topic test-topic --describe --command-config /tmp/client.properties
Topic: test-topic       TopicId: yNWQQ6yKSBeLmvVUFf2IVw PartitionCount: 3       ReplicationFactor: 3    Configs:
        Topic: test-topic       Partition: 0    Leader: 0       Replicas: 0,1,2 Isr: 0,1,2
        Topic: test-topic       Partition: 1    Leader: 1       Replicas: 1,2,0 Isr: 1,2,0
        Topic: test-topic       Partition: 2    Leader: 2       Replicas: 2,0,1 Isr: 2,0,1

# 执行命令，生产数据

kafka-console-producer.sh \
  --broker-list kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 \
  --topic test-topic --producer.config /tmp/client.properties

# 再打开一个测试 Pod 终端，消费数据
kafka-console-consumer.sh \
  --bootstrap-server kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 \
  --topic test-topic \
  --from-beginning --consumer.config /tmp/client.properties
```
生产并消费数据测试
在生产者一侧随便输入测试数据，观察消费者一侧是否正确收到信息。

```
生产者侧：

I have no name!@opsxlab-kafka-client:/$ kafka-console-producer.sh \
  --broker-list kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 \
  --topic test-topic --producer.config /tmp/client.properties
>cluster kafka test 1
>cluster kafka test 2
>cluster kafka test 3

消费者侧：

I have no name!@opsxlab-kafka-client:/$ kafka-console-consumer.sh \
  --bootstrap-server kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 \
  --topic test-topic \
  --from-beginning --consumer.config /tmp/client.properties
cluster kafka test 1
cluster kafka test 2
cluster kafka test 3
```
### 2.3.1.3 k8s 集群外部验证
为了更严谨的测试 Kafka 在 k8s 集群外的可用性，我在 k8s 集群外找了一台机器，安装 JDK 和 Kafka。安装方式上 JDK 选择了 Yum 安装openjdk，Kafka 则选用了官方提供的3.9.0最新版本的二进制包。

实际测试时还可以选择 Docker 镜像或是在 k8s 集群上再创建一个 Pod，测试时连接 k8s 节点的宿主机 IP 和 NodePort。

#### 2.3.1.3.1 准备外部测试环境

```
# 安装 JDK
yum install java-1.8.0-openjdk

# 下载 Kafka
cd /srv
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.9.0.tgz

# 解压
tar xvf kafka_2.13-3.9.0.tgz
cd /srv/kafka_2.13-3.9.0/bin
```

#### 2.3.1.3.2 获取 Kafka 外部访问配置信息
本文使用一个 Master 节点，作为 Kafka NodePort 的 IP，实际使用中建议使用多个 Worker 节点，每个 Pod 对应一个 Worker节点IP。

下面测试的 Broker  Server 地址使用192.168.9.121:31211

生成 client.properties

```
cat << EOF > /tmp/client.properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="user1" password="PleaseChangeMe";
EOF
```
#### 2.3.1.3.3 外部节点连接 Kafka 测试
跟 k8s 集群内部验证测试过程一样，打开两个终端，运行生产者和消费者脚本。执行下面的命令验证测试(细节略过，直接上结果)。

```
# 外部生产者侧：

$ ./kafka-console-producer.sh --broker-list 192.168.9.121:31211 --topic test-topic --producer.config /tmp/client.properties
>external kafka test 10
>external kafka test 20
>external kafka test 30

# 外部消费者侧：

$ ./kafka-console-consumer.sh --bootstrap-server 192.168.9.121:31211 --topic test-topic --from-beginning --consumer.config /tmp/client.properties

external kafka test 10
external kafka test 20
external kafka test 30
cluster kafka test 1
cluster kafka test 2
cluster kafka test 3
```
k8s集群内的应用创建的topic  我k8s集群外的应用消费topic

```
I have no name!@opsxlab-kafka-client:/$ kafka-console-consumer.sh \
  --bootstrap-server kafka-controller-0-0.kafka-controller-hs.opsxlab.svc.cluster.local:9092 \
  --topic test-topic \
  --from-beginning --consumer.config /tmp/client.properties
cluster kafka test 1
cluster kafka test 2
cluster kafka test 3
external kafka test 10
external kafka test 20
external kafka test 30
删除测试 Topic
./kafka-topics.sh --bootstrap-server 192.168.9.121:31211 --delete --topic test-topic --command-config /tmp/client.properties
查看 Topic
./kafka-topics.sh --bootstrap-server 192.168.9.121:31211 --list --command-config /tmp/client.properti
```


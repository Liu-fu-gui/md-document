# 1.安装 Logstash

```
# 安装
wget https://artifacts.elastic.co/downloads/logstash/logstash-7.10.2.rpm && \


sudo yum install -y ./logstash-7.10.2.rpm && \
sudo systemctl enable --now logstash && \
sudo systemctl status logstash	
## Logstash 的配置文件通常位于 /etc/logstash/conf.d/
```

```
需求，收集本地 /deploy/logs/app/zj-sae-api/s-sae-management/ 文件到es
设置
```





## 2. Logstash 配置文件示例(elk 收集本地)

```
input {
  file {
    path => "/deploy/logs/app/zj-sae-api/s-sae-management/*.log"
    start_position => "beginning"
    sincedb_path => "/var/lib/logstash/sincedb/s-sae-management.log" # 普通日志的 sincedb 文件
    codec => "plain"
    type => "log"
    sincedb_write_interval => 15
  }

  file {
    path => "/deploy/logs/app/zj-sae-api/s-sae-management/*.err.log"
    start_position => "beginning"
    sincedb_path => "/var/lib/logstash/sincedb/s-sae-management-err.log" # 错误日志的 sincedb 文件
    codec => "plain"
    type => "error_log"
    sincedb_write_interval => 15
  }
}

filter {
  if [type] == "log" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel} %{NUMBER:pid} --- \[%{DATA:thread}\] %{DATA:logger}:%{NUMBER:linenumber} : %{GREEDYDATA:message}" }
    }
  }

  if [type] == "error_log" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel} %{NUMBER:pid} --- \[%{DATA:thread}\] %{DATA:logger}:%{NUMBER:linenumber} : %{GREEDYDATA:message}" }
    }
  }

  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
    target => "@timestamp"
  }
}

output {
  if [type] == "log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-management-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false 
    }
  }

  if [type] == "error_log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-management-error-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false 
    }
  }

  stdout { codec => rubydebug }
}
```

## 3. 测试 Logstash 配置

```
/usr/share/logstash/bin/logstash -f /etc/logstash/conf.d/logstash.conf --config.test_and_exit
systemctl restart logstash
```

## 4. 查看 Logstash 日志

```
tail -f /var/log/logstash/logstash-plain.log
```





## 5. Logstash 配置文件示例(elfk 收集本地)

```
# opt下安装filebeat包
wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-7.17.0-x86_64.rpm
rpm -ivh filebeat-7.17.0-x86_64.rpm 
```

### filebeat.yml(按照文件配置)

```
filebeat.inputs:
- type: log
  paths:
    - /deploy/logs/app/zj-sae-api/s-sae-management/*.log
  fields:
    type: "log"
    project: "s-sae-management"
    rizhi_ip: "192.168.7.147"
  fields_under_root: true

- type: log
  paths:
    - /deploy/logs/app/zj-sae-api/s-sae-management/*.err.log
  fields:
    type: "error_log"
    project: "s-sae-management"
    rizhi_ip: "192.168.7.147"
  fields_under_root: true

- type: log
  paths:
    - /deploy/logs/app/zj-sae-api/s-sae-study/*.log
  fields:
    type: "log"
    project: "s-sae-study"
    rizhi_ip: "192.168.7.147"
  fields_under_root: true

- type: log
  paths:
    - /deploy/logs/app/zj-sae-api/s-sae-study/*.err.log
  fields:
    type: "error_log"
    project: "s-sae-study"
    rizhi_ip: "192.168.7.147"
  fields_under_root: true

output.logstash:
  hosts: ["192.168.7.147:5044"]
```

### 2.filebeat.yml（优化) 不可用 待优化

```
filebeat.inputs:
- type: log
  paths:
    - /deploy/logs/app/zj-sae-api/*/*.log
    - /deploy/logs/app/zj-sae-api/*/*.err.log
  fields:
    rizhi_ip: "192.168.7.147"
    type: "log"  # 默认类型
  fields_under_root: true
  processors:
    - if:
        contains:
          log.file.path: ".err.log"
      then:
        - add_fields:
            fields:
              type: "error_log"  # 如果是错误日志，覆盖默认类型
    - add_fields:
        fields:
          project: "${path.project}"  # 动态设置 project 字段
    - dissect:
        tokenizer: "/deploy/logs/app/zj-sae-api/%{project}/%{logfile}"
        field: "log.file.path"
        target_prefix: "path"  # 将解析结果存储到 path 命名空间

output.logstash:
  hosts: ["192.168.7.147:5044"]
```



### logstash.conf

```

input {
  beats {
    port => 5044  # 监听 5044 端口，接收来自 Filebeat 的数据
  }
}

filter {
  # 处理普通日志
  if [type] == "log" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel} %{NUMBER:pid} --- \[%{DATA:thread}\] %{DATA:logger}:%{NUMBER:linenumber} : %{GREEDYDATA:message}" }
    }
  }

  # 处理错误日志
  if [type] == "error_log" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel} %{NUMBER:pid} --- \[%{DATA:thread}\] %{DATA:logger}:%{NUMBER:linenumber} : %{GREEDYDATA:message}" }
    }
  }

  # 解析时间戳
  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
    target => "@timestamp"
  }
}

output {
  # 输出 s-sae-management 普通日志到 Elasticsearch
  if [project] == "s-sae-management" and [type] == "log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-management-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false
    }
  }

  # 输出 s-sae-management 错误日志到 Elasticsearch
  if [project] == "s-sae-management" and [type] == "error_log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-management-error-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false
    }
  }

  # 输出 s-sae-study 普通日志到 Elasticsearch
  if [project] == "s-sae-study" and [type] == "log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-study-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false
    }
  }

  # 输出 s-sae-study 错误日志到 Elasticsearch
  if [project] == "s-sae-study" and [type] == "error_log" {
    elasticsearch {
      hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
      user => "admin"
      password => "zjtvu_kc!5"
      ssl => true
      cacert => "/etc/logstash/CloudSearchService.cer"
      index => "s-sae-study-error-logs-%{+YYYY.MM.dd}"
      ilm_enabled => false
    }
  }

  # 输出到控制台（用于调试）
  stdout { codec => rubydebug }
}
```

logstash。conf 优化

```
[root@y03-0729938 /etc/filebeat]$ cat /etc/logstash/conf.d/logstash.conf
input {
  beats {
    port => 5044  # 监听 5044 端口，接收来自 Filebeat 的数据
  }
}

filter {
  # 处理普通日志和错误日志
  if [type] == "log" or [type] == "error_log" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel} %{NUMBER:pid} --- \[%{DATA:thread}\] %{DATA:logger}:%{NUMBER:linenumber} : %{GREEDYDATA:message}" }
    }
  }

  # 解析时间戳
  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
    target => "@timestamp"
  }
}

output {
  elasticsearch {
    hosts => ["192.168.7.184:9200", "192.168.7.141:9200", "192.168.7.24:9200"]
    user => "admin"
    password => "zjtvu_kc!5"
    ssl => true
    cacert => "/etc/logstash/CloudSearchService.cer"
    index => "%{[project]}-%{[type]}-%{+YYYY.MM.dd}"    
    ilm_enabled => false
  }

  # 输出到控制台（用于调试）
  stdout { codec => rubydebug }
}

```


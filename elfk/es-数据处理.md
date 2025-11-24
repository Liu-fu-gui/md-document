
### 参考文献：
https://support.huaweicloud.com/trouble-css/css_10_0042.html#ZH-CN_TOPIC_0000001950381768__li182004282617
## 1. 查看所有索引
通过以下命令，你可以列出 Elasticsearch 中所有的索引：

```
GET /_cat/indices?v
```
输出示例：


```
health status index              uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   logs-2024-12-01     P3g1TeDQfqKBOhhVdTzyQg   1   1    15000          0      1.2gb          600mb
yellow open   logs-2024-12-02     9wM9Ht9kS7gROqNfyzE-4A   1   1    12000          0      1.0gb          500mb
green  open   .kibana             G5MKmtI3ShGUkHZbYZ-Qhg   1   0    1200           0      10mb     
```
- index：索引名称
- uuid：索引的唯一标识符
- pri：主分片数量
- rep：副本分片数量
- docs.count：文档数量
- docs.deleted：已删除但未清除的文档数量
- store.size：索引的数据存储大小
- pri.store.size：主分片存储的大小


## 2. 查看特定索引
如果你想查看某个特定索引的详细信息，可以指定索引名称。例如，查看 logs-* 模式的所有索引：


```
GET /_cat/indices/logs-*?v
```
## 3. 查看索引的详细映射和设置
如果你想查看某个索引的映射（mapping）或设置（settings），可以分别使用以下命令：

### 3.1.查看映射（mapping）：

```
GET /logs-*/_mapping
```
### 3.2.查看设置（settings）：

```
GET /logs-*/_settings
```
## 4. 过滤和分页
如果索引较多，你可以使用 grep 等工具进行过滤，或者使用分页显示索引。比如，只列出以 logs- 开头的索引：


```
GET /_cat/indices/logs-*?v
```

分页显示索引的命令也可以如下：


```
GET /_cat/indices?v&h=index,status,docs.count,store.size&page=1
```

## 5. 查看索引生命周期管理 (ILM) 状态
如果你正在使用 ILM 策略，并希望查看每个索引的生命周期状态，可以执行以下命令：


```
GET /logs-*/_ilm/explain
```

这个命令会返回所有匹配的索引，并显示它们的生命周期阶段（例如，热阶段、删除阶段等）。

## 6. 查看系统索引
Elasticsearch 还会创建一些系统索引（如 .kibana、.security 等），它们存储了系统配置、用户数据等。如果你想查看这些系统索引，可以执行：


```
GET /_cat/indices/.kibana*?v
```


## 案例1 es创建ilm，删除90天之前的索引
参考文档
https://support.huaweicloud.com/usermanual-css/css_01_0021.html

###  1.1 先创建生命周期  保留90天策略
```
## 生命周期策略
PUT _opendistro/_ism/policies/rollover_workflow
{
  "policy": {
    "description": "Delete indices older than 90 days",
    "default_state": "hot",
    "states": [
      {
        "name": "hot",
        "actions": [
          {
          }
        ],
        "transitions": [
          {
            "state_name": "delete",
            "conditions": {
              "min_index_age": "90d"
            }
          }
        ]
      },
      {
        "name": "delete",
        "actions": [
          {
            "delete": {}
          }
        ]
      }
    ]
  }
}
```
### 1.2 ilm关联索引

```
## 索引模板关联生命周期策略id

PUT _template/template_qmzs
{
  "index_patterns": "qmzs*",
  "settings": {
    "number_of_replicas": 1,
    "number_of_shards": 1,
    "opendistro.index_state_management.policy_id": "rollover_workflow"
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text"
      }
    }
  }
}
```
### 1.3 关联历史模板，立刻删除90天前的索引
```
## 关联历史模板 按照ilm策略删除

POST _opendistro/_ism/add/qmzs*
{
  "policy_id": "rollover_workflow"
}


POST _opendistro/_ism/add/open*
{
  "policy_id": "rollover_workflow"
}

```
### 1.4 前提是logstash input中定义了别名

![20241202163631](https://liu-fu-gui.github.io/myimg/halo/20241202163631.png)
![20241202163728](https://liu-fu-gui.github.io/myimg/halo/20241202163728.png)
### 如果没有定义，就要手更

```
## 创建新索引-关联写别名
POST /_aliases
{
  "actions": [
    {
      "add": {
        "index": "open-course-record-2024.12.02",
        "alias": "log_alias",
        "is_write_index": true
      }
    }
  ]
}
## 索引别名查询
GET /_alias/log_alias


## 创建索引别名-关联写别名
PUT open-course-record-2024.12.02
{
  "aliases": {
    "log_alias": {
      "is_write_index": true
    }
  }
}
```







## es脚本清理

匹配前缀为指定名字（如 `qmzs-*` 或 `open-course-*`）的索引

解析索引名称中的日期（假设格式为 `YYYY.MM.DD`）

仅保留最近 7 天内的索引，删除其余

使用 curl 调用 Elasticsearch HTTP API

```
#!/bin/bash

# 配置项
ES_HOST="http://192.168.7.126:9200"
ES_USER="admin"
ES_PASS="zjtvu_kc!5"
RETENTION_DAYS=7

# 支持的索引前缀（可根据实际修改）
INDEX_PREFIXES=("qmzs-" "open-course-" ".opendistro-ism-managed-index-history-")

# 获取今天日期
TODAY=$(date +%s)

for PREFIX in "${INDEX_PREFIXES[@]}"; do
  echo "处理前缀: $PREFIX"

  # 获取所有符合前缀的索引名
  curl -s -u "${ES_USER}:${ES_PASS}" "${ES_HOST}/_cat/indices?h=index" | grep "^${PREFIX}" | while read index; do
    # 从索引名中提取日期
    if [[ "$index" =~ ([0-9]{4})\.([0-9]{2})\.([0-9]{2}) ]]; then
      YEAR="${BASH_REMATCH[1]}"
      MONTH="${BASH_REMATCH[2]}"
      DAY="${BASH_REMATCH[3]}"
      INDEX_DATE=$(date -d "${YEAR}-${MONTH}-${DAY}" +%s)

      # 比较时间差
      AGE=$(( (TODAY - INDEX_DATE) / 86400 ))
      if (( AGE > RETENTION_DAYS )); then
        echo "➡️ 删除索引 $index（${AGE}天前）"
        curl -s -u "${ES_USER}:${ES_PASS}" -X DELETE "${ES_HOST}/${index}"
      else
        echo "✅ 保留索引 $index（${AGE}天前）"
      fi
    else
      echo "⚠️ 未识别日期格式的索引：$index"
    fi
  done
done

```


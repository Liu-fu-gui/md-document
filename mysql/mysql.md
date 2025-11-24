
<!-- more -->
# docker-compose安装方式
```
version: '3.8'

services:
  mysql:
    image: mysql:5.7.43
    container_name: mysql_5_7_43
    environment:
      MYSQL_ROOT_PASSWORD: '密码'
    ports:
      - "3306:3306"
    volumes:
      - /root/mysql5.7.43:/var/lib/mysql
    restart: always
```
# 授权对外开放和设置密码

```
docker exec -it mysql_5_7_43 /bin/bash
mysql -uroot -p密码
ALTER USER 'root'@'%' IDENTIFIED BY '新密码';
# 刷新权限
FLUSH PRIVILEGES;
```
# 导出

```
# 本机数据库
mysqldump -u 账号 -p密码 -P 端口 --databases single_edu > single_edu.sql
# 远端数据库
mysqldump -h 远程数据库ip -u shield -p密码 -P 端口 --databases social_study_user > social_study_user.sql
# 导出的时候忽略sys_log表
mysqldump -h 远程数据库ip -u shield -p密码 -P 端口 --databases social_study_user --ignore-table=social_study_user.sys_log > social_study_user.sql
```
- -u 账号：指定 MySQL 用户名。
- -p密码：指定 MySQL 密码（注意密码和 -p 之间没有空格）。
- -P 端口：指定 MySQL 端口号。
- --databases single_edu：指定要导出的数据库名（single_edu）。
- single_edu.sql：将导出的数据库内容保存到 single_edu.sql 文件中。

## tar打包 rsync加速传输 bbcp传输
```
tar czvf social_study_edu.tar.gz social_study_edu.sql

rsync -avP social_study_edu.tar.gz root@ip:/root/mysql-data

bbcp social_study_edu.sql root@192.168.1.53:/var/lib/docker
```
## 导入docker

```
docker cp /root/mysql-data/social_study_user.sql  mysql_5_7_43:/root/social_study_user.sql
```
## 导入数据库

```
mysql -u root -p密码 < social_study_edu.sql
mysql -u root -p密码 --databases single_edu  < social_study_edu.sql
```
## 进程监控

```
# mysql进程监控
 SHOW PROCESSLIST;
```

# 报错问题
Caused by: java.sql.SQLException: null,  message from server: "Host '10.100.20.204' is blocked because of many connection errors; unblock with 'mysqladmin flush-hosts'"
增加线程池

```
max_connections = 500
```

## 数据库拉取
前提条件，mysql版本一致
需求，手动从生产库拉去指定数据库到目标库
```
[root]$ cat xxx.py 
import pymysql
import requests
from concurrent.futures import ThreadPoolExecutor
import gc  # 引入垃圾回收模块

def send_dingtalk_notification(message):
    url = "钉钉的通知webhook"
    headers = {"Content-Type": "application/json"}
    data = {
        "msgtype": "text",
        "text": {
            "content": message
        }
    }
    response = requests.post(url, json=data, headers=headers)
    if response.status_code == 200:
        print("钉钉通知发送成功")
    else:
        print("钉钉通知发送失败", response.text)

def insert_data(target_config, table_name, rows):
    target_db = pymysql.connect(**target_config)
    try:
        target_cursor = target_db.cursor()

        # 插入数据
        if rows:
            insert_query = f"""
            INSERT INTO {table_name} ({', '.join(rows[0].keys())})
            VALUES ({', '.join(['%s'] * len(rows[0]))})
            ON DUPLICATE KEY UPDATE 
            {', '.join([f"{col}=VALUES({col})" for col in rows[0].keys()])};
            """
            target_cursor.executemany(insert_query, [tuple(row.values()) for row in rows])
            target_db.commit()
            print(f"成功插入 {len(rows)} 条数据到表 {table_name}。")

        # 强制垃圾回收
        gc.collect()

    except Exception as e:
        print(f"插入数据时出现错误: {e}")
        target_db.rollback()
    finally:
        target_cursor.close()
        target_db.close()

def sync_table(source_config, target_config, table_name):
    if table_name in ['sys_log', 'user_login_log']:
        print(f"跳过表 {table_name} 的同步。")
        return

    source_db = pymysql.connect(**source_config)
    try:
        source_cursor = source_db.cursor()

        # 统计源表中的总记录数
        source_cursor.execute(f"SELECT COUNT(*) FROM {table_name};")
        total_count = source_cursor.fetchone()[0]
        print(f"表 {table_name} 中总共有 {total_count} 条数据。")

        batch_size = 100000  # 每批处理的大小
        inserted_count = 0  # 记录已插入的总数

        # 获取列名
        source_cursor.execute(f"SHOW COLUMNS FROM {table_name};")
        columns = [col[0] for col in source_cursor.fetchall()]

        # 使用生成器逐批获取数据
        for offset in range(0, total_count, batch_size):
            query = f"SELECT * FROM {table_name} LIMIT {offset}, {batch_size};"
            source_cursor.execute(query)
            rows = source_cursor.fetchall()

            if not rows:
                break  # 没有更多数据

            # 将元组转换为字典
            rows_dict = [dict(zip(columns, row)) for row in rows]

            # 使用线程池进行并行插入
            with ThreadPoolExecutor(max_workers=5) as executor:
                executor.submit(insert_data, target_config, table_name, rows_dict)
                inserted_count += len(rows)

                # 打印当前进度
                print(f"已同步 {inserted_count} / {total_count} 条数据 ({(inserted_count / total_count) * 100:.2f}%)")

    except Exception as e:
        print(f"同步表 {table_name} 时出现错误: {e}")

    finally:
        source_cursor.close()
        source_db.close()

def sync_databases(source_config, target_config):
    source_db = pymysql.connect(**source_config)
    
    try:
        source_cursor = source_db.cursor()
        source_cursor.execute("SHOW TABLES;")
        tables = source_cursor.fetchall()

        for table in tables:
            table_name = table[0]
            print(f"开始同步表 {table_name}...")
            sync_table(source_config, target_config, table_name)
            print(f"完成表 {table_name} 的同步。")

    except Exception as e:
        print(f"数据库操作过程中出现错误: {e}")

    finally:
        source_cursor.close()
        source_db.close()

if __name__ == "__main__":
    source_config = {
        "host": "",   ip
        "user": "shield",  #账号
        "password": "",    #密码
        "port": 3306,
        "database": ""  # 源数据库名称（需要拉的数据库名称）
    }
    
    target_config = {
        "host": "",    #ip
        "user": "",     #账号
        "password": "",  # 密码
        "port": 3306,    
        "database": ""  # 目标数据库名称
    }
    
    sync_databases(source_config, target_config)
    send_dingtalk_notification(f"{target_config['database']} 同步完成")
```
如果出现缓存内存溢出执行

```
echo 1 > /proc/sys/vm/drop_caches
```



## mysql8

```
cat mysql-kaoshizhongxin.yaml 


version: '3.8'

services:
  mysql_kaoshi:
    image: mysql:8.0.33
    container_name: kaoshi_mysql_8.0.33
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 111         # MySQL root 用户密码
      MYSQL_DATABASE: test                # 要创建的数据库名称
      MYSQL_USER: test                        # 普通用户
      MYSQL_PASSWORD: 111              # 普通用户的密码
      TZ: "Asia/Shanghai"                     # 设置时区为上海
    ports:
      - "23406:3306"                             # 将主机的 3306 端口映射到容器的 3306 端口
    volumes:
      - "/home/mysql/data:/var/lib/mysql"   
      - "/home/mysql/my.cnf:/etc/mysql/my.cnf"
      - "/home/mysql/config:/etc/mysql/conf.d"
    networks:
      - mysql_kaoshi_network                   # 使用定义的网络

networks:
  mysql_kaoshi_network:
    driver: bridge                         # 新建网络使用 bridge 驱动
```
### 创建用户

```
CREATE USER 'test'@'%' IDENTIFIED BY '111';
GRANT ALL PRIVILEGES ON *.* TO 'test'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
SELECT User, Host FROM mysql.user;
```
### 更新对外

```
UPDATE mysql.user SET Host='%' WHERE User='test' AND Host='localhost'
```
## my.cnf

``
# 默认存在地址
datadir=/var/lib/mysql
socket=/var/run/mysqld/mysqld.sock
```
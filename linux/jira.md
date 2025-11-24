

参考

https://blog.csdn.net/weixin_44770684/article/details/129227223



需要准备

破解依赖：https://github.com/qinyuxin99/atlassian-agent

mysql-connector-java-5.1.49-bin.jar   https://downloads.mysql.com/archives/c-j/

## 直接构建

需求因为我是已有mysql 所以我是直接运行（先创建一个mysql空的数据库，到时候直接修改文件进行生效）

## docker-compose.yaml

```
version: '3.8'

services:
  jira:
    image: atlassian/jira-software:8.13.22
    container_name: jira
    environment:
      - ATL_DB_TYPE=mysql
      - ATL_JDBC_URL=jdbc:mysql://101.69.246.86:18106/jira?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&sessionVariables=storage_engine=InnoDB
      - ATL_JDBC_USER=open
      - ATL_JDBC_PASSWORD=Cr45ZkSxnnwLyi27
    ports:
      - "8090:8080"
    volumes:
      - jira-data:/var/atlassian/application-data/jira
      - /home/jira/mysql-connector-java-5.1.49/mysql-connector-java-5.1.49-bin.jar:/opt/atlassian/jira/lib/mysql-connector-java-5.1.49-bin.jar
volumes:
  jira-data:
```

docker inspect jira 查看

```
        "Mounts": [
            {
                "Type": "bind",
                "Source": "/home/jira/mysql-connector-java-5.1.49/mysql-connector-java-5.1.49-bin.jar",
                "Destination": "/opt/atlassian/jira/lib/mysql-connector-java-5.1.49-bin.jar",
                "Mode": "rw",
                "RW": true,
                "Propagation": "rprivate"
            },
            {
                "Type": "volume",
                "Name": "jira_jira-data",
                "Source": "/var/lib/docker/volumes/jira_jira-data/_data",
                "Destination": "/var/atlassian/application-data/jira",
                "Driver": "local",
                "Mode": "z",
                "RW": true,
                "Propagation": ""
            }
        ],
```

然后修改 /var/lib/docker/volumes/jira_jira-data/_data 中的dbconfig.xml 文件(库名改成旧的，然后docker compose down up -d)

```
root@dd6960b60678:/var/atlassian/application-data/jira# cat /var/atlassian/application-data/jira/dbconfig.xml 
<?xml version="1.0" encoding="UTF-8"?>

<jira-database-config>
  <name>defaultDS</name>
  <delegator-name>default</delegator-name>
  <database-type>mysql57</database-type>
  <jdbc-datasource>
    <url>jdbc:mysql://address=(protocol=tcp)(host=101.69.246.86)(port=18106)/jira713?sessionVariables=default_storage_engine=InnoDB</url>
    <driver-class>com.mysql.jdbc.Driver</driver-class>
    <username>open</username>
    <password>Cr45ZkSxnnwLyi27</password>
    <pool-min-size>30</pool-min-size>
    <pool-max-size>30</pool-max-size>
    <pool-max-wait>30000</pool-max-wait>
    <validation-query>select 1</validation-query>
    <min-evictable-idle-time-millis>60000</min-evictable-idle-time-millis>
    <time-between-eviction-runs-millis>300000</time-between-eviction-runs-millis>
    <pool-max-idle>30</pool-max-idle>
    <pool-remove-abandoned>true</pool-remove-abandoned>
    <pool-remove-abandoned-timeout>300</pool-remove-abandoned-timeout>
    <pool-test-on-borrow>false</pool-test-on-borrow>
    <pool-test-while-idle>true</pool-test-while-idle>
    <validation-query-timeout>3</validation-query-timeout>
  </jdbc-datasource>
</jira-database-config>
```



## 重新构建

1. 结构

![img](https://liu-fu-gui.github.io/myimg/halo/20250110140409357.png)

2. Dockerfile 文件为

```
## cat Dockerfile
 
# jira 的基础镜像版本, 需要更换成自己需求的版本号，适用于升级jira
FROM atlassian/jira-software:8.16.1 AS jira
 
LABEL creator="刘富贵"
 
USER root
 
# 软件包下载地址: https://github.com/qinyuxin99/atlassian-agent
 
COPY atlassian-agent.jar /opt/atlassian/jira/
 
# jira配置和连接mysql参考文档：https://confluence.atlassian.com/adminjiraserver0816/connecting-jira-applications-to-a-database-1063163980.html
# 添加 mysql 驱动 下载地址： https://downloads.mysql.com/archives/c-j/
#COPY mysql-connector-java-5.1.49.jar /opt/atlassian/jira/lib/
COPY mysql-connector-java-8.0.23.jar  /opt/atlassian/jira/lib/
 
# 使atlassian-agent.jar和 Tomcat 一起启动
RUN echo 'export JAVA_OPTS="-javaagent:/opt/atlassian/jira/atlassian-agent.jar ${JAVA_OPTS}"' >> /opt/atlassian/jira/bin/setenv.sh
 
# RUN echo 'java -jar /opt/atlassian/jira/atlassian-agent.jar -p jira -m admin@qq.com -n jira -o https://zhile.io -s BUB5-TBAT-K30D-VCPN' >/opt/1.txt
 
# ubuntu 系统设置时区 和 24小时
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone && echo "LC_TIME=en_DK.UTF-8" >/etc/default/locale
 

# 构建 jira镜像和 推送镜像到harbor等镜像仓库
#  docker build -t test/atlassian/jira-software:8.16.1 . --no-cache
#  docker push test/atlassian/jira-software:8.16.1 
```

3. 构建jira镜像

```
# 构建 jira 镜像
cd jira  # 切换到 Dockerfile 文件目录下，执行构建镜像命令
docker build -t test/atlassian/jira-software:8.16.1 . --no-cache
```

 4.创建jira容器启动文件

使用 docker-compose 创建jira启动文件，并且包含了mysql 启动文件（若是使用其他安装的mysql，则可以注释掉mysql 启动文件部分）

```
## cat docker-compose.yaml
 
version: '3.3'
services:
    jira:
        container_name: jira
#        depends_on:
#        - mysql
#        links:
#        - mysql
        image: test/atlassian/jira-software:8.16.1
        restart: always
        privileged: true
        ports:
            - '8080:8080'
        environment:
            - TZ=Asia/Shanghai
#            TZ: Asia/Shanghai # 两种环境变量写法
        volumes:
            - './jira/jiradata:/var/atlassian/application-data/jira'
 
#   容器安装mysql 启动
#   jira连接mysql 配置参考文档： https://confluence.atlassian.com/adminjiraserver0816/connecting-jira-applications-to-a-database-1063163980.html
    mysql:
        container_name: mysql
        image: mysql:5.7
        restart: always
        ports:
            - '3306:3306'
        environment:
            - MYSQL_ROOT_PASSWORD=root
            - TZ=Asia/Shanghai
        volumes:
            - './mysql/data:/var/lib/mysql'
            - './mysql/conf:/etc/mysql/conf.d'
            - './mysql/backup:/backup'
```

5. 数据库配置和创建jira须满足条件

    1、jira连接mysql 5.7，需要满足的条件

   参考文档：https://confluence.atlassian.com/adminjiraserver0816/connecting-jira-applications-to-mysql-5-7-1063163986.html

   ​     2、jira连接mysql 8.0，需要满足的条件 

   参考文档：https://confluence.atlassian.com/adminjiraserver0816/connecting-jira-applications-to-mysql-8-0-1063163987.html

   ​    3、mysql启动参数

   ```
   ## my.cnf
   ## 将 my.cnf 文件复制到 mysql的配置映射目录 mysql/conf
    
   [mysqld]
   default-storage-engine=INNODB
   character_set_server=utf8mb4
   innodb_default_row_format=DYNAMIC
   innodb_large_prefix=ON
   innodb_file_format=Barracuda
   innodb_log_file_size=2G
   ```

    4、登录mysql创建jiradb数据库和jira用户（mysql 5.7）

   ```
   --- mysql 5.7 创建 jiradb数据库脚本
   CREATE DATABASE jiradb CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
    
   --- MySQL 5.7.0 - 5.7.5:
    
   GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,ALTER,INDEX on jiradb.* TO 'jira'@'%' IDENTIFIED BY 'jira';
   flush privileges;
    
   --- MySQL 5.7.6 and later 
   GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,REFERENCES,ALTER,INDEX on jiradb.* TO 'jira'@'%' IDENTIFIED BY 'jira';
   flush privileges;
   ```

   

![image-20250110140814622](https://liu-fu-gui.github.io/myimg/halo/20250110140814689.png)

6. 配置jira和配置许可

访问地址 http://ip:8080

![image-20250110140920798](https://liu-fu-gui.github.io/myimg/halo/20250110140920858.png)

![image-20250110141545603](https://liu-fu-gui.github.io/myimg/halo/20250110141545667.png)

![image-20250110141557600](https://liu-fu-gui.github.io/myimg/halo/20250110141557657.png)

从截图看出jira Server ID为： BRM9-6AGL-H5ZZ-2APS，则登录jira容器执行获取许可命令：

http://101.69.246.86:18090/secure/admin/ViewSystemInfo.jspa

![image-20250110181150046](https://liu-fu-gui.github.io/myimg/halo/20250110181150103.png)

```
# 登录 jira容器 执行破解命令， BRM9-6AGL-H5ZZ-2APS 为 对应的  Server ID
java -jar /opt/atlassian/jira/atlassian-agent.jar -p jira -m admin@qq.com -n jira -o https://zhile.io -s 'BBUV-DNLG-SGDL-RG31'
```

![image-20250110141955049](https://liu-fu-gui.github.io/myimg/halo/20250110141955106.png)

![image-20250110142009102](https://liu-fu-gui.github.io/myimg/halo/20250110142009157.png)

   3、将输出许可秘钥结果回填到 Your License Key 填写框中：

![image-20250110142032304](https://liu-fu-gui.github.io/myimg/halo/20250110142032360.png)

  4、填写许可秘钥，点下一步后，出现的空白框为设置管理员相关信息、，按照自己的情况填写即可，我这里以admin为例：

![image-20250110142050653](https://liu-fu-gui.github.io/myimg/halo/20250110142050717.png)

5、邮箱认证测试可以选择laster,即先不测试邮箱是否能接收到通知可用，然后接着下一步继续即可

![image-20250110142107773](https://liu-fu-gui.github.io/myimg/halo/20250110142107833.png)

![image-20250110142120692](https://liu-fu-gui.github.io/myimg/halo/20250110142120752.png)

![image-20250110142129628](https://liu-fu-gui.github.io/myimg/halo/20250110142129685.png)

## [k8s部署](https://so.csdn.net/so/search?q=k8s部署&spm=1001.2101.3001.7020)jira文件(附加内容)

```
 
### cat k8s-jira.yaml
 
---
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    meta.helm.sh/release-name: my-jira
    meta.helm.sh/release-namespace: default
  labels:
    app.kubernetes.io/instance: my-jira
    app.kubernetes.io/name: jira-software
  name: my-jira-jira-software
  namespace: default
spec:
  progressDeadlineSeconds: 600
  replicas: 1
  revisionHistoryLimit: 10
  selector:
    matchLabels:
      app.kubernetes.io/instance: my-jira
      app.kubernetes.io/name: jira-software
  strategy:
    type: Recreate
  template:
    metadata:
      annotations:
      labels:
        app.kubernetes.io/instance: my-jira
        app.kubernetes.io/name: jira-software
    spec:
      containers:
      - image: test/atlassian/jira-software:8.16.1
        imagePullPolicy: IfNotPresent
        name: jira-software
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
        terminationMessagePath: /dev/termination-log
        terminationMessagePolicy: File
        volumeMounts:
        - mountPath: /etc/localtime
          name: host-time
          readOnly: true
        - mountPath: /var/atlassian/application-data/jira
          name: jira-data-pvc
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext:
        fsGroup: 2001
      serviceAccount: default
      serviceAccountName: default
      terminationGracePeriodSeconds: 30
      volumes:
      - hostPath:
          path: /etc/localtime
          type: ""
        name: host-time
      - name: jira-data-pvc
        persistentVolumeClaim:
          claimName: jira-data
---
---
apiVersion: v1
kind: Service
metadata:
  annotations:
    meta.helm.sh/release-name: my-jira
    meta.helm.sh/release-namespace: default
  labels:
    app.kubernetes.io/instance: my-jira
    app.kubernetes.io/name: jira-software
  name: my-jira-jira-software
  namespace: default
spec:
  clusterIP: 10.233.39.2
  clusterIPs:
  - 10.233.39.2
  internalTrafficPolicy: Cluster
  ipFamilies:
  - IPv4
  ipFamilyPolicy: SingleStack
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 8080
  selector:
    app.kubernetes.io/instance: my-jira
    app.kubernetes.io/name: jira-software
  sessionAffinity: None
  type: ClusterIP
 
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    nginx.ingress.kubernetes.io/configuration-snippet: |
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    nginx.ingress.kubernetes.io/proxy-body-size: 1024m
  name: my-jira-jira-software
spec:
  ingressClassName: nginx
  rules:
  - host: myjira.com
    http:
      paths:
      - backend:
          service:
            name: my-jira-jira-software
            port:
              number: 80
        path: /
        pathType: Prefix
---

```


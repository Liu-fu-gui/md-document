### **系统环境准备**

1. **检查系统版本**

   ```
   cat /etc/centos-release
   ```
   
   输出应为：`CentOS Linux release 7.9.2009 (Core)`
   
2. **更新系统 **安装必要工具****

   ```
   sudo yum update -y
   yum install -y wget unzip tar vim java-11-openjdk-devel
   ```
   
4. **配置 Java 环境变量** 验证 Java 是否安装正确：

   ```
   java -version
   ```
   
   配置环境变量（如果需要）：
   
   ```
   # 单体
   echo "export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which java)))))" >> ~/.bashrc
   echo "export PATH=$JAVA_HOME/bin:$PATH" >> ~/.bashrc
   source ~/.bashrc
   
   # 全局
   echo "export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which java))))))" | sudo tee -a /etc/profile
   echo "export PATH=\$JAVA_HOME/bin:\$PATH" | sudo tee -a /etc/profile
   source /etc/profile
   ```

------

### **步骤 1: 安装 MySQL**

1. **安装 MySQL** CentOS 7.9 默认使用 MariaDB，但 Ranger 推荐使用 MySQL。

   ```
   sudo yum install -y https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm
   sudo yum install -y mysql-community-server
   sudo yum install --nogpgcheck -y mysql-community-server
   
    # MySQL 的 JDBC 驱动 mysql-connector-java
   ```

2. **启动 MySQL 服务**

   ```
   sudo systemctl start mysqld
   sudo systemctl enable mysqld
   ```

3. **设置 MySQL Root 密码** 获取临时密码：

   ```
   grep 'temporary password' /var/log/mysqld.log
   
   [root@ceshi /opt/apache-ranger-2.4.0]$ grep 'temporary password' /var/log/mysqld.log
   2025-01-02T06:45:29.888963Z 1 [Note] A temporary password is generated for root@localhost: &jXENc5LEJiG
   ```

   使用临时密码登录：

   ```
   mysql -u root -p
   ```

   设置新密码并执行安全配置：

   ```
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'Qyuh@12345';
   ```

4. **创建 Ranger 数据库**

   ```
   CREATE DATABASE ranger;
   CREATE USER 'rangeradmin'@'%' IDENTIFIED BY 'Qyuh@12345';
   GRANT ALL PRIVILEGES ON ranger.* TO 'rangeradmin'@'%';
   FLUSH PRIVILEGES;
   ```

------

### **步骤 2: 安装 Apache Ranger**

1. **下载 Ranger**

   ```
   # 需要自己构建，很恶心
   #wget https://downloads.apache.org/ranger/2.5.0/apache-ranger-2.5.0.tar.gz
   #tar -zxvf ranger-2.5.0.tar.gz
   # cd ranger-2.5.0
   
   服务器中有构建好的，直接用 ranger-2.1.0.tar.gz
   
   ## 支支持arm，垃圾，我们正宗adm64 86的
   #docker pull apache/ranger
   #docker run -d -p 6080:6080 apache/ranger
   ```

   **构建安装包** 在 Ranger 源代码目录中，执行以下命令以生成安装包：（构建需要）

   ```
   wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
   tar -zxvf apache-maven-3.9.5-bin.tar.gz
   mv apache-maven-3.9.5 /opt/maven
   
   配置环境变量 编辑 /etc/profile 或用户的 ~/.bashrc 文件，添加以下内容：
   
   echo "export MAVEN_HOME=/opt//apache-maven-3.9.5" | sudo tee -a /etc/profile
   echo "export PATH=\$MAVEN_HOME/bin:\$PATH" | sudo tee -a /etc/profile
   
   source /etc/profile
   mvn -v
   
   mvn clean compile package -Pall -DskipTests
   ```

   **找到生成的安装包** 构建成功后，安装包通常位于以下路径：（构建需要）

   ```
   target/ranger-<module>-<version>.tar.gz
   ```

   **解压 Admin 服务包** 进入 `target` 目录并解压 `ranger-<module>-admin-<version>.tar.gz`：（构建需要）

   ```
   tar -zxvf ranger-<module>-admin-<version>.tar.gz -C /opt/ranger-admin
   ```

   **配置 Ranger Admin** 编辑 Ranger Admin 配置文件 `/opt/ranger-admin/install.properties`，设置数据库连接信息：（构建需要）

   ```
   db_root_user=root
   db_root_password=Qyuh@12345
   db_host=localhost
   
   # DB UserId used for the Ranger schema
   #
   db_name=ranger
   db_user=rangeradmin
   db_password=Qyuh@12345
   
   # Solr details
   audit_solr_urls=http://localhost:8983/solr/ranger_audits
   ```

   **初始化数据库** 切换到 Ranger Admin 安装目录并运行数据库初始化脚本：

   ```
   cd /opt/ranger-admin
   
   ./setup.sh
   # 不行就绝对路径
   sudo JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.412.b08-1.el7_9.x86_64 ./setup.sh
   ```

   **启动 Ranger Admin**

   ```
   ./ranger-admin start
   sudo /opt/ranger-2.1.0/admin/ews/ranger-admin-services.sh start
   sudo /opt/ranger-2.1.0/admin/ews/ranger-admin-services.sh status
   
   ```

   检查状态：

   ```
   ./ranger-admin status
   ```

2. **访问 Web 界面** 打开浏览器访问：`http://ip:6080`
   默认登录：`admin` / `admin`

### 然后开始装插件Ranger Kafka 插件

```
[root@ceshi /opt/ranger-2.1.0]$ find . -name kafka
./ews/webapp/WEB-INF/classes/ranger-plugins/kafka

## 放kafka中
cd /opt/kafka_2.13-3.9.0
cp /opt/ranger-2.1.0/ews/webapp/WEB-INF/classes/ranger-plugins/kafka/*.jar /opt/kafka_2.13-3.9.0/libs/

## kafka server.properties


# 启用 Kafka 的 ACL 授权器
authorizer.class.name=kafka.security.authorizer.AclAuthorizer

# 设置超级用户，可以访问所有的资源
super.users=User:admin

# 启用 Ranger Kafka 插件
ranger.kafka.plugin.enabled=true

# 配置 Ranger Admin 服务的 URL，确保 Kafka 可以访问到 Ranger 的 REST API
ranger.plugin.kafka.service.url=http://localhost:6080

重启kafka和zk
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

```
# 验证
./kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
  --list --topic test_topic
  
  [root@ceshi /opt/kafka_2.13-3.9.0/bin]$ ./kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
>   --list --topic test_topic
Warning: support for ACL configuration directly through the authorizer is deprecated and will be removed in a future release. Please use --bootstrap-server instead to set ACLs through the admin client.
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=test_topic, patternType=LITERAL)`: 
 	(principal=User:admin, host=*, operation=WRITE, permissionType=ALLOW) 

从输出来看，User:admin 已经成功被授予对 test_topic 的 WRITE 权限。接下来，我们需要确保生产者使用了 User:admin 进行连接，并验证生产者是否能够正常发送消息

# 测试
./kafka-console-producer.sh --broker-list localhost:9092 --topic test_topic
# 检验连通性
./kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# 运行时指定
./kafka-console-producer.sh --broker-list localhost:9092 --topic test_topic \
  --producer-property principal=User:admin
```



<!-- more -->
## mongodb
因为mongodb是NOSQL，所以他的数据也是比较重要的，所以做个持久化，也就是将容器中的数据映射到机器上；

### 创建存放数据、日志、配置文件的目录：

```
mkdir /data/mongo/config -p
mkdir /data/mongo/data
mkdir /data/mongo/logs
```

创建配置文件：

```
touch /data/mongo/config/mongod.conf
chmod 777 /data/mongo
```

修改配置文件：

```
vim /data/mongo/config/mongod.conf 
```

```
# 数据库存储路径
dbpath=/data/mongo/data
 
# 日志文件路径
logpath=/data/mongo/logs/mongod.log
 
# 监听的端口
port=27017
 
# 允许所有的 IP 地址连接
bind_ip=0.0.0.0
 
# 启用日志记录
journal=true
 
# 是否后台运行
fork=true
 
# 启用身份验证
#auth=true
```


启动容器：

```
[root@docker ~/docker-com]$ cat mongo.yaml 
version: '3.8'

services:
  mongo:
    container_name: mongo
    image: mongo:3.4
    ports:
      - "17017:27017"
    volumes:
      - /data/mongo/config/mongo.conf:/etc/mongod.conf
      - /data/mongo/data:/data/db
      - /data/mongo/logs:/var/log/mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: 123456
    restart: always
```
命令解释：

```
    docker run：运行 Docker 容器的命令。
    -itd：以守护进程模式运行容器，并在容器内分配一个伪终端。
    --name mongo：为容器指定一个名称。
    -p 17017:27017：将容器的 27017 端口映射到主机的 17017 端口，允许外部访问 MongoDB 数据库。
    -v /data/mongo/config/mongod.conf:/etc/mongod.conf：将主机上的/data/mongo/config/mongod.conf文件挂载到容器的/etc/mongod.conf位置，作为 MongoDB 的配置文件。
    -v /data/mongo/data:/data/db：将主机上的/data/mongo/data目录挂载到容器的/data/db位置，作为 MongoDB 的数据存储目录。
    -v /data/mongo/logs:/var/log/mongodb：将主机上的/data/mongo/logs目录挂载到容器的/var/log/mongodb位置，作为 MongoDB 的日志存储目录。
    -e MONGO_INITDB_ROOT_USERNAME=admin：设置 MongoDB 初始化时的 root 用户名为admin。
    -e MONGO_INITDB_ROOT_PASSWORD=123456：设置 MongoDB 初始化时的 root 密码为123456。
    --restart=always：容器在退出后总是自动重启。
    mongo：指定要运行的 Docker 镜像为 MongoDB。
```
进入容器中：
```
docker exec -it mongo /bin/bash
```

![20241129164413](https://liu-fu-gui.github.io/myimg/halo/20241129164413.png)


```
mongo --host mongo -u admin -p 123456 --authenticationDatabase admin
```
使用admin账户 连接：

```
use admin
```
切换完之后，开始进行用户认证：

```
db.auth("admin","123456")
```

请按照以下步骤重新创建用户：

确保您连接到 admin 数据库：

```
docker exec -it mongo mongo -u admin -p 123456 --authenticationDatabase admin
```

创建 goldmanUser 用户并授予其在 goldman 数据库上的 readWrite 权限：


```
use goldman
db.createUser({
    user: "goldmanUser",
    pwd: "mongo",
    roles: [{ role: "readWrite", db: "goldman" }]
});
```

之后，您可以再次检查 goldman 数据库中的用户：

```
db.getUsers()
```

尝试使用 goldmanUser 进行连接：

```
docker exec -it mongo mongo -u goldmanUser -p mongo --authenticationDatabase goldman
```

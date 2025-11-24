<!-- more -->

## win安装切换

PowerShell 临时设置
```
# 设置 JAVA_HOME 到 Java 20 的路径
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_211"
# PowerShell 更新 PATH 变量
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

## 用户版临时
$newPath = "$env:JAVA_HOME\bin;" + [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::User)
[System.Environment]::SetEnvironmentVariable("PATH", $newPath, [System.EnvironmentVariableTarget]::User)

## 永久
$newPath = "$env:JAVA_HOME\bin;" + [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::Machine)
[System.Environment]::SetEnvironmentVariable("PATH", $newPath, [System.EnvironmentVariableTarget]::Machine)
```
CMD

```
## 临时
REM 切换到 Java 20
set JAVA_HOME=C:\Program Files\Java\jdk-20
set PATH=%JAVA_HOME%\bin;%PATH%

REM 验证 Java 版本
java -version

## 永久
REM 切换到 Java 20
setx JAVA_HOME "C:\Program Files\Java\jdk-20"
setx PATH "%JAVA_HOME%\bin;%PATH%"

REM 需要重新启动 CMD 或重新登录系统

```


## jdk1.8安装

### 安装
```
yum -y list java*  //查看目前yum中的jdk版本，下载
yum install java-1.8.0-openjdk.x86_64
# 完整版
yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel

```
### 验证

```
java -version
```
## 多个java环境

```
yum install epel-release
yum install java-1.7.0-openjdk-devel
#查看当前是否添加成功，tar.gz本地安装方法
alternatives --install /usr/bin/java java /opt/jdk-17.0.12/bin/java 2
alternatives --install /usr/bin/javac javac /opt/jdk-17.0.12/bin/javac 2
alternatives --display java
alternatives --config java


#ubt
update-alternatives --display java

```
### 卸载OpenJDK以及相关的Java文件

```
查看JDK信息，输入命令：java -version
检测JDK安装包，输入命令：rpm -qa | grep java
```
### （选填）删除Java相关文件（/usr/lib/jvm是默认openjdk安装路径）

```
rm -rf /usr/lib/jvm 
```
### 安装包安装 jdk17

```
1.使用root在local下 下载安装包
cd /usr/local
 
看架构 
 uname -m
cat /etc/os-release

#链接失效去找云资源，或者call我 
https://www.oracle.com/cn/java/technologies/javase/javase8u211-later-archive-downloads.html

https://www.oracle.com/cn/java/technologies/downloads/#java17

##  ubt安装rpm包
apt install alien -y
alien --to-deb jdk-17_linux-x64_bin.rpm
apt-get install -f
dpkg -i jdk-17_linux-x64_bin.deb
java -version

## ubt yum jdk
apt install openjdk-17-jdk



wget https://bucket-qinfeng.oss-cn-beijing.aliyuncs.com/storehouse/v1/cloudfiles/tmp1/jdk-8u221-linux-x64.tar.gz 
2.解压缩
tar -zxvf jdk-8u221-linux-x64.tar.gz
 
3.修改环境变量
vim /etc/profile
 
export JAVA_HOME=/usr/local/jdk1.8.0_221
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib:$CLASSPATH
export JAVA_PATH=${JAVA_HOME}/bin:${JRE_HOME}/bin
export PATH=$PATH:${JAVA_PATH}
 
4.生效化环境变量
source /etc/profile
```


## maven安装
### 官方安装
https://maven.apache.org/download.cgi


![20241129174233](https://liu-fu-gui.github.io/myimg/halo/20241129174233.png)

### 安装
#### 1.将下载好的maven安装包放在磁盘的 /usr/local/ 目录下，如下图：


![20241129174243](https://liu-fu-gui.github.io/myimg/halo/20241129174243.png)
#### 2.解压apache-maven-3.6.3-bin.tar.gz文件。如下图：

![20241129174251](https://liu-fu-gui.github.io/myimg/halo/20241129174251.png)

#### 3.将解压后的文件夹改成一个短一点的名字：maven

```
mv /usr/local/apache-maven-3.8.6/ /usr/local/maven
```
#### 4.配置仓库（取决于你们的需求）
settings.xml

#### 5.配置系统变量

```
vim ~/.bashrc 
```

```
export MAVEN_HOME=/usr/local/maven
export PATH=$PATH:$MAVEN_HOME/bin
```
#### 6.验证

```
mvn -version
```
## sup安装
### pip3安装

```
pip3 install supervisor -i https://pypi.tuna.tsinghua.edu.cn/simple

# ubt安装
apt install supervisor
apt install python3-pip
sudo pip3 install supervisor
```
### 如果报错网络问题

```
mkdir -p ~/.pip
vim ~/.pip/pip.conf

[global]
index-url = https://pypi.tuna.tsinghua.edu.cn/simple
```
### 配置文件生成

```
mkdir -p /etc/supervisor
/usr/local/bin/echo_supervisord_conf > /etc/supervisor/supervisord.conf
cd /etc/supervisor
##备份完整配置文件
cp supervisord.conf{,.bak}

# 修改配置文件
[unix_http_server]
file=/etc/supervisor/supervisor.sock   ; Unix socket文件的路径
chmod=0700                            ; 设置socket文件权限

[supervisord]
logfile=/etc/supervisor/supervisord.log          ; 日志文件路径
logfile_maxbytes=50MB                 ; 最大日志文件大小
logfile_backups=10                    ; 保留的日志文件备份数
pidfile=/etc/supervisor/supervisord.pid           ; pid文件路径
nodaemon=false                       ; 后台运行，默认为false
minfds=1024                           ; 启动所需的最小文件描述符数
minprocs=200                          ; 启动所需的最小进程数

[rpcinterface:supervisor]
supervisor.rpcinterface_factory = supervisor.rpcinterface:make_main_rpcinterface

[supervisorctl]
serverurl=unix:///etc/supervisor/supervisor.sock  ; 配置连接到supervisord的Unix socket路径

[include]
files = /etc/supervisor/conf.d/*.ini  ; 加载额外的配置文件




## ubt
echo_supervisord_conf | sudo tee /etc/supervisor/supervisord.conf
```

![image-20250207104855787](https://liu-fu-gui.github.io/myimg/halo/20250207104855886.png)



### 子配置文件示范（按照自己需求改）

```
vim /etc/supervisor/conf.d/test/ini
```
```
[program:test]
user=root  # 执行进程的用户
directory=/xxx/PythonProject  # 脚本的工作目录
command=/user/bin/python xxx.py # 运行的实际命令

# 自启
priority=3  # 进程启动优先级，默认999，值小的优先启动
startsecs=30  #进程持续运行多久才认为是启动成功
autostart=true  # supervisor启动时自动该应用
autorestart=true  # 程序崩溃时自动重启

# 日志
stderr_logfile=/etc/supervisor/log/test/err.log # 输出error日志的文件路径
stdout_logfile=/etc/supervisor/log/test/out.log # 输出日志的文件路径
stdout_logfile_maxbytes = 30MB # stdout日志文件大小，默认 50MB
stdout_logfile_backups = 3  # stdout日志文件备份数



## 示例
[program:s-gateway]
directory=/deploy/app/open-course-api-prod/jar
command=/opt/jdk-17.0.13/bin/java -jar -Xms1024m -Xmx2048m -XX:MetaspaceSize=1024m -XX:MaxMetaspaceSize=2048m  s-gateway.jar --spring.profiles.active=prod --logPath=/deploy/logs/app/open-course-api-prod/s-gateway
autorestart=true
startsecs=5
startretries=3
stdout_logfile=/deploy/app/open-course-api-prod/log/s-gateway.log
stderr_logfile=/deploy/app/open-course-api-prod/log/s-gateway.err.log

```

### 启动sup服务，并查看是否生效

```
/usr/local/python3/bin/supervisord -c  /etc/supervisor/supervisord.conf
	
ps -ef | grep supervisord 
supervisorctl status
```
### 设置开机自启动

#### 配置文件

```
vim /etc/systemd/system/supervisord.service
```

```
[Unit]
Description=Supervisor process control system for UNIX
Documentation=http://supervisord.org
After=network.target

[Service]
ExecStart=/usr/local/bin/supervisord -c /etc/supervisor/supervisord.conf
ExecStop=/usr/local/bin/supervisorctl shutdown
Restart=always
User=root
Group=root
StandardOutput=syslog
StandardError=syslog

[Install]
WantedBy=multi-user.target
```
```
systemctl enable supervisord.service
systemctl is-enabled supervisord #提示：enabled 表示设置成功！
systemctl status supervisord  # 检查启动状态
```

## 安装node mvn

```
wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash

# 安装 Node.js 和 npm
nvm install node
nvm use node
```
### 加速

```
# node加速
export NVM_NODEJS_ORG_MIRROR=https://npmmirror.com/mirror/node
export NVM_NODEJS_ORG_MIRROR=https://nodejs.org/dist
export NVM_NODEJS_ORG_MIRROR=https://mirrors.ustc.edu.cn/node/
```

### jenkins生效
echo $PATH
![20241129213233](https://liu-fu-gui.github.io/myimg/halo/20241129213233.png)

## nrm管理npm源
安装

```
npm config set registry https://registry.npmjs.org/
npm install -g nrm
nrm ls
```
当前源

```
nrm current
```
切换源

```
nrm use npm
```
测试源![20241129213220](https://liu-fu-gui.github.io/myimg/halo/20241129213220.png)

```
nrm test
```



<!-- more -->
# yum命令安装jenkins
## 1.安装jdk：

```
yum install java
```

## 2.检查jdk：

```
java -version
```

## 3.linux中执行命令(设置配置文件)

```
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key
yum install epel-release # repository that provides ‘daemonize’ -y
```
## 4.执行安装jenkins命令

```
yum install jenkins -y
```

## 5.启动jenkins命令

```
systemctl start jenkins --now
```
## 6.检查jenkins
```
ps aux|grep jenkins
```

### 6.开通端口号(如下为临时端口号开通，机器重启还需要再次重启，最好做一个nginx代理)不做安全可以略过
/sbin/iptables -I INPUT -p tcp --dport 80 -j ACCEPT
7.浏览器访问：
ip+8080端口
8.获取密码登录jenkins；
cat /var/lib/jenkins/secrets/initialAdminPassword
9.初始化jenkins
选择后续安装插件
10.登录进去之后安装其它插件依赖项
国际化插件：Localization: Chinese (Simplified)、
git插件：Git、
ssh插件：Publish Over SSH、
maven插件：Maven Integration、
npm插件(前端vue)：NodeJS Plugin
11.服务器安装git和maven
yum install git
yum install maven





## docker安装

```
mkdir -p /home/docker/jenkins
chown -R 1000:1000 /home/docker/jenkins
chmod -R 755 /home/docker/jenkins


version: '3.8'
services:
  jenkins:
    image: jenkinsci/blueocean:latest
    container_name: jenkins
    restart: unless-stopped
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - /home/docker/jenkins:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    user: "1000:1000"  # 添加此行
```


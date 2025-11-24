1、安装docker

(1)卸载旧版本

sudo apt-get remove docker docker-engine docker.io containerd runc



(2)更新apt包索引并安装包以允许apt在HTTPS上使用存储库

sudo apt-get install -y apt-transport-https ca-certificates curl gnupg-agent software-properties-common



(3)添加Docker官方GPG密钥 # -fsSL

apt update

apt-get install ca-certificates curl gnupg lsb-release



(4)写入docker镜像源信息

sudo add-apt-repository "deb [arch=amd64] https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/ubuntu $(lsb_release -cs) stable"



(5)安装特定版本的Docker引擎,请在repo中列出可用的版本

sudo apt-get update && sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin



(6)修改docker启动项

sudo mkdir -p /etc/docker

sudo tee /etc/docker/daemon.json <<-'EOF'

{

 "registry-mirrors": [

  "https://docker.credclouds.com",

  "https://k8s.credclouds.com",

  "https://quay.credclouds.com",

  "https://gcr.credclouds.com",

  "https://k8s-gcr.credclouds.com",

  "https://ghcr.credclouds.com",

  "https://do.nark.eu.org",

  "https://docker.m.daocloud.io",

  "https://docker.nju.edu.cn",

  "https://docker.mirrors.sjtug.sjtu.edu.cn"

 ]

}

EOF



(7)保存退出,更新设置

sudo systemctl daemon-reload

sudo systemctl start docker

sudo systemctl enable docker



2、安装mysql

sudo docker run --name mysql-server -t \

-e MYSQL_DATABASE="zabbix" \

-e MYSQL_USER="zabbix" \

-e MYSQL_PASSWORD="123.com" \

-e MYSQL_ROOT_PASSWORD="123.com" \

-p 3306:3306 \

-d mysql:5.7



3、安装zabbix_server

sudo docker run --name zabbix-server-mysql -t \

-e DB_SERVER_HOST="mysql-server" \

-e MYSQL_DATABASE="zabbix" \

-e MYSQL_USER="zabbix" \

-e MYSQL_PASSWORD="123.com" \

-e MYSQL_ROOT_PASSWORD="123.com" \

--link mysql-server:mysql \

-d zabbix/zabbix-server-mysql:latest



4、zabbix前端

sudo docker run --name zabbix-web-nginx-mysql -t \

-e DB_SERVER_HOST="mysql-server" \

-e MYSQL_DATABASE="zabbix" \

-e MYSQL_USER="zabbix" \

-e MYSQL_PASSWORD="123.com" \

--link zabbix-server-mysql:zabbix-server \

--link mysql-server:mysql \

-p 80:80 \

-d zabbix/zabbix-web-nginx-mysql:latest



5、zabbix agentd

sudo docker run --name zabbix-agent -t \

-e ZBX_SERVER_HOST="zabbix-server-mysql" \

-p 10050:10050 \

-d zabbix/zabbix-agent:latest

![img](https://mmbiz.qpic.cn/mmbiz_png/v1aibJFYicrQBYWxKVwnXzvhfFBrujNdveYHJw7qUSxpDS8gTuDyJMevI2FD1t7ibZMicDyMf0jibABa565O81kPzzQ/640?wx_fmt=png&from=appmsg)

以上，既然看到这里了，如果觉得不错，随手点个赞、在看、转发三连吧，如果想第一时间收到推送，也可以给我个星标⭐～谢谢你看我的文章，我们下次再见。
```
#!/bin/sh
#zabbix install 
ip=`ip addr |grep inet |egrep -v "inet6|127.0.0.1" |awk '{print $2}' |awk -F "/" '{print $1}'`
release=`cat /etc/redhat-release | awk '{print $4}' | awk -F '.' '{print $1}'`
printf "
#######################################################################
#                 欢迎使用Zabbix离线一键部署脚本                      #
#             脚本适配环境CentOS7/8、Zabbix5.0/5.2                    #
#             避免软件包产生冲突建议使用纯净的操作系统进行安装！#
#######################################################################
"
echo "#######################################################################"
echo "#                                                                     #"
echo "#                  正在关闭SElinux策略及防火墙 请稍等~                #"
echo "#                                                                     #"
echo "#######################################################################"
sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
setenforce 0
systemctl stop firewalld  && systemctl disable firewalld
echo "#######################################################################"
echo "#                                                                     #"
echo "#           正在判断系统环境并安装相对应的yum源和软件 请稍等~         #"
echo "#           (如果是CentOS 8 就安装5.2，如果是7则安装5.0)              #"
echo "#                                                                     #"
echo "#######################################################################"
if [ $release -eq '7' ];then
cat <<EOF > /etc/yum.repos.d/zabbix.repo
[zabbix]
name=Zabbix Official Repository - \$basearch
baseurl=https://mirrors.aliyun.com/zabbix/zabbix/5.0/rhel/7/\$basearch/
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591

[zabbix-frontend]
name=Zabbix Official Repository frontend - $basearch
baseurl=https://mirrors.aliyun.com/zabbix/zabbix/5.0/rhel/7/\$basearch/frontend
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591

[zabbix-non-supported]
name=Zabbix Official Repository non-supported - \$basearch
baseurl=https://mirrors.aliyun.com/zabbix/non-supported/rhel/7/\$basearch/
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
gpgcheck=1
EOF
echo "添加源gpgkey"
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX-A14FE591 \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX-A14FE591 \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
yum -y install mariadb-server centos-release-scl epel-release
yum -y install zabbix-server-mysql zabbix-web-mysql zabbix-web-mysql-scl zabbix-apache-conf-scl zabbix-agent vim 
elif [ $release -eq '8' ];then
echo "检测您的机器为CentOS 8，可以安装Zabbix 5.2"
cat <<EOF > /etc/yum.repos.d/zabbix.repo
[zabbix]
name=Zabbix Official Repository - \$basearch
baseurl=https://mirrors.aliyun.com/zabbix/zabbix/5.2/rhel/8/\$basearch/
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591
[zabbix-non-supported]
name=Zabbix Official Repository non-supported - \$basearch
baseurl=https://mirrors.aliyun.com/zabbix/non-supported/rhel/8/\$basearch/
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
gpgcheck=1
EOF
echo "添加源gpgkey"
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX-A14FE591 \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX-A14FE591 \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX-A14FE591
curl https://mirrors.aliyun.com/zabbix/RPM-GPG-KEY-ZABBIX \
-o /etc/pki/rpm-gpg/RPM-GPG-KEY-ZABBIX
dnf -y install mariadb-server zabbix-server-mysql zabbix-web-mysql zabbix-apache-conf zabbix-agent vim
fi
echo "#######################################################################"
echo "#                                                                     #"
echo "#                 正在启动及配置Mariadb数据库 请稍等~                 #"
echo "#                                                                     #"
echo "#######################################################################"
service mariadb start && service mariadb enable
while :; do echo
    read -p "设置Mysql数据库root密码（建议使用字母+数字）: " Database_Password 
    [ -n "$Database_Password" ] && break
done

mysqladmin -u root password "$Database_Password"
echo "create database  zabbix default charset utf8 COLLATE utf8_bin;" | mysql -uroot -p$Database_Password
echo "grant all privileges on zabbix.* to zabbix@'localhost' identified by '$Database_Password';" | mysql -uroot -p$Database_Password
echo "flush privileges;" | mysql -uroot -p$Database_Password

echo "#######################################################################"
echo "#                                                                     #"
echo "#                 正在导入zabbix数据库架构文件，请稍等~               #"
echo "#                                                                     #"
echo "#######################################################################"
zcat /usr/share/doc/zabbix-server-mysql*/create.sql.gz | mysql -uzabbix -p$Database_Password zabbix

echo "#######################################################################"
echo "#                                                                     #"
echo "#                 正在修改Zabbix配置文件，请稍等~                     #"
echo "#                                                                     #"
echo "#######################################################################"
sed -i 's/# DBPassword=/DBPassword=$Database_Password/' /etc/zabbix/zabbix_server.conf
if [ $release -eq '7' ];then
echo "php_value[date.timezone] = Asia/Shanghai" >> /etc/opt/rh/rh-php72/php-fpm.d/zabbix.conf
systemctl restart zabbix-server zabbix-agent httpd rh-php72-php-fpm
systemctl enable zabbix-server zabbix-agent httpd rh-php72-php-fpm
elif [ $release -eq '8' ];then
systemctl restart zabbix-server zabbix-agent httpd php-fpm
systemctl enable zabbix-server zabbix-agent httpd php-fpm
fi

echo "#######################################################################"
echo "#                 安装已经完成 请移步浏览器，进行下一步操作           #"
echo "#                 登录地址为http://$ip/zabbix                         #"
echo "#                 数据库密码为$Database_Password，尽情享用吧！#"
echo "#######################################################################"

```


# 升级编译安装的nginx----yum安装的nginx
cp -r /usr/local/nginx/conf /root/nginx_backup_$(date +%Y%m%d)/
/usr/local/nginx/sbin/nginx -s stop

# 安装 yum-utils（如未安装）
yum install -y yum-utils
yum list available nginx --showduplicates

# 添加 Nginx 官方仓库
cat > /etc/yum.repos.d/nginx.repo <<EOF
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/\$releasever/\$basearch/
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true

[nginx-mainline]
name=nginx mainline repo
baseurl=http://nginx.org/packages/mainline/centos/\$releasever/\$basearch/
gpgcheck=1
enabled=0
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true
EOF

yum install -y nginx
# 测试配置
nginx -t

# 启动服务（使用 systemd）
systemctl start nginx
systemctl enable nginx

# 检查状态
systemctl status nginx
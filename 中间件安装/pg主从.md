



# 前提条件

master 10.0.0.100

node 10.0.0.110



两台机器：主节点 10.0.0.100，从节点 10.0.0.110。

系统为 CentOS 7，已配置网络互通。

已安装 epel-release 和 PostgreSQL 源（如果未安装，按步骤 1 配置）

## 主 10.0.0.100

```
sudo yum install -y epel-release https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo yum install -y postgresql15-server postgresql15-contrib
sudo /usr/pgsql-15/bin/postgresql-15-setup initdb
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '10.0.0.100'/" /var/lib/pgsql/15/data/postgresql.conf
sudo sed -i "s/#wal_level = replica/wal_level = replica/" /var/lib/pgsql/15/data/postgresql.conf
sudo sed -i "s/#max_wal_senders = 10/max_wal_senders = 10/" /var/lib/pgsql/15/data/postgresql.conf
sudo sed -i "s/#wal_keep_size = 0/wal_keep_size = 128MB/" /var/lib/pgsql/15/data/postgresql.conf
echo "host replication replica 10.0.0.110/32 md5" | sudo tee -a /var/lib/pgsql/15/data/pg_hba.conf
sudo systemctl start postgresql-15
sudo -u postgres psql -c "CREATE ROLE replica REPLICATION LOGIN PASSWORD '123456';"
sudo systemctl enable postgresql-15
sudo systemctl restart postgresql-15
```

# 从 10.0.0.110

```
sudo yum install -y epel-release https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo yum install -y postgresql15-server postgresql15-contrib
sudo /usr/pgsql-15/bin/postgresql-15-setup initdb
sudo systemctl stop postgresql-15
sudo rm -rf /var/lib/pgsql/15/data/*
sudo -u postgres pg_basebackup -h 10.0.0.100 -D /var/lib/pgsql/15/data -U replica -P --wal-method=stream
sudo sed -i "s/#hot_standby = off/hot_standby = on/" /var/lib/pgsql/15/data/postgresql.conf
sudo -u postgres touch /var/lib/pgsql/15/data/standby.signal
echo "standby_mode = 'on'" | sudo tee /var/lib/pgsql/15/data/recovery.conf
echo "primary_conninfo = 'host=10.0.0.100 port=5432 user=replica password=123456'" | sudo tee -a /var/lib/pgsql/15/data/recovery.conf
sudo systemctl start postgresql-15
sudo systemctl enable postgresql-15
```

### 注意事项

- 替换 your_password 为你设置的强密码。
- 确保防火墙开放 5432 端口（如使用 firewalld：sudo firewall-cmd --add-port=5432/tcp --permanent; sudo firewall-cmd --reload）。
- 如果遇到问题，检查日志：/var/lib/pgsql/15/data/log/。
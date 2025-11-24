
<!-- more -->
# Logrotate日志分割

logrotate是一个linux系统日志的管理工具。可以对单个日志文件或者某个目录下的文件按时间/大小进行切割，压缩操作；指定日志保存数量；还可以在切割之后运行自定义命令。
```
yum install -y logrotate
```
## 创建目录

```
mkdir -p /var/log/nginx/access_logs
mkdir -p /var/log/nginx/error_logs
```
## 配置文件
```
vim /etc/logrotate.d/nginx
```

```
/var/log/nginx/*access.log {
    daily
    # 按天分割日志
    dateext
    # 使用日期作为分割后的日志文件的后缀
    dateformat -%Y-%m-%d
    # 指定日期格式为-YYYY-MM-DD
    missingok
    # 如果日志文件不存在，不报错继续处理下一个日志文件
    rotate 14
    # 保留14个旧的日志文件
    compress
    # 对分割后的日志文件进行压缩
    delaycompress
    # 延迟到下一个轮转周期压缩日志文件
    notifempty
    # 如果日志文件为空，不进行分割
    create 0640 nginx adm
    # 以0640的权限创建新的日志文件，并设置所有者和所属组为nginx和adm
    olddir /var/log/nginx/access_logs
    # 指定旧的日志文件存放位置
    sharedscripts
    # 脚本只运行一次而不是每个日志文件运行一次
    postrotate
    # 在日志分割后执行的脚本
        [ -f /var/run/nginx.pid ] && kill -USR1 `cat /var/run/nginx.pid`
    endscript
}

/var/log/nginx/*error.log {
    daily
    # 按天分割日志
    dateext
    # 使用日期作为分割后的日志文件的后缀
    dateformat -%Y-%m-%d
    # 指定日期格式为-YYYY-MM-DD
    missingok
    # 如果日志文件不存在，不报错继续处理下一个日志文件
    rotate 14
    # 保留14个旧的日志文件
    compress
    # 对分割后的日志文件进行压缩
    delaycompress
    # 延迟到下一个轮转周期压缩日志文件
    notifempty
    # 如果日志文件为空，不进行分割
    create 0640 nginx adm
    # 以0640的权限创建新的日志文件，并设置所有者和所属组为nginx和adm
    olddir /var/log/nginx/error_logs
    # 指定旧的日志文件存放位置
    sharedscripts
    # 脚本只运行一次而不是每个日志文件运行一次
    postrotate
    # 在日志分割后执行的脚本
        [ -f /var/run/nginx.pid ] && kill -USR1 `cat /var/run/nginx.pid`
    endscript
}
```
## 调试模式验证结果

```
logrotate -d /etc/logrotate.d/nginx
```
## 成功标志!



![alt%20text.webp](https://liu-fu-gui.github.io/myimg/halo/alt%20text.webp)

## 强制执行日志轮转

```
logrotate -vf /etc/logrotate.d/nginx
```
## 验证结果

```
ls /var/log/nginx/access_logs
ls /var/log/nginx/error_logs
```





## 示例

需求是： 每次分割后迁移到指定目录



问题的主要原因是 **`postrotate` 脚本在文件压缩之前执行**，

`logrotate` 提供了 `lastaction` 脚本，它在所有操作（包括压缩）完成后执行。将 `mv` 命令移到 `lastaction` 脚本中

```
[root@iZi5c01zlzty2isavfgor8Z nginx_log]# cat /etc/logrotate.d/nginx 
/opt/nginx_log/*.log {
    daily
    missingok
    rotate 1
    compress
    notifempty
    create 0644 root root
    sharedscripts
    dateext
    dateformat -%Y-%m-%d

    postrotate
        echo "运行后轮换脚本"
        if [ -f /usr/local/nginx/conf/nginx.pid ]; then
            echo "重启nginx"
            kill -USR1 `cat /usr/local/nginx/conf/nginx.pid`
        fi
    endscript

    lastaction
        echo "移动日志到 /mnt/data/nginx_log/"
        if find /opt/nginx_log/ -name "*.log-*.gz" -print -quit | grep -q .; then
            find /opt/nginx_log/ -name "*.log-*.gz" -exec /bin/mv {} /mnt/data/nginx_log/ \;
        else
            echo "未找到压缩日志文件。"
        fi
    endscript
}
```


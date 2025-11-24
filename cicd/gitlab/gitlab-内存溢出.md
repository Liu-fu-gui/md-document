## gitlab内存溢出问题
gitlab环境为14以上的版本
<!-- more -->
内存用段时间会越来越大，是因为用户量大，或者是线程多引起的并发问题

配置文件修改

```
 vim /etc/gitlab/gitlab.rb 
```

```
# 减少进程数
puma['worker_processes'] = 2
# 减少postgresql数据库缓存
postgresql['shared_buffers'] = "256MB"
# 减少数据库并发数
postgresql['max_worker_processes'] = 8
```
日志中可能会有这个报错

```
 chpst: fatal: unable to run: /opt/gitlab/embedded/sbin/nginx: file does not exist
```
解决方案

```
 cp /opt/gitlab/embedded/sbin/gitlab-web /opt/gitlab/embedded/sbin/nginx
```
因为14以上的版本，为了nginx冲突，把文件名字改为nginx-web
## 附上常用命令

```
gitlab-ctl start # 启动所有 gitlab 组件；
gitlab-ctl stop # 停止所有 gitlab 组件；
gitlab-ctl restart # 重启所有 gitlab 组件；
gitlab-ctl status # 查看服务状态；
gitlab-ctl reconfigure # 启动服务；刷新配置文件
vim /etc/gitlab/gitlab.rb # 修改默认的配置文件；
gitlab-rake gitlab:check SANITIZE=true --trace # 检查gitlab；
gitlab-ctl tail # 查看日志；
```
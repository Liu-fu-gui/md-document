# gitlab 安装
GitLab是一个基于Git的代码托管和协作平台，提供了从代码管理到持续集成/持续部署（CI/CD）的全方位 DevOps功能。本文为您介绍如何在Linux系统的ECS实例上部署GitLab，搭建属于您自己的代码托管平台。
## gitlab教程
https://help.aliyun.com/zh/ecs/use-cases/deploy-and-use-gitlab?spm=5176.28426678.J_HeJR_wZokYt378dwP-lLl.1.641d5181eqS7Fq&scm=20140722.S_help@@%E6%96%87%E6%A1%A3@@52857.S_BB1@bl+RQW@ag0+BB2@ag0+hot+os0.ID_52857-RL_gitlab%E5%AE%89%E8%A3%85-LOC_search~UND~helpdoc~UND~item-OR_ser-V_4-P0_0#b97489230dz58
## docker docker-compose安装参考
https://help.aliyun.com/zh/ecs/use-cases/install-and-use-docker-on-a-linux-ecs-instance?spm=a2c4g.11186623.0.0.d3b55906gFezl8
## 方式一：使用安装包
### gitlab社区版

```
# 添加GitLab包仓库。
curl -sS https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash
#安装GitLab所需的依赖包
sudo yum install -y curl python3-policycoreutils openssh-server
#安装GitLab社区版
sudo EXTERNAL_URL=<GitLab服务器的公网IP地址> sudo yum install -y gitlab-ce
```
低于4 vCPU、8 GiB会因规格过小，导致长时间处于Installing状态或安装失败
![20241213173239](https://liu-fu-gui.github.io/myimg/halo/20241213173239.png)
### 极狐gitlab

```
# 安装GitLab所需的依赖包
sudo yum install -y curl python3-policycoreutils openssh-server
# 添加GitLab软件包仓库
curl -fsSL https://get.gitlab.cn | sudo /bin/bash
# 安装GitLab。
sudo EXTERNAL_URL=<GitLab服务器的公网IP地址> yum install -y gitlab-jh
# <GitLab服务器的公网IP地址>请替换成安装GitLab的实例公网IP地址。
```
当出现类似如下回显信息，表示GitLab软件包仓库已安装。
![20241213173351](https://liu-fu-gui.github.io/myimg/halo/20241213173351.png)

## 方式二：使用Docker镜像

```
# 创建Docker容器的挂载数据目录，该目录会作为GitLab配置、日志和数据文件所在的目录。
sudo mkdir -p /srv/gitlab 
# 设置环境变量$GITLAB_HOME。
export GITLAB_HOME=/srv/gitlab
```
### gitlab社区版

```
sudo docker run --detach \
  --hostname gitlab.example.com \
  --publish 443:443 --publish 80:80 --publish 2222:22 \
  --name gitlab \
  --restart always \
  --volume $GITLAB_HOME/config:/etc/gitlab \
  --volume $GITLAB_HOME/logs:/var/log/gitlab \
  --volume $GITLAB_HOME/data:/var/opt/gitlab \
  --shm-size 256m \
  gitlab/gitlab-ce:latest
```
#### 修改配置文件
```
[root@tiaoban gitlab]# vim config/gitlab.rb
external_url 'http://gitlab.test.com'
gitlab_rails['gitlab_ssh_host'] = '192.168.10.100'
gitlab_rails['time_zone'] = 'Asia/Shanghai'
gitlab_rails['gitlab_shell_ssh_port'] = 8022
# 解决头像显示异常问题
gitlab_rails['gravatar_plain_url'] = 'http://gravatar.loli.net/avatar/%{hash}?s=%{size}&d=identicon'
gitlab_rails['gravatar_ssl_url'] = 'https://gravatar.loli.net/avatar/%{hash}?s=%{size}&d=identicon'
# 关闭 promethues和alertmanager
prometheus['enable'] = false
alertmanager['enable'] = false
# 默认gitlab配置资源占用较高，可以根据情况减少资源占用
# 关闭邮件服务
gitlab_rails['gitlab_email_enabled'] = false
gitlab_rails['smtp_enable'] = false
# 减少 postgresql 数据库缓存
postgresql['shared_buffers'] = "128MB"
# 减少 postgresql 数据库并发数量
postgresql['max_connections'] = 200
# nginx减少进程数
nginx['worker_processes'] = 2
[root@tiaoban gitlab]# docker exec -it gitlab bash
root@gitlab:/# gitlab-ctl reconfigure
gitlab Reconfigured!
root@gitlab:/# gitlab-ctl restart
```
#### 服务控制
```
[root@tiaoban gitlab]# docker restart gitlab
[root@tiaoban gitlab]# docker start gitlab
[root@tiaoban gitlab]# docker stop gitlab
[root@tiaoban gitlab]# docker rm gitlab
```
#### 客户端添加hosts记录
修改hosts文件，添加如下记录gitlab.test.com 192.168.10.100，然后浏览器访问即可。
### 极狐版

```
sudo docker run --detach \
  --hostname gitlab.example.com \
  --publish 443:443 --publish 80:80 --publish 2222:22 \
  --name gitlab \
  --restart always \
  --volume $GITLAB_HOME/config:/etc/gitlab \
  --volume $GITLAB_HOME/logs:/var/log/gitlab \
  --volume $GITLAB_HOME/data:/var/opt/gitlab \
  --shm-size 256m \
  registry.gitlab.cn/omnibus/gitlab-jh:latest
```
| 目录路径                   | 说明             |
|----------------------------|------------------|
| `$GITLAB_HOME/data`         | /var/opt/gitlab  | 存储应用程序数据。  |
| `$GITLAB_HOME/logs`         | /var/log/gitlab  | 存储GitLab日志。    |
| `$GITLAB_HOME/config`       | /etc/gitlab      | 存储GitLab配置文件。|

查看容器状态。

 
```
docker ps -a
```
![20241213174036](https://liu-fu-gui.github.io/myimg/halo/20241213174036.png)
当容器状态为healthy时，说明GitLab容器已经正常启动。

## 进入GitLab管理页面

1. 在浏览器输入网址。访问网址：http://${ECS的公网IP}。
2. 首次登录使用用户名root，通过如下方式获取密码。

获取GitLab的登录密码。在ECS实例执行以下命令：

```
Linux安装包方式：sudo cat /etc/gitlab/initial_root_password
Docker安装方式：sudo docker exec -it gitlab grep 'Password:' /etc/gitlab/initial_root_password
```
回显信息类似如下所示，您可以在Password后获取GitLab的初始登录密码。

![20241213174144](https://liu-fu-gui.github.io/myimg/halo/20241213174144.png)

> 出于安全原因，24小时后该文件会被自动删除。建议您安装成功首次登录之后，修改GitLab的初始密码，操作步骤参见https://gitlab.cn/docs/jh/security/reset_user_password.html


![20241213174232](https://liu-fu-gui.github.io/myimg/halo/20241213174232.png)

3. 进入Admin页面。
![20241213174324](https://liu-fu-gui.github.io/myimg/halo/20241213174324.png)
4. 在Users>Pending approval页面审批新用户的申请。其他操作例如管理项目、管理用户等，请参见
![20241213174344](https://liu-fu-gui.github.io/myimg/halo/20241213174344.png)

> **说明**  
> 本示例介绍将文件上传到 GitLab 仓库的步骤。如果您想了解更多的 GitLab 操作，例如 GitLab 常用命令、数据备份、配置选项、用户管理、与其他服务集成、故障排除等，请参见极狐 GitLab 和 GitLab 社区版。
> https://docs.gitlab.cn/
https://docs.gitlab.com/ee/topics/git/


## 注册用户并设置免密访问
1. 访问GitLab页面。单击Sign in按钮下的Register now，创建一个新用户。等待GitLab管理员通过申请后，使用新创建的用户登录GitLab。

2. 在本地生成密钥对文件。
```
ssh-keygen
```

生成密钥对的过程中，系统会提示输入密钥对存放目录（默认为当前用户目录下的.ssh/id_rsa，例如/home/test/.ssh/id_rsa）和密钥对密码，您可以手动输入，也可以按Enter保持默认。

回显信息类似如下所示。
![20241213174552](https://liu-fu-gui.github.io/myimg/halo/20241213174552.png)

3. 查看并复制公钥文件id_rsa.pub中的内容，便于后续步骤使用。

```
cat ~/.ssh/id_rsa.pub
```
回显信息类似如下所示。

```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDQVwWjF3KXmI549jDI0fuCgl+syJjjn55iMUDRRiCd/B+9TwUda3l9WXH5i7RU53QGRCsDVFZxixLOlmXr9E3VSqkf8xXBnHs/5E2z5PIOCN0nxfB9xeA1db/QxPwK4gkHisep+eNHRn9x+DpCYDoSoYQN0nBg+H3uqfOqL42mJ+tqSfkyqbhjBf1kjtDTlBfVCWtI0siu7owm+c65+8KNyPlj5/0AyJ4Aqk1OX2jv+YE4nTipucn7rHwWuowasPU86l+uBsLNwOSb+H7loJvQyhEINX2FS1KnpRU+ld20t07n+N3ErfX5xBAGfxXpoN9BKKSP+RT7rvTeXTVE**** test@iZuf63zs0dn0qccsisy****
```

4. 添加SSH key。将获取的公钥添加了GitLab账户中，以便进行免密码的身份验证。

- 4.1单击页面右上角的头像，然后单击Edit profile。

![20241213175000](https://liu-fu-gui.github.io/myimg/halo/20241213175000.png)

 -  4.2在左侧导航栏，单击SSH Keys。将公钥文件id_rsa.pub中的内容粘贴到Key所在的文本框中，然后单击Add key。

![20241213175017](https://liu-fu-gui.github.io/myimg/halo/20241213175017.png)
 - 4.3 SSH Key添加完成后，如下图所示。

![20241213175037](https://liu-fu-gui.github.io/myimg/halo/20241213175037.png)

## 创建项目并托管代码
### 创建新项目
1. 在GitLab的主页中，单击页面右侧的New Project按钮，然后单击Create blank project。
![20241213175106](https://liu-fu-gui.github.io/myimg/halo/20241213175106.png)

2. 单击Create blank project，设置Project name和Project URL，然后单击页面底部的Create project。本文以mywork项目为例进行说明。

![20241213175121](https://liu-fu-gui.github.io/myimg/halo/20241213175121.png)
3. 回到项目页面，复制SSH克隆地址，该地址在进行克隆操作时需要使用。
![20241213175135](https://liu-fu-gui.github.io/myimg/halo/20241213175135.png)

## 克隆远程仓库
1. 在本地安装Git。

```
sudo yum install git
```

2. 在本地配置使用Git仓库的人员信息。

- 2.1 配置使用Git仓库的用户名。

```
git config --global user.name "testname" 
```
- 2.2 配置使用Git仓库的人员邮箱。

```
git config --global user.email "abc@example.com" 
```
3. 克隆已创建的项目到本地。

- 3.1 输入git clone并粘贴SSH克隆地址，Git会自动创建一个以仓库名称命名的文件夹并下载文件。
```
git clone ${SSH URL}
```
如果使用Docker镜像安装，需要在链接中添加ssh://和docker run命令中的映射端口，例如：


> **说明**  
> 如果不想修改SSH链接，需要修改gitlab_rails['gitlab_shell_ssh_port'] 参数，


```
git clone ssh://git@{IP域名}:{SSH端口}/root/mywork
```
![20241213175437](https://liu-fu-gui.github.io/myimg/halo/20241213175437.png)

- 3.2 进入到项目目录。

```
cd mywork/ 
```

- 3.3查看当前分支的名称，默认为主分支main。

```
git branch
```
## 新建分支并进行更改
在本地创建新的分支，便于更改文件。

1. 新建一个分支example。

```
git checkout -b example  
```  
2. 新建需要上传到GitLab中的目标文件test.txt，并写入内容Hello world!。

```
echo "Hello world!" > test.txt
```
### 提交并推送更改
将新分支example推送到远程仓库进行保存。

1. 将test.txt文件添加到暂存区。

```
git add test.txt
```

2. 确认变更的文件。

```
git status
```
获得以下输出：
```
On branch example
Changes to be committed:
  (use "git restore --staged <file>..." to unstage)
        modified:   test.txt
```
3. 提交暂存文件test.txt。

```
git commit -m "测试用"
```
4. example分支目前只在本地可用。将分支推送到GitLab仓库，便于其他人访问。

 
```
git push origin example
```
5. 推送到GitLab仓库后，其他用户也可以查看新建的分支。

![20241213181739](https://liu-fu-gui.github.io/myimg/halo/20241213181739.png)

## 合并更改
将本地example 分支的更改合并到主分支 main，然后将合并后的主分支 main推送到远程仓库。

1. 切换到主分支main。

```
git checkout main
```
2. 将新建分支example 合并到主分支main。

```
git merge example
```
3. 将合并后的主分支推送到GitLab仓库。

```
git push
```
4. 变更已同步到GitLab仓库的主分支main中。
![20241213181834](https://liu-fu-gui.github.io/myimg/halo/20241213181834.png)

![20241213181855](https://liu-fu-gui.github.io/myimg/halo/20241213181855.png)
## 配置邮件通知
1. https://gitlab.cn/docs/omnibus/settings/smtp.html
2. 验证SMTP配置效果。

- 2.1执行gitlab-rails console命令进入Rails控制台。

- 2.2 输入以下命令，发送测试电子邮件：

```
Notify.test_email('destination_email@address.com', 'Message Subject', 'Message Body').deliver_now
```
- 2.3 在目标邮箱中查看测试邮件，或者在ECS实例中执行sudo tail -f /var/log/mail.log查看邮件的日志。

## 备份gitlab仓库的数据
在ECS上安装GitLab后，GitLab仓库的数据通常会存储在服务器的/var/opt/gitlab/git-data路径中，仓库存储在名为repositories的子文件夹中
## 配置gitlab 复制域名
![20241213182145](https://liu-fu-gui.github.io/myimg/halo/20241213182145.png)
修改gitlab.rb中的external_url参数，配置完成后如下图所示。
- 安装包方式：[配置 GitLab 的外部 URL](https://docs.gitlab.com/omnibus/settings/configuration.html)
- Docker镜像方式：[编辑配置文件。](https://docs.gitlab.com/ee/install/docker/configuration.html)
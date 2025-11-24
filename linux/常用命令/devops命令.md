## yq — 解析和修改 YAML
Yq 是一个轻量级且便携的命令行 YAML 处理器。更多信息：https://mikefarah.gitbook.io/yq/。

1. YAML 文件在 DevOps 中无处不在，尤其是在配置管理中。`yq` 命令是一个强大的工具，用于解析和修改这些文件。让我们使用`yq` 检查一个部署配置文件。

```
cat deploy-config.yaml
```

```
app:
  name: mywebapp
  version: 1.0.0
  image: nginx:latest
  replicas: 3
database:
  image: postgres:13
  password: secretpassword
```

```
yq '.app.image' deploy-config.yaml
```

输出为

```
nginx:latest
```

2. 安装步骤

   ```
   yum install -y epel-release
   yum install -y python3-pip
   pip3 install yq
   yum install -y jq
   ```

   

**2. sed 和 grep — 更新配置**

`Sed` 允许以可编程方式编辑文本。另见：`awk`,`ed`。更多信息：https://manned.org/man/sed.1posix。

当需要新版本时，更新配置文件是一项常规任务。`sed` 和`grep` 的组合使这一过程无缝进行。以下是如何在我们的 YAML 文件中更新版本：

本：

```
sed -i 's/version: 1.0.0/version: 1.1.0/' deploy-config.yaml
grep version deploy-config.yaml
```

使用正则表达式在文件中查找模式。更多信息：https://www.gnu.org/software/grep/manual/grep.html。

`Sed` 将更新版本，我们可以快速`grep` 以确认更改。

# **3. curl — 检查部署状态**

从服务器传输数据。支持大多数协议，包括 HTTP、FTP 和 POP3。更多信息：curl 手册页https://curl.se/docs/manpage.html。

监控部署 API 的状态至关重要。`curl` 命令允许您检查 API 状态并解析响应。例如，检查 Kubernetes 的最新版本：

```
curl -s 'https://api.github.com/repos/kubernetes/kubernetes/releases/latest' | yq '.tag_name'
```

这将为我们提供最新`Kubernets` 版本的标签名称。

# **4. tee — 记录部署步骤**

**应用场景一**就是有时候我们希望操作命令既显示到屏幕又保存到文档，tee命令是我们的不二选择；

**应用场景二**是重复展示输入内容；

**应用场景三**是可以将文件同时复制多份。当然tee命令还可以与其他命令结合使用，组合达到我们期待的效果。

记录部署步骤可确保您有执行内容的轨迹。`tee` 命令非常适合此用途：

```
echo 'Starting deployment process' | tee deployment.log
echo 'App version: 1.1.0' | tee -a deployment.log
cat deployment.log
```

# **5. watch — 监控部署进度**

`Watch` 定期执行程序，显示输出。更多信息：watch https://manned.org/watch。

`watch` 命令非常适合实时监控。例如，通过持续监控 Kubernetes pod 的状态，您可以随时了解部署进度：

```
watch kubectl get pods
```

> `kubectl` 有自己的 `— watch` 标志，我们可以在这种特定情况下使用：
>
> ```
> kubectl get pods — watch
> ```

# **6. journalctl — 查看系统日志**

由于大多数时间您将使用基于 Linux 的虚拟机，`journald` 可以查询`systemd` 日志。更多信息：journalctl https://manned.org/journalctl。

系统日志对于故障排除至关重要。`journalctl` 命令帮助您查看和过滤这些日志。例如，查看特定服务的日志：

```
journalctl -u nginx.service | tail
```
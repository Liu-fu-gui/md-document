# k8s集群命令行工具kubectl之故障排除和调试命令
<!-- more -->
## 一、describe
显示某个资源或某组资源的详细信息。

用法：

kubectl describe (-f FILENAME | TYPE [NAME_PREFIX | -l label] | TYPE/NAME)
示例：

```
# 显示单个node节点详细信息
kubectl describe nodes k8s-node1
# 显示单个pod详细信息
kubectl describe pods/nginx
# 显示文件描述的资源的详细信息
kubectl describe -f myapp-deployment.yaml
# 显示以k8s开头的节点的详细信息
kubectl describe node k8s
# 显示以myapp-deployment开头的pod的详细信息，pod命名通常与其控制器有关
kubectl describe pods myapp-deployment
# 显示指定label的pod详细信息
kubectl describe po -l name=myapp
```
## 二、logs
输出 pod 中某容器的日志。

用法：

kubectl logs [-f] [-p] (POD | TYPE/NAME) [-c CONTAINER]


示例：
（1）首先：
```
# 运行一个nginx Pod
kubectl run nginx --image=nginx
```
（2）其次：
```
# 获取pod第一个容器的日志
kubectl logs nginx
# 获取pod中所有容器的日志
kubectl logs <podname> --all-containers=true
# 获取labels包含 name=myapp的所有pod下的所有容器日志
kubectl logs -l name=myapp --all-containers=true
# 持续输出pod中某个容器产生的日志，容器名为 myhello
kubectl logs -f -c myhello <podname>
# 持续输出labels包含 name=myapp的所有pod下的所有容器日志，最大并发日志请求数为10
kubectl logs -f -l name=myapp --all-containers=true --max-log-requests=10
# 获取最近几行日志
kubectl logs --tail=5 nginx
# 获取最近一个小时的日志
kubectl logs --since=1h nginx
# 获取pod中第一个容器的日志
kubectl logs pod/<podname>
# 获取指定deployment中，第一个pod的指定容器的日志。默认日志请求并发数为5
kubectl logs deployment/myapp-deployment -c myhello
```
3. 更多示例：

```
# 输出pod web-1中曾经运行过的，但目前已终止的容器ruby的日志
kubectl logs -p -c ruby web-1
```
## 三、attach
连接到一个正在运行的容器。

用法：

kubectl attach (POD | TYPE/NAME) -c CONTAINER


示例：

```
# 连接到指定pod中正在运行的第一个容器
kubectl attach <podname>
# 连接到指定pod中正在运行容器名为 myhello的容器
kubectl attach <podname> -c myhello
# 连接到指定deployments正在运行的第一个容器
kubectl attach deployments/myapp-deployment
```



## 四、exec
在容器中执行相关命令。

用法：

kubectl exec (POD | TYPE/NAME) [-c CONTAINER] [flags] -- COMMAND [args...]


示例：

```
# 在pod nginx 第一个容器中执行date命令
kubectl exec nginx -- date
# 通过-c 指定容器
kubectl exec <podname> -c myhello -- date
# 传入 ls命令和相关参数
kubectl exec <podname> -c myhello -- ls -al ./
# 通过 -it 开启一个虚拟终端
kubectl exec <podname> -c myhello -i -t -- /bin/sh
# deployment/myapp-deployment第一个容器中执行命令
kubectl exec deployments/myapp-deployment -- date
# svc/myapp-svc 第一个容器中执行命令
kubectl exec svc/myapp-svc -- date
```



## 五、port-forward
将一个或者多个本地端口转发到 pod。如果有多个pod符合条件，将自动选择一个pod。当所选pod终止时，转发会话结束，需要重新运行该命令才能恢复转发。



用法：

kubectl port-forward TYPE/NAME [options] [LOCAL_PORT:]REMOTE_PORT [... [LOCAL_PORT_N:]REMOTE_PORT_N]


示例：

```
# 转发本机5000 6000 端口到pod对应端口
kubectl port-forward pod/nginx 5000 6000
# 转发本机 5000 6000 分别到80和81端口
kubectl port-forward pod/nginx 5000:80 6000:81
# 从deployments/myapp-deployment 选择一个pod进行转发操作
kubectl port-forward deployments/myapp-deployment 5000 6000
# 从service中选择第一个pod，并将端口转发到端口名为http的端口上
kubectl port-forward service/myapp-svc 8080:http
# 通过--address指定监听地址
kubectl port-forward --address localhost,192.168.239.142 pod/nginx 8888:80
# 随机一个本地端口，转发到pod的指定端口
kubectl port-forward pod/nginx :80
```



## 六、proxy
在本地主机和Kubernetes API服务器之间创建代理服务器或应用程序级网关。它还允许通过指定的HTTP路径提供静态内容。所有传入数据都通过一个端口进入并转发到远程Kubernetes API服务器端口，但与静态内容路径匹配的路径除外。



用法：

kubectl proxy [--port=PORT] [--www=static-dir] [--www-prefix=prefix] [--apiprefix=prefix]


示例：

```
kubectl proxy --api-prefix=/custom/ --port=8011 --www=$HOME/ --wwwprefix=/static/

# 通过代理访问apiserver接口
curl http://127.0.0.1:8011/custom/api/v1/pods
# 通过代理访问静态内容
curl http://127.0.0.1:8011/static/
```



## 七、cp
将文件和目录拷入/拷出容器。

用法：

kubectl cp <file-spec-src> <file-spec-dest>


示例：

```
# 把pod第一个容器中当前工作目录下的app 复制到宿主机的/tmp/app
kubectl cp <podname>:app /tmp/app
# 把/tmp/app 文件复制到 pod第一个容器当前工作目录下，命名为app1
kubectl cp /tmp/app <podname>:app1
# 把default命名空间下pod中容器myhello中当前工作目录下的app 复制到宿主机的/tmp/app
kubectl cp default/<podname>:app /tmp/app -c myhello
# 查看容器当前工作目录内容
kubectl exec <podname> ls 
```

## 八、debug
创建用于排查工作负载和节点故障的调试会话。

用法：

kubectl debug (POD | TYPE[[.VERSION].GROUP]/NAME) [ -- COMMAND [args...] ]


### 8.1、案例1：共享进程空间

```
# 运行一个nginx pod
kubectl run nginx --image=nginx
# 创建一个新的pod my-debugger 用来调试，
#将原有pod内的容器拷贝到新的pod，并增加一个镜像为ubuntu的容器
# 并且通过进程共享
kubectl debug nginx -it --image=ubuntu --copy-to=my-debugger --share-processes
# 开启另一个终端，可以查看命名空间共享配置
kubectl get pod my-debugger -o yaml | grep shareProcessNamespace   
```

调试容器内，执行ps ax可以看到包括被调试容器在内的所有进程。

![20241129230227](https://liu-fu-gui.github.io/myimg/halo/20241129230227.png)


通过访问/proc/{PID}/root 可访问到其他容器的文件系统，PID为主进程ID。

![20241129230239](https://liu-fu-gui.github.io/myimg/halo/20241129230239.png)



### 8.2、案例2：更改启动命令、容器镜像
    

```
 # 运行一个myhello的pod
kubectl run myhello --image=xlhmzch/hello:1.0.0
# 更改容器启动命令
kubectl debug myhello -it --copy-to=my-debugger1 --container=myhello -- /bin/sh
# 更改容器镜像
kubectl debug myhello -it --copy-to=my-debugger2 --set-image=myhello=xlhmzch/hello:1.0.1
kubectl debug <podname> -it --copy-to=my-debugger3 --set-image=myhello=xlhmzch/hello:1.0.1,myredis=redis:alpine
# 复制并注入临时容器，共享进程空间同时修改myhello容器的镜像
kubectl debug myhello -it --copy-to=my-debugger4 --image=busybox --setimage=myhello=xlhmzch/hello:1.0.1 --share-processes
```


### 8.3、案例3：调试节点
    
```
  # 通过创建Pod来调试节点，Pod将运行在指定的节点上，节点的根文件系统挂载在/host目录下
kubectl debug node/k8s-node1 -it --image=busybox  
```



### 8.4、其他
其他，需要为集群开启临时容器等特性功能，否则无法使用以下操作。

```
 # 直接在指定的pod中创建一个基于busybox的临时容器
kubectl debug <podname> -it --image=busybox
# 直接在指定的pod中创建一个基于自定义镜像的容器，并且为容器指定容器名为debugger
kubectl debug <podname> --image=<cusimage> -c debugger   
```



<!-- more -->

## 初始化

```
yum install -y nfs-utils 
systemctl enable --now nfs-server 
systemctl status nfs-server
```

添加了 NFS Subdir External Provisioner 的 Helm 仓库
```
helm repo add nfs-subdir-external-provisioner https://charts.helm.sh/incubator
helm repo list
helm repo update
```
安装

```
helm install nfs-client nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  --set nfs.server=10.0.0.100 \
  --set nfs.path=/home/nfs \
  --set storageClass.name=nfs-storage
```
# 安装后检查

```
kubectl get pods -n kube-system
```
## 测试

```
chmod 777 /home/nfs
```

```
[root@4c8-k8s-all ~]$ cat nfs-ceshi.yaml 
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-deployment
  namespace: default 
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nfs
  template:
    metadata:
      labels:
        app: nfs
    spec:
      containers:
      - name: nfs-container
        image: alpine
        command: ["/bin/sh", "-c", "while true; do sleep 30; done;"] 
        volumeMounts:
        - mountPath: /mnt/nfs
          name: nfs-storage
      volumes:
      - name: nfs-storage
        nfs:
          server: 10.0.0.100
          path: /home/nfs

```


```
kubectl exec -it nfs-deployment-6c4f87ffcd-tsbts -- /bin/sh
touch /mnt/nfs/test-file
ls /mnt/nfs
```
ls能出来东西就说明是成功的





# 使用 Helm 安装 NFS 存储

## 步骤 1：安装 Helm
如果还没有安装 Helm，可以参考官方文档进行安装：[Helm 安装文档](https://helm.sh/docs/intro/install/)

## 步骤 2：添加 Helm 仓库
首先，添加 appstore 仓库来获取 nfs-subdir-external-provisioner 和 nfs-server 的 Helm Chart。

```bash
helm repo add appstore https://charts.grapps.cn
helm repo update appstore
```





## 步骤 3：部署 nfs-server

使用 Helm 安装 nfs-server，它将作为 NFS 服务器提供存储服务。

```
helm install nfs-server appstore/nfs-server --version 1.1.2
```

在执行此命令时，nfs-server 会被部署到你的 Kubernetes 集群中。你可以根据需求定制安装参数（如 NFS 存储路径等）。安装后，检查 NFS 服务器的状态：

```
kubectl get pods
kubectl get svc
```

## 步骤 4：部署 nfs-subdir-external-provisioner

接下来，安装 nfs-subdir-external-provisioner，它是一个外部存储类，可以使用 NFS 提供动态存储。

```
helm install nfs-subdir-external-provisioner appstore/nfs-subdir-external-provisioner --version 4.0.9
```

安装时，你可能需要自定义一些参数，如 NFS 服务器地址和共享路径。例如，你可以通过 `--set` 来设置这些参数：

```
helm install nfs-subdir-external-provisioner appstore/nfs-subdir-external-provisioner --version 4.0.9 \
  --set nfs.server=<NFS_SERVER_IP> \
  --set nfs.path=<NFS_SHARE_PATH>
```

## 步骤 5：验证安装

确保两个服务都已成功启动，并且相关的 PVC 也能正常工作。通过以下命令检查 Pod 和 Service 状态：

```
kubectl get pods -n default
kubectl get svc -n default
```

如果你想创建 PVC 并且让 nfs-subdir-external-provisioner 动态供给存储，可以创建一个 PVC 来测试是否工作正常。例如：

```
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: nfs-pvc
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 10Gi
  storageClassName: nfs-client
```

## 步骤 6：使用动态存储

在部署应用时，使用 PVC 来动态挂载存储。例如，以下是一个示例部署，它使用了 `nfs-pvc`：

```
yaml复制apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-test-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nfs-test
  template:
    metadata:
      labels:
        app: nfs-test
    spec:
      containers:
      - name: nfs-test
        image: busybox
        volumeMounts:
        - mountPath: /mnt/data
          name: nfs-storage
      volumes:
      - name: nfs-storage
        persistentVolumeClaim:
          claimName: nfs-pvc
```

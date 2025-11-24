升级前版本：1.28.2



![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAX72mAsYomcpEIW5UTsPNtaMPWmktfVygSxGIVFL7icNb3rWMEjEBVxg/640?wx_fmt=png&from=appmsg)

升级后版本：1.29.10

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAtnHaOjagxe1ECeOK2XBnEdfzQYUicicxOcdcwb7o6n3bbnHt35vZS5lQ/640?wx_fmt=png&from=appmsg)



## **升级概要**

- 升级集群版本建议逐步升级，k8s版本以 x.y.z 表示，其中 x 是主要版本， y 是次要版本，z 是补丁版本,尽量不能跳过次要版本升级，比如1.28.0->1.30.0可能遭遇失败，补丁版本可以跳跃更新，比如1.28.2->1.28.10
- 尽量将kubelet和kubeadm版本保持一致，可以偏差一个版本
- 升级后，因为容器spec的哈希值已更改，所有容器都会被重新启动



**kubernetes官方升级参考：**

- 

```
https://v1-29.docs.kubernetes.io/zh-cn/docs/tasks/administer-cluster/kubeadm/kubeadm-upgrade/
```

**
**

**升级工作的基本流程如下：**

- 升级控制节点（master）
- 升级其他控制平面节点（高可用集群） 
- 升级工作节点（worker）



# **更改软件包仓库（所有节点）**

现在新版本都使用单独的软件仓库，需要修改里面的版本值，如你需要1.29版本，就需要将版本改为v1.29。

```
cat <<EOF | tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.29/rpm/
enabled=1
gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes-new/core/stable/v1.29/rpm/repodata/repomd.xml.key
EOF
```

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAjw5icrBPfibzYDrLDRWia1E3S6vCOna4151Wt2Aa2y7FvqIGoAYibJAWSA/640?wx_fmt=png&from=appmsg)

将软件仓库配置分发到node节点

```
scp /etc/yum.repos.d/kubernetes.repo root@10.0.0.101:/etc/yum.repos.d
scp /etc/yum.repos.d/kubernetes.repo root@10.0.0.102:/etc/yum.repos.d
```

# **升级前备份**

```
# 备份目录
cp -a /etc/kubernetes/  /etc/kubernetes.bak

cp -a /var/lib/etcd   /var/lib/etcd.bak

# 备份etcd数据
ETCDCTL_API=3 etcdctl snapshot save /data/etcd/etcd_bak.db \
--endpoints=https://127.0.0.1:2379 \
--cacert=/etc/kubernetes/pki/etcd/ca.crt \
--cert=/etc/kubernetes/pki/etcd/server.crt \
--key=/etc/kubernetes/pki/etcd/server.key 

# 如果没有etcdctl工具需要安装一下
yum install -y etcd
```

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAqyoRgBmPF8TbFXtFrzEB1MOWb5BYKkTUFgcCJy91whVgUXoicdnP7Qw/640?wx_fmt=png&from=appmsg)

# **控制平台节点（master）升级**

## **1 升级kubeadm**

```
yum install -y kubeadm-'1.29.10-*' --disableexcludes=kubernetes
```

**说明：--disableexcludes=kubernetes：禁掉除了这个kubernetes之外的别的仓库**

## **2 验证下载操作正常，并且 kubeadm 版本正确**

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAxMicdvlkrm0hTT9pNlYwbth7sN8uNPu9qpAHYDTUFjYeVQep0ia521nw/640?wx_fmt=png&from=appmsg)可见版本已经升级到1.29.10

## **3、验证升级计划：**

```
kubeadm upgrade plan
```

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAoweMX2z5flQu3WRX5kpLcRFMejILbvKlwS6XsahderuFfLm5MkymTA/640?wx_fmt=png&from=appmsg)

此命令检查你的集群是否可被升级，并取回你要升级的目标版本。命令也会显示一个包含组件配置版本状态的表格。

## **4 选择要升级到的目标版本**

```
kubeadm upgrade apply v1.29.10
```

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAXTsLfAfPYRfML7Xic185D0VCIKqP5uWiafTJDPlBIxo8ZYibNnRrGbDTg/640?wx_fmt=png&from=appmsg)

## **5 腾空节点**

将节点标记为不可调度并驱逐所有负载，准备节点的维护：

```
# 将 <node-to-drain> 替换为你要腾空的控制面节点名称
kubectl drain <node-to-drain> --ignore-daemonsets

kubectl drain k8s-master --ignore-daemonsets
```

**说明：**--ignore-daemonsets 忽略DaemonSet管理下的Pod![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsA3lOywzyaPBW7v3VQbfPu24aeoic8h1KqtTPoldiaLzibGf6ZX95oibmEdw/640?wx_fmt=png&from=appmsg)

## **6 升级 kubelet 和 kubectl**

（1）升级 kubelet 和 kubectl：

```
yum install -y kubelet-'1.29.10-*' kubectl-'1.29.10-*' --disableexcludes=kubernetes
```

（2）重启 kubelet：

```
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

## **7 解除节点的保护**

```
# 将 <node-to-uncordon> 替换为你的节点名称
kubectl uncordon <node-to-uncordon>

kubectl uncordon k8s-master
```

此时master节点已经升上去了。

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsApib0w79eC8jEwIlP3w8BTlQggBJxehQeuv4xNtvgHNOicneykULPCnvA/640?wx_fmt=png&from=appmsg)

# **升级工作节点**

工作节点上的升级过程应该一次执行一个节点，以不影响运行工作负载所需的最小容量。

我的集群有两个工作节点，先升级节点1，再升级2，步骤是一样的

## **1 升级 kubeadm**

```
# 将 1.29.x-* 中的 x 替换为最新的补丁版本
sudo yum install -y kubeadm-'1.29.x-*' --disableexcludes=kubernetes

yum install -y kubeadm-'1.29.10-*' --disableexcludes=kubernetes
```

## **2 执行 "kubeadm upgrade"**

对于工作节点，下面的命令会升级本地的 kubelet 配置：

```
kubeadm upgrade node
```

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsAk3GuiclRXIfyBL7pEfKsO30Ds3WWsN84rY8lgwdZbxFIV2WIe8rUxbQ/640?wx_fmt=png&from=appmsg)

## **3 腾空节点（master上执行）**

将节点标记为不可调度并驱逐所有负载，准备节点的维护：

```
# 将 <node-to-drain> 替换为你正腾空的节点的名称

节点1：
kubectl drain k8s-node1 --ignore-daemonsets

节点2：
kubectl drain k8s-node2 --ignore-daemonsets
```

## **4 升级 kubelet 和 kubectl**

**（1）升级 kubelet 和 kubectl**

尽量跟kubeadm版本保持一致

```
yum install -y kubelet-'1.29.10-*' kubectl-'1.29.10-*' --disableexcludes=kubernetes
```

**（2）重启 kubelet**

```
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

## **5 解除节点的保护（master上执行）**

通过将节点标记为可调度，让节点重新上线：

```
# 在控制平面节点上执行此命令
# 将 <node-to-uncordon> 替换为你的节点名称
kubectl uncordon <node-to-uncordon>

节点1：
kubectl uncordon k8s-node1

节点2：
kubectl uncordon k8s-node2
```

## **6 查看节点情况**

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsArvhSBfza3r39tcBHBiayvjE9WndjCRJ0m9TLLiajAhQNwhv2jTHBibykg/640?wx_fmt=png&from=appmsg)

**其他工作节点也按照升级工作节点来操作的步骤进行操作**

最后结果：

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/86HCic5IM7OddZs6mTKdMvBRUDTUsrzsATkqKibR9foqKymGdKOsSBIfXI5zc4VgwGP9XwJu7rtCkjdrBexjhia7w/640?wx_fmt=png&from=appmsg)

升级成功后，需要确保所有的对象资源的状态跟升级前是一样的。
## SSH公钥
1. 生成 sshkey:
```
ssh-keygen -t rsa -C "xxxxx@xxxxx.com"  
```
2. 按照提示完成三次回车，即可生成 ssh key。通过查看 ~/.ssh/id_rsa.pub 文件内容，获取到你的 public key
```
cat ~/.ssh/id_rsa.pub 
```
输出结果
```
# ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC6eNtGpNGwstc.... 
```
![20241213184250](https://liu-fu-gui.github.io/myimg/halo/20241213184250.png)
3. 复制生成后的 ssh key，通过仓库主页 「管理」->「部署公钥管理」->「添加部署公钥」 ，添加生成的 public key 添加到仓库中。

![20241213184310](https://liu-fu-gui.github.io/myimg/halo/20241213184310.png)
4. 添加后，在终端中输入
```
ssh -T git@gitee.com
```
首次使用需要确认并添加主机到本机SSH可信列表。若返回 
```
Hi XXX! You've successfully authenticated, but Gitee.com does not provide shell access. 
```

内容，则证明添加成功。
![20241213184333](https://liu-fu-gui.github.io/myimg/halo/20241213184333.png)

添加成功后，就可以使用SSH协议对仓库进行操作了。
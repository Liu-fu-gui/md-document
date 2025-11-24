
<!-- more -->
## 官方yum源
https://nginx.org/en/linux_packages.html#RHEL


![2b6607dc28d4c3dc3a0ae10ff53306c3.image](https://liu-fu-gui.github.io/myimg/halo/2b6607dc28d4c3dc3a0ae10ff53306c3.image.webp)

## 欧拉系统等于contos8
### cat nginx.repo
```
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/8/x86_64
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/8/x86_64
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true

```
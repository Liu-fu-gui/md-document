

```
C:\Users\Administrator>wsl --version
WSL 版本： 2.3.26.0
内核版本： 5.15.167.4-1
WSLg 版本： 1.0.65
MSRDC 版本： 1.2.5620
Direct3D 版本： 1.611.1-81528511
DXCore 版本： 10.0.26100.1-240331-1435.ge-release
Windows 版本： 10.0.22621.4602

C:\Users\Administrator>wsl --status
默认版本: 2

wsl安装任务进行
Get-Service LxssManager
Start-Service LxssManager

查看发行版
wsl --list --online


导入发行版本
wsl --import CentOS7 "F:\Linux\Centos7" "F:\centos.tar"

卸载
wsl --unregister  Ubuntu-24.04 


他就是一直这个状态，下载中，但是没日志没报错


查看wsl是否已启动
wsl --list --verbose

查看可安装版本
wsl --list --all


网络安装 走int 默认安装走mic
wsl --install  --web-download Ubuntu-24.04 

查看所以版本号
wsl -l -v --all
```



开启代理

```
vim C:\Users\Administrator\.wslconfig

[experimental]
autoMemoryReclaim=gradual  # 选择 gradual、dropcache 或 disabled
networkingMode=mirrored      # 设置为 mirrored 或 isolated
dnsTunneling=true            # 选择 true 或 false
firewall=true                # 选择 true 或 false
autoProxy=true               # 选择 true 或 false
sparseVhd=true               # 选择 true 或 false


然后重启
wsl --shutdown
```

![image-20241230175459852](https://liu-fu-gui.github.io/myimg/halo/202412301754908.png)



如果遇到卡的问题 可参考

https://github.com/microsoft/WSL/issues/6405

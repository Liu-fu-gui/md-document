![image-20241226093902115](https://liu-fu-gui.github.io/myimg/halo/202412260939179.png)



![img](https://liu-fu-gui.github.io/myimg/halo/202412260939259.png)

```
start /w "" "Docker Desktop Installer.exe" install --backend=wsl-2 --installation-dir=E:\docker\docker --wsl-default-data-root=E:\docker\wsl --accept-license

```



```
安装目录：

--installation-dir=E:\docker\docker
Docker Desktop 的安装文件将被存放在 E:\docker\docker。
WSL 数据存储路径：

--wsl-default-data-root=E:\docker\wsl
与 WSL 相关的数据将存储在 E:\docker\wsl
```





```
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.top"
  ],
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false
}

```


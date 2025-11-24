https://github.com/langgenius/dify?tab=readme-ov-file

```
cd dify
cd docker
cp .env.example .env
docker compose up -d
```

![image-20250213131646006](https://liu-fu-gui.github.io/myimg/halo/202502131322533.png)





**Dify 502错误解决方法，以及Dify脱离Nginx容器，Dify遇到502**

参考

https://www.bilibili.com/opus/928662372843782145

docker部署的解决方案是

```
docker inspect --format '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' docker-api-1
docker inspect --format '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' docker-web-1
```


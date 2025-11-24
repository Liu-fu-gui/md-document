

## 1.节点信息

| IP                  |       |
| ------------------- | ----- |
| 10.100.20.206:27017 | mongo |
| 10.100.20.220:9000  | yapi  |

安装的环境 node npm mvn nrm 必须有python2

### docker-compose.yaml

```
version: '3.8'

services:
  mongo:
    image: mongo:4.4.29
    container_name: mongo_yapi
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: yapi
      MONGO_INITDB_ROOT_PASSWORD: yapipass
    ports:
      - "27010:27017"
    volumes:
      - ./mongo_data:/data/db

  yapi:
    image: jayfong/yapi:latest
    container_name: yapi
    depends_on:
      - mongo
    ports:
      - "3001:3000"
    environment:
      YAPI_ADMIN_ACCOUNT: "admin@admin.com"
      YAPI_ADMIN_PASSWORD: "ymfe.org"
      YAPI_CLOSE_REGISTER: "true"
      YAPI_DB_SERVERNAME: "mongo"
      YAPI_DB_PORT: "27017"
      YAPI_DB_DATABASE: "yapi"
      YAPI_DB_USER: "yapi"
      YAPI_DB_PASS: "yapipass"
      YAPI_DB_AUTH_SOURCE: "admin"
    restart: always
```

### 三、验证

浏览器访问URL：http://10.100.20.220:3001
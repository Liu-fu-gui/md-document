
<!-- more -->
## 参考文献
```
https://github.com/acmesh-official/acme.sh/wiki/Run-acme.sh-in-docker
https://letsencrypt.org/zh-cn/getting-started/

不会就去可视化申请，但是有限制
https://letsencrypt.osfipin.com/

## 通知
https://github.com/luolongfei/freenom
![20241212132909](https://liu-fu-gui.github.io/myimg/halo/20241212132909.png)
```
## 部署方式（本地）
```
curl https://get.acme.sh | sh -s email=2467802439@qq.com
```
```
## 首次
Ali_Key="xxxxxxxxxxxxxxx" \
Ali_Secret="xxxxxxxxxxxxxxx" \
/root/.acme.sh/acme.sh --issue -d k8syun.com -d '*.k8syun.com' --dns dns_ali --cert-home /etc/nginx/cert \
systemctl reload nginx
```
## sh脚本
```
#!/bin/bash
  
# 设置 Aliyun API 密钥
export Ali_Key="111"
export Ali_Secret="1111"

# 执行证书强制更新命令
/root/.acme.sh/acme.sh --renew -d k8syun.com -d '*.k8syun.com' --force --cert-home /var/www

# 重载 Nginx 配置
systemctl reload nginx
```


## cat ssl.yaml（容器化） 

```
version: '3.8'  # 可以根据需要选择不同的版本

services:
  acme:
    image: neilpang/acme.sh
    container_name: acme.sh
    volumes:
      - ./out:/acme.sh  
     #network_mode: host  # 使用宿主机网络
    command: daemon
```
## 进入容器

```
docker exec -it acme.sh /bin/sh
```
## 设置 Let's Encrypt 为默认 CA

```
acme.sh --set-default-ca --server letsencrypt
```
## 申请证书： 接下来，再次尝试申请证书，使用 --webroot 参数
这个参数是指定申请下来的的文件夹
```
acme.sh --issue -d www.xiaoliu.xn--6qq986b3xl --webroot /acme.sh
```

# 手动阿里云生成一个dns，手动更新dns
```
acme.sh --issue --dns -d xiaoliu.xn--6qq986b3xl -d *.xiaoliu.xn--6qq986b3xl --keylength 4096 --yes-I-know-dns-manual-mode-enough-go-ahead-please

# 不手动
acme.sh --issue --dns dns_cf -d xiaoliu.xn--6qq986b3xl -d *.xiaoliu.xn--6qq986b3xl --keylength 4096
```

![20241129232054](https://liu-fu-gui.github.io/myimg/halo/20241129232054.png)
## 把这个txt加入到你的阿里云的域名解析dns中
![20241129232101](https://liu-fu-gui.github.io/myimg/halo/20241129232101.png)
## 然后去阿里云检测是否生效
https://boce.aliyun.com/detect/dns?spm=a2c1d.8251892.domain-setting.ddetect.b4cc5b76tLeHI4&target=_acme-challenge.www.xiaoliu.%E6%88%91%E7%88%B1%E4%BD%A0&type=TXT

![20241129232121](https://liu-fu-gui.github.io/myimg/halo/20241129232121.png)
获取命令看记录是否存在

![20241129232130](https://liu-fu-gui.github.io/myimg/halo/20241129232130.png)
## dns记录成功后再次执行
```
acme.sh --renew -d www.xiaoliu.xn--6qq986b3xl --dns --yes-I-know-dns-manual-mode-enough-go-ahead-please
```

![20241129232139](https://liu-fu-gui.github.io/myimg/halo/20241129232139.png)

## 特定文件夹保存

```
acme.sh --renew -d www.xiaoliu.xn--6qq986b3xl --dns --yes-I-know-dns-manual-mode-enough-go-ahead-please --cert-home /path/to/cert
```

## 阿里云api方式dns校验
https://usercenter.console.aliyun.com/#/manage/ak
步骤 1: 获取阿里云 API 凭证
登录到阿里云控制台。
导航到 AccessKey 管理 页面，创建一个新的 AccessKey（Access Key ID 和 Access Key Secret）。
记下这两个凭证。

![20241129232152](https://liu-fu-gui.github.io/myimg/halo/20241129232152.png)

# 首次申请

```
AliDNS_AccessKeyID="你的AccessKeyID" AliDNS_AccessKeySecret="你的AccessKeySecret"    acme.sh --issue -d qinglong.xiaoliu.xn--6qq986b3xl --dns dns_ali --yes-I-know-dns-manual-mode-enough-go-ahead-please

```

## 中文域名申请
### 先拿txt记录

```
acme.sh --issue --dns -d ssl.xiaoliu.xn--6qq986b3xl --yes-I-know-dns-manual-mode-enough-go-ahead-plea
```
### 安装模块

```
pip3 install aliyun-python-sdk-core aliyun-python-sdk-alidns
```
### 

python3 中英转换域名申请.py 
```
import aliyunsdkcore.client
from aliyunsdkalidns.request.v20150109.AddDomainRecordRequest import AddDomainRecordRequest

# 初始化阿里云 API 客户端
client = aliyunsdkcore.client.AcsClient('Your_AccessKey_ID', 'Your_AccessKey_Secret', 'cn-hangzhou')

# 创建添加域名记录的请求
request = AddDomainRecordRequest()
request.set_DomainName("xiaoliu.我爱你")
request.set_RR("_acme-challenge.ssl")
request.set_Type("TXT")
request.set_Value("你从 acme.sh 获取的 TXT 记录值")

# 执行 API 请求
response = client.do_action_with_exception(request)
print(response)
****
```
## 添加完之后执行

```
acme.sh --issue --dns dns_ali -d "ssl.xiaoliu.xn--6qq986b3xl"
```

## 续订

```
AliDNS_AccessKeyID="你的AccessKeyID" AliDNS_AccessKeySecret="你的AccessKeySecret" acme.sh --renew -d www.xiaoliu.xn--6qq986b3xl --dns dns_ali --yes-I-know-dns-manual-mode-enough-go-ahead-please
```

## 查看证书信息

```
openssl s_client -connect www.xiaoliu.xn--6qq986b3xl:443 
```

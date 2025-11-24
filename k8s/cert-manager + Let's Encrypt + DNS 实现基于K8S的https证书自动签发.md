### 原理

1. 创建ClusterIssuer或者Issuer资源用于创建颁发者，决定cert-manager签发证书的方式，然后会在cert-manager上的namespace下生成该颁发者的secret用于证书申请的准备。
2. 通过创建Certificate资源来告知cert-manager ：在哪个namespace生成证书、需要签发的域名的证书名称，域名对应的secret资源，以及的引用ClusterIssuer或者Issuer资源等等信息。
3. cert-manager拿着创建好的Certificate资源与ClusterIssuer或者Issuer资源的secret通过内部或外部的webhook向所支持的域名服务提供商对域名进行解析，这里会发起certificaterequests与challenges动作。
4. 解析通过acme校验后将证书返回至cert-manager，certificatere对应项会变成True验证通过状态，再将证书转换到对应namespace下的secret资源做证书引用准备。
5. 在Ingress-controller上生成ingress资源引用该证书secret即可实现https可信访问。

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/iaeFALvWl9EkyJWUl08zcIPs4xQrRmGA5O1hhuZAVgonZW4SOwuGm7y480n5ibFjO96g93tB1IkqQDN1gNXgoJpw/640?wx_fmt=png&from=appmsg)

### 部署cert-manager

```
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.3/cert-manager.yaml
```

### 导入阿里云的AKSK(管理DNS的权限)

```
apiVersion: v1
kind: Secret
metadata:
  name: alidns-secret
  namespace: cert-manager
stringData:
  access-key-id: "Your Access Key Id"
  access-key-secret: "Your Access Key Secret"
```

### 部署alidns-webhook

> K8S v1.30.4 + cert-manager v1.15.3 测试证书签发成功

```
# alidns-webhook 仓库
# https://github.com/starsliao/alidns-webhook

helm upgrade --install alidns-webhook alidns-webhook \
    --repo https://wjiec.github.io/alidns-webhook \
    --namespace cert-manager --create-namespace \
    --set groupName=acme.yourcompany.com
# groupName=设置成你的名称(可以不是申请证书的域名)

# 部署后更换国内镜像
# registry.cn-shenzhen.aliyuncs.com/starsl/alidns-webhook:v1.0.0
```

### 创建CA证书颁发者

```
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: example-acme
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your@aaabbbccc.com # 填写邮箱名称
    privateKeySecretRef:
      name: example-acme # 用于存储 ACME 帐户私钥的密钥名称(可自定义名称)
    solvers:
      - dns01:
          webhook:
            groupName: acme.yourcompany.com # 要和安装时的groupName一致
            solverName: alidns
            config:
              region: "cn-hangzhou" # 不用修改
              accessKeyIdRef:
                name: alidns-secret
                key: access-key-id
              accessKeySecretRef:
                name: alidns-secret
                key: access-key-secret
```

### 创建证书

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: star-example-com
  namespace: cert-manager
spec:
  secretName: star-example-com-tls #生成证书文件的名称
  dnsNames: # 需要签发证书的域名
  - "example.com" 
  - "*.example.com"
  issuerRef:
    name: example-acme # 上一步生成的ClusterIssuer的名称
    kind: ClusterIssuer
```

创建`Certificate`后，cert-manager与alidns-webhook开始进行证书签发工作:

- alidns-webhook使用AKSK去请求阿里的DNS域名解析接口去进行解析记录操作。
- 检查创建的certificate资源，状态变为`True`表示证书已经签发成功。
- 查看secret资源，生成的`star-example-com-tls`即为证书文件。

### 从ingress自动生成证书

```
kind: Ingress
apiVersion: networking.k8s.io/v1
metadata:
  name: foo-example-com
  annotations:
    cert-manager.io/cluster-issuer: "example-acme" #增加该行即可生成证书
spec:
  tls:
  - hosts:
    - foo.example.com
    secretName: foo-example-com-tls
  rules:
  - host: foo.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: backend-service
            port:
              name: http
```
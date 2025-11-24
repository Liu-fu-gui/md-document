## 基于gitlab+docker实现CI/CD



## 项目简介
利用Docker、Gitlab、Gitlab Runner(docker)、SonarQube、Harbor、Jmeter、Maven、Java技术，搭建一个完整的 CI/CD 管道，实现当开发人员完成代码提交后，开始流水线工作，完成编译打包、单元测试、源码扫描、上传制品、部署服务到Docker容器、自动化测试工作。通过自动化构建、测试、代码质量检查和容器化部署，将开发人员从繁琐的手动操作中解放出来，提高团队的开发效率、软件质量和安全性，实现持续更新迭代和持续部署交付。

## CI/CD流程图
![20241213171809](https://liu-fu-gui.github.io/myimg/halo/20241213171809.png)
## 流程说明
1. 开发人员将代码提交到Gitlab代码仓库时，触发持续构建和持续部署流程。
2. 在build标签的Runner上通过maven镜像实现编译打包、单元测试操作。
3. 在build标签的Runner上通过sonar-scanner镜像请求sonarqube服务，实现源码扫描操作。
4. 在build标签的Runner上通过docker-dind镜像实现项目镜像构建并推送至Harbor镜像仓库。
5. 在deployment标签的Runner上执行shell脚本完成镜像拉取以及启动容器服务操作。
6. 在build标签的Runner上通过jmeter镜像实现自动化测试操作。
7. 流水线执行完成后，将结果邮件通知给开发和运维人员。
8. 用户访问项目服务器。

## 服务器列表

| 服务器名称      | 主机名        | IP            | 部署服务     | Runner标签   |
|-----------------|---------------|---------------|--------------|--------------|
| 代码审查服务器  | sonarqube     | 192.168.10.71 | SonarQube    |              |
| 代码托管服务器  | gitlab        | 192.168.10.72 | Gitlab       |              |
| 打包编译服务器  | build         | 192.168.10.74 | Docker       | build        |
| 服务部署服务器  | springboot    | 192.168.10.75 | Docker       | deployment   |
| 镜像仓库服务器  | harbor        | 192.168.10.10 | Harbor       |              |

## 服务部署
Gitlab：https://www.cuiliangblog.cn/detail/section/126398301
SonarQube：https://www.cuiliangblog.cn/detail/section/131602160
Jmeter：https://www.cuiliangblog.cn/detail/section/173491430
Harbor：https://www.cuiliangblog.cn/detail/section/15189547
## 镜像构建
### Maven镜像
maven镜像只需要修改镜像源为国内地址即可。
```
FROM maven:3.9.3
RUN sed -i -E '159a <mirror>\n<id>TencentMirror</id>\n<name>Tencent Mirror</name>\n<url>https://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>\n<mirrorOf>central</mirrorOf>\n</mirror>' /usr/share/maven/conf/settings.xml
```
### sonar-scanner镜像
仓库地址：https://hub.docker.com/r/sonarsource/sonar-scanner-cli
```
docker pull sonarsource/sonar-scanner-cli:10
```
### docker-dind镜像
仓库地址：https://hub.docker.com/_/docker

```
docker pull docker:dind
```
jmeter镜像
由于官方并未提供jmeter镜像，且第三方镜像版本较老，因此推荐构建自定义镜像完成部署。

```
[root@jmeter ~]# cat Dockerfile
# FROM openjdk:17-jdk-alpine
FROM harbor.local.com/library/openjdk:17-jdk-alpine
ENV JMETER_HOME /opt/jmeter
ENV PATH $JMETER_HOME/bin:$PATH
ENV CLASSPATH $JMETER_HOME/lib/ext/ApacheJMeter_core.jar:$JMETER_HOME/lib/jorphan.jar:$CLASSPATH
COPY apache-jmeter-5.6.3.tgz /tmp/
RUN tar -zxf /tmp/apache-jmeter-5.6.3.tgz -C /tmp \
  && mv /tmp/apache-jmeter-5.6.3 /opt/jmeter \
  && rm -rf /tmp/apache-jmeter-5.6.3.tgz
CMD ["jmeter","-v"]
[root@jmeter ~]# docker build -t harbor.local.com/cicd/jmeter:5.6.3 .
```
## 模板库资源更新
模板库具体介绍可参考文档：https://www.cuiliangblog.cn/detail/section/173479217，本文是在gitlab+Linux项目基础上补充模板库内容。

deploy.yml

```
# 服务部署
.deploy-linux: # 部署到linux系统
  stage: deploy
  tags:
    - deploy
  script:
    - sh -x $DEPLOY_PATH $ARTIFACT_USER $ARTIFACTORY_KEY /opt/$ARTIFACT_URL_PATH $ARTIFACTORY_PUBLIC_URL/$ARTIFACT_REPO/$ARTIFACT_URL_PATH

.deploy-docker: # 部署到docker环境
  stage: deploy
  image: harbor.local.com/cicd/docker:dind # 在部署阶段使用docker:dind镜像操作
  tags:
    - docker
  script:
    - sh -x $DEPLOY_PATH $HARBOR_USER $HARBOR_PASSWORD $IMAGE_FULL_NAME $CI_PROJECT_NAME
```
harbor.yml

```
# 镜像上传与下载
variables: # 全局变量
  HARBOR_URL: harbor.local.com # harbor仓库地址
  IMAGE_FULL_NAME: "$HARBOR_URL/$HARBOR_REPO/$IMAGE_NAME"

.docker-upload-harbor:
  stage: upload-harbor
  image: harbor.local.com/cicd/docker:dind # 在构建镜像阶段使用docker:dind镜像操作
  tags: # 在docker机器构建镜像
    - docker
  before_script:
    - cat $DOCKERFILE_PATH 
  script:
    - docker build -f $DOCKERFILE_PATH -t $IMAGE_FULL_NAME .
    - docker login $HARBOR_URL -u $HARBOR_USER -p $HARBOR_PASSWORD # 登录harbor
    - docker push $IMAGE_FULL_NAME # 上传镜像
    - docker rmi -f $IMAGE_FULL_NAME # 删除镜像

.docker-download-harbor:
  stage: download-harbor
  image: harbor.local.com/cicd/docker:dind # 在构建镜像阶段使用docker:dind镜像操作
  tags:
    - docker
  script:
    - docker login $HARBOR_URL -u $HARBOR_USER -p $HARBOR_PASSWORD # 登录harbor
    - docker pull $IMAGE_FULL_NAME # 下载镜像
  after_script:
    - docker images
```
## 流水线项目创建
### 项目代码仓库地址
gitee：https://gitee.com/cuiliang0302/spring_boot_demo
github：https://github.com/cuiliang0302/spring-boot-demo

### gitlab项目权限配置
具体参考文档：https://www.cuiliangblog.cn/detail/section/169621642

### Runner部署配置
Runner安装：https://www.cuiliangblog.cn/detail/section/123128550
Runner注册：https://www.cuiliangblog.cn/detail/section/123863613
注册的Runner执行器类型为Docker，作用范围为shared类型，注册后效果如下：
![20241213172515](https://liu-fu-gui.github.io/myimg/halo/20241213172515.png)
## 配置密钥变量
进入项目——>设置——>CI/CD——>变量
新建SONAR_QUBE_TOEKN、HARBOR_PASSWORD两个变量，取消保护变量，并勾选隐藏变量。
变量配置信息内容如下：
![20241213172543](https://liu-fu-gui.github.io/myimg/halo/20241213172543.png)

## 配置邮件发送
具体可参考文档：https://www.cuiliangblog.cn/detail/section/173068275
## 流水线配置
在项目根目录下创建.gitlab-ci.yml文件
![20241213172615](https://liu-fu-gui.github.io/myimg/halo/20241213172615.png)
流水线内容如下

```
include: # 引入模板库公共文件
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/build.yml'
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/test.yml'
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/sonarqube.yml'
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/harbor.yml'
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/deploy.yml'
  - project: 'devops/gitlabci-template'
    ref: master
    file: 'jobs/jmeter.yml'

variables: # 全局变量
  SONAR_QUBE_PATH: "$CI_PROJECT_DIR/cicd/sonar-project.properties" # sonarqube配置文件地址
  # 镜像上传
  HARBOR_REPO: devops # harbor仓库名
  HARBOR_USER: admin # harbor用户名
  DOCKERFILE_PATH: cicd/Dockerfile # Dockerfile文件路径
  IMAGE_NAME: "$CI_PROJECT_NAME:$CI_COMMIT_BRANCH-$CI_COMMIT_SHORT_SHA" # 镜像名称
  # 服务部署
  DEPLOY_PATH: "$CI_PROJECT_DIR/cicd/deployment-docker.sh" # 服务部署脚本路径
  # 自动化测试
  JMETER_PATH: "$CI_PROJECT_DIR/cicd/jmeter/demo.jmx" # 自动化测试脚本路径

default:
  cache: # 全局缓存配置
    paths:
      - target/

stages:
  - build
  - code_scan
  - product
  - deploy
  - test

mvn: # 编译打包
  stage: build
  extends: .mvn-build
  image: harbor.local.com/cicd/maven:v3.9.3 # 构建阶段使用指定的maven镜像
  tags:
    - build
  
unit_test: # 单元测试
  stage: build
  extends: .mvn_unit_test
  image: harbor.local.com/cicd/maven:v3.9.3 # 构建阶段使用指定的maven镜像
  tags:
    - build

code_scan: # SonarQube代码扫描
  stage: code_scan
  extends: .sonarqube
  image: harbor.local.com/cicd/sonar-scanner-cli:10 # 代码扫描阶段使用sonar-scanner-cli镜像
  before_script:
    - ls target/
  tags:
    - build
  
product: # 上传到harbor仓库
  stage: product
  extends: .docker-upload-harbor
  tags:
    - build

deploy_to_prod: # 部署到生产环境
  stage: deploy
  extends: .deploy-docker
  rules:
    - if: '$CI_COMMIT_BRANCH == "master"'
  tags: 
    - deployment
  after_script:
    - sleep 10
    - docker ps | grep 8888
  environment: # 生产环境
    name: production
    url: http://prod.demo.com:8888

deploy_to_test: # 部署到测试环境
  stage: deploy
  extends: .deploy-docker
  rules:
    - if: '$CI_COMMIT_BRANCH == "test"'
  tags: 
    - build
  after_script:
    - sleep 10
    - docker ps | grep 8888
  environment: # 测试环境
    name: test
    url: http://test.demo.com:8888

pages: # 自动化测试并收集测试报告
  stage: test
  extends: .jmeter
  image: harbor.local.com/cicd/jmeter:5.6.3 # 自动化阶段使用jmeter镜像
  tags: 
    - build
```
## 结果验证
### 镜像查看
![20241213172719](https://liu-fu-gui.github.io/myimg/halo/20241213172719.png)
## 容器信息查看
登录springboot服务器查看容器信息

```
[root@springboot ~]# docker ps
CONTAINER ID   IMAGE                                                      COMMAND                   CREATED          STATUS                    PORTS                                       NAMES
b5bbf8ecd87d   harbor.local.com/devops/spring_boot_demo:master-12895965   "java -jar /app.jar"      51 seconds ago   Up 50 seconds (healthy)   0.0.0.0:8888->8888/tcp, :::8888->8888/tcp   spring_boot_demo
9c3bd4d81c61   harbor.local.com/cicd/gitlab-runner:v16.10.0               "/usr/bin/dumb-init …"   17 minutes ago   Up 12 minutes                                                         gitlab-runner
```
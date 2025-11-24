### 1.DockerFile是什么

### 2.DockerFile构建过程解析

### 3.DockerFile常用保留字指令

### 4.DockerFile自定义CentOS镜像

### 5.DockerFile自定义微服务镜像

**1.DockerFile是什么**

假设我们想**自定义**一个Docker镜像，基于Centos7镜像具备**vim+ifconfig+jdk8**，按照我们以前的学习的步骤，我们需要**先下载镜像**，然后**运行容器**，再以**进入容器进行vim+ifconfig+jdk8的安装**，再把这个容器打包成新的镜像，这样操作非常繁琐，我们有没有更简单的方式来操作这一系列的步骤呢？此时就引出了我们的**DockerFile**。

**DockerFile**是用来**构建Docker镜像的文本文件**，是**由一条条构建镜像所需的指令和参数构成的脚本。**

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06Gg0WVrsXLN9IKAd9TIyvsSBmh5gHztSQkib6jibj2B0JmwgNXBvGkEtibKA/640?wx_fmt=png&from=appmsg)

使用**DockerFile的3步骤：**
**1)编写DockerFile文件。**
**2)docker build命令构成镜像。**
**3)docker run依镜像运行容器实例**

**2.DockerFile构建过程解析**
我们先来看看一个简单的DockerFile文件：

```
#基于centos
FROM centos
#镜像维护者的姓名和邮箱地址
MAINTAINER slf<xxx@qq.com>
#设置名为MYPATH的变量
ENV MYPATH /usr/local
#指定在创建容器后，终端默认登陆的进来工作目录
WORKDIR $MYPATH
```

以上案例中，基于centos镜像，对镜像进行了一系列操作，我们可以得出Dockerfile内容基础知识为：
**1)每条保留指令必须都为大写字母**且后面必须要跟至少一个参数
**2)指令从上到下，顺序执行**
**3)#表示注释**
**4)每条指令都会创建一个新的镜像并对容器进行提交**

由此可以得出，Docker执行DockerFile的大致**流程为：**
**1)docker从基础镜像中选择一个当做容器**
**2)执行一条条指令对容器进行修改**
**3)执行类似docker commit的操作提交一个新的镜像层**
**4)docker再基于刚提交的镜像运行一个新容器**
**5)执行dockerfile中的下一条指令直到所有指令都执行完成**

最后我们得出如下**结论**：

我们从软件的角度来看，**Dockerfile**、**Docker镜像**和**Docker容器**分别代表一个软件的**三个不同的阶段**：

- Dockerfile是软件的一系列**原材料**
- Docker镜像是软件的**交付品**
- Docker容器则是软件镜像的**运行态**。

Dockerfile面向开发，Docker镜像成为交付标准，Docker容器则涉及部署与运维，三者缺一不可，合力充当Docker体系的基石。

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgZjial583tCwX4hR2GfNm5jVOtqelib5FNicS4IUPFFaDjPfMTQO3HCrMQ/640?wx_fmt=png&from=appmsg)

最后再总结一下这三个概念：

**1)Dockerfile**，Dockerfile定义了应用程序所需要的一切东西，包括执行的代码，文件，环境，依赖，操作系统版本等等。

**2)Docker镜像**，在用Dockerfile定义一个文件之后，docker build时会产生一个Docker镜像，当运行 Docker镜像时会真正开始提供服务;

**3)Docker容器**，直接提供服务

**3.DockerFile常用保留字指令**

**FROM** ：**基础镜像**，当前新镜像是基于哪个镜像，指定那个镜像作为模板，第一条必须是FROM

**MAINTAINER**：**镜像维护的作者**

**RUN**：**容器构建时要运行的命令** （在 docker build时运行）

它有**两种格式**：

**shell格式**：

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgcS55ibvrwFUicguOlibaqzsddD8gmqUqb0hbu7EibicibthuAiamIavicjVdbw/640?wx_fmt=png&from=appmsg)
RUN yum -y install vim

**exec格式**：
![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgXj9ELRV7Kml4EnrNgCMaw2JZTBpxFsGiaNudpCDRXrFCROVwyicZtMvg/640?wx_fmt=png&from=appmsg)

**EXPOSE**：容器往外暴露的接口

**WORKDIR**：用来指定创建容器后，终端默认登录进来的工作目录。

**USER**：指定该镜像使用什么样的用户执行，默认root

**ENV**：设置变量，如：

**ENV MY_PATH /usr/mytest**
这个**环境变量可以在后续的任何RUN指令中使用**，这就如同在命令前面指定了环境变量前缀一样，也可以在其它指令中直接使用这些环境变量。

比如：**WORKDIR $MY_PATH**

**ADD**：**将宿主机目录下的文件拷贝到镜像里面**，且会自动处理URL和解压tar压缩包。

**COPY**：**类似ADD**，拷贝文件和目录到镜像中。
将从构建上下文目录中 <源路径> 的文件/目录复制到新的一层的镜像内的 <目标路径> 位置。

COPY src dest

**VOLUME** ：容器数据卷，用于数据保存和持久化工作。

VOLUME指令只是起到了声明了容器中的目录作为匿名卷，但是并没有将匿名卷绑定到宿主机指定目录的功能。

**CMD**：指容器启动后干的事情。

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgTibJ9OdU9Kcfiaq4vUaAdq0DbZ7ZITgw36NXdekW1ASrjHHibL21HibDGA/640?wx_fmt=png&from=appmsg)

Dockerfile 中可以有多个 CMD 指令，但只有最后一个生效，CMD 会被 docker run 之后的参数替换。

CMD和run的区别：

**CMD是在docker run 时运行。**

**RUN是在 docker build时运行。**

**ENTRYPOINT**：也是用来**指定一个容器启动时要运行的命令**，但有几点要注意：

这条明令**类似于CMD命令**，但是ENTRYPOINT**不会被docker run后面的命令覆盖**，而且这些命令行参数会被当作参数送给 ENTRYPOINT 指令指定的程序。

举例：
ENTRYPOINT ["java","-jar","DockerDemo.jar"] 运行jar包。

**优点**：在执行docker run的时候可以指定ENTRYPOINT运行所需参数。

**注意**：如果Dockerfile中存在多个ENTRYPOINT指令，仅最后一个生效。

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgcR8JvlhWRF6EBbxzDicNNJVJlJZBich1T2vce0YkBfPfbJUjpyvUtcwg/640?wx_fmt=png&from=appmsg)

**4.DockerFile自定义CentOS镜像**

我们搭建一个Centos7镜像，具备**vim+ifconfig+jdk8**。

我们先去下载一个java8的Linux安装包：

https://mirrors.yangxingzhen....

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgNaZgwBiacjcZ2Ya3KeUdOIapCiaibxOyK6ibt4B2ticSf9DLXSOEsHS0wTQ/640?wx_fmt=png&from=appmsg)

复制到和Dockerfile一个目录下面：

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgskH14w3AGThFFvS3QFmVIOVrGErJswCTblq0Fvh76K9ibDoIZlxZNww/640?wx_fmt=png&from=appmsg)

准备编写Dockerfile文件：

```
FROM centos:7
MAINTAINER slf
 
ENV MYPATH /usr/local
WORKDIR $MYPATH
 
#安装vim编辑器
RUN yum -y install vim
#安装ifconfig命令查看网络IP
RUN yum -y install net-tools
#安装java8及lib库
RUN yum -y install glibc.i686
RUN mkdir /usr/local/java
#ADD 是相对路径jar,把jdk-8u171-linux-x64.tar.gz添加到容器中,安装包必须要和Dockerfile文件在同一位置
ADD jdk-8u171-linux-x64.tar.gz /usr/local/java/
#配置java环境变量
ENV JAVA_HOME /usr/local/java/jdk1.8.0_171
ENV JRE_HOME $JAVA_HOME/jre
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$PATH
 
EXPOSE 80
 
CMD echo $MYPATH
CMD echo "success--------------ok"
CMD /bin/bash
```

**执行命令：**

```
docker build -t centosjava8:1.5 .

注意 :上面TAG后面有个空格，有个点
```

**执行命令：**

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgUdhicCRLKToLF9fOe5WYlg9SPdJuNJU8OsWibwUrbI0JXbFN5wUgibAmQ/640?wx_fmt=png&from=appmsg)

**进入这个容器，查看是否安装成功：**

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgxJWjYshJHzseRiaqPXG17ZSmCnaQYHWyVoPS1JhY55b6XmXhicEZnREQ/640?wx_fmt=png&from=appmsg)

**5.DockerFile自定义微服务镜像**

我们使用idea打包一个springBoot项目出来，然后把这个微服务丢到Docker里面运行：

yml:

```
server:
  port: 6001
```

controller:

```
@RestController
public class TestController {

    @Value("${server.port}")
    private String port;

    @RequestMapping("/docker")
    public String helloDocker()
    {
        return "hello docker"+"\t"+port+"\t"+ UUID.randomUUID().toString();
    }

    @RequestMapping(value ="/index",method = RequestMethod.GET)
    public String index()
    {
        return "服务端口号: "+"\t"+port+"\t"+UUID.randomUUID().toString();
    }

}
```

打包：
![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GghnCJ8YGIwrAP9iaItQwUochPF2NT7tF9pVNkyDZYZ6tNR82xNEtb6tg/640?wx_fmt=png&from=appmsg)

拷贝到和DockerFile一个目录下：
![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06Gg5jh8UO1ibV36gjP5hzI1iaZ60p0KFNssZ8c80J58IZKRJl8NMBrFpsTA/640?wx_fmt=png&from=appmsg)

Dockerfile:

```
# 基础镜像使用java
FROM williamyeh/java8
# 作者
MAINTAINER slf
# VOLUME 指定临时文件目录为/tmp，在主机/var/lib/docker目录下创建了一个临时文件并链接到容器的/tmp
VOLUME /tmp
# 将jar包添加到容器中并更名为DockerDemo.jar
ADD DockerDemo-0.0.1-SNAPSHOT.jar DockerDemo.jar
# 运行jar包
RUN bash -c 'touch /DockerDemo.jar'
ENTRYPOINT ["java","-jar","DockerDemo.jar"]
#暴露6001端口作为微服务
EXPOSE 6001
 
```

启动构建命令：

```
docker build -t slf_docker:1.6 .
```

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgkxkJsiahqzia1OfHOX9ibb2uIBmymUHnD0ATLr25tCm1SOKwS4KVAoYuA/640?wx_fmt=png&from=appmsg)

运行镜像：

```
docker run -d -p 6001:6001 slf_docker:1.6
```

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06GgwCSCfwowKFlz1ofj2K2XiaKen0Z49G2Uu1QxrqdfCxeZFv03r970kVg/640?wx_fmt=png&from=appmsg)

访问测试：

![image.png](https://mmbiz.qpic.cn/sz_mmbiz_png/mtd7BmFjfdtrMmZ262Klp1foqiaVB06Gg5Hevw0pwW3ibEbHkNFnbFxFyxTDUV5Hq4O59Z0oPOyaA1A0K2qpPjwA/640?wx_fmt=png&from=appmsg)
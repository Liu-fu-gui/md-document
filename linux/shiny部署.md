

## Shiny-Server

Shiny server 可以在线发布shiny应用程序并让用户可以在线管理自己的shiny应用程序。
Shiny Server运行各种不同的shiny程序

https://posit.co/download/shiny-server/

###  ubuntu

```
# install R
sudo apt-get install r-base

# install shiny
sudo su - \
-c "R -e \"install.packages('shiny', repos='https://cran.rstudio.com/')\""

# download and install
sudo apt-get install gdebi-core
wget https://download3.rstudio.org/ubuntu-14.04/x86_64/shiny-server-1.5.17.973-amd64.deb
sudo gdebi shiny-server-1.5.17.973-amd64.deb

```

### centos

```

# install R
sudo yum install R

# install shiny
sudo su - \
-c "R -e \"install.packages('shiny', repos='https://cran.rstudio.com/')\""

# download
wget https://download3.rstudio.org/centos7/x86_64/shiny-server-1.5.17.973-x86_64.rpm

# install 
sudo yum install --nogpgcheck shiny-server-1.5.17.973-x86_64.rpm

```



### 引用文件

```
/srv/shiny-server/
├── index.html -> /opt/shiny-server/samples/welcome.html
├── sample-apps -> /opt/shiny-server/samples/sample-apps
└── myapp/
    ├── server.R
    └── ui.R
```



#### 升级gcc

参考

https://blog.csdn.net/m0_74031424/article/details/144206926

```
#更换阿里云YUM源
cd /etc/yum.repos.d/
mkdir bak
mv *.repo bak/
​
#下载阿里云yum仓库
curl -o /etc/yum.repos.d/Aliyun.repo http://mirrors.aliyun.com/repo/Centos-7.repo
​
yum makecache
​
#下载SCL软件集合库
yum install -y centos-release-scl centos-release-scl-rh
​
#注释旧的源
sed 's,^,#,' /etc/yum.repos.d/CentOS-SCLo-scl.repo -i
​
#更换SCL仓库源
cat >> /etc/yum.repos.d/CentOS-SCLo-scl.repo <<EOF
[centos-sclo-sclo]
name=CentOS-7 - SCLo sclo
baseurl=https://mirrors.aliyun.com/centos/7/sclo/x86_64/sclo/
# mirrorlist=http://mirrorlist.centos.org?arch=$basearch&release=7&repo=sclo-sclo
gpgcheck=0
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-SIG-SCLo
EOF
​
#注释旧的源
sed 's,^,#,' /etc/yum.repos.d/CentOS-SCLo-scl-rh.repo -i
​
#更换SCL仓库源
cat >> /etc/yum.repos.d/CentOS-SCLo-scl-rh.repo <<EOF
[centos-sclo-rh]
name=CentOS-7 - SCLo rh
baseurl=https://mirrors.aliyun.com/centos/7/sclo/x86_64/rh/
# mirrorlist=http://mirrorlist.centos.org?arch=$basearch&release=7&repo=sclo-rh
gpgcheck=0
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-SIG-SCLo
EOF
​
#安装高版本GCC,不同版本对应修改数字就好
yum -y install devtoolset-10-gcc devtoolset-10-gcc-c++ devtoolset-10-binutils
​
#临时应用高版本GCC
scl enable devtoolset-10 bash
​
#永久应用
echo "source /opt/rh/devtoolset-10/enable" >> ~/.bashrc
```



r安装

```
sudo yum install -y gcc gcc-c++ libcurl-devel libxml2-devel openssl-devel
wget https://cran.r-project.org/src/base-prerelease/R-devel.tar.gz
tar -xvzf R-devel.tar.gz
cd R-devel
./configure --prefix=/usr/local --enable-R-shlib
make && make install
R --version
```



## 启动r文件测试

```
# 终端输入R 进入控制台
R
# 在r中运行程序
shiny::runApp("/srv/shiny-server/myapp")
source("/srv/shiny-server/myapp/server.R")
source("/srv/shiny-server/myapp/ui.R")
### 没有问题会提示
### Listening on http://127.0.0.1:XXXX



# 退出r
q()
# 安装缺失包（中国15 16 美国 64 65）
install.packages("shiny")
install.packages("Rcpp")

# 验证包是否安装
library(flextable)
```

#### 常用插件

```
## 使用 lintr 包进行静态代码分析
install.packages("tictoc")


tictoc
## 使用方法
library(lintr)
lint("/srv/shiny-server/myapp/server.R")
lint("/srv/shiny-server/myapp/ui.R")
```


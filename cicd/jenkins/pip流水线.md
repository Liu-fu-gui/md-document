
# 需求是：实现跨平台批量打包发布
<!-- more -->
jenkins pip流水线+xshell

## jenkins pip

```
// 定义一个函数来生成版本号
def createVersion() {
    // 返回格式为 yyyyMMdd_BuildID 的版本号
    return new Date().format('yyyyMMdd') + "_${env.BUILD_ID}"
}

pipeline {
    agent any
    
    // 在 environment 中定义全局变量
    environment {
        VERSION = createVersion()  // 调用 createVersion 函数生成版本号
        PROJECT = "s-statistics-r"  // 项目名称
        sup = "r-" + "${PROJECT}"  // 组合字符串
        ENV = "r-api-prod"  // 环境名称
        JAR_PATH = "${PROJECT}" + "/target"  // JAR 文件路径
    }

    stages {
        // 拉取代码阶段
        stage('拉取代码') {
            steps {
                git branch: '${branch}',  // 指定分支
                credentialsId: 'c1f395ca-d3db-45de-a345-b1613edbffda',  // 认证信息
                url: 'http://101.69.246.86:18099/statistics/statistics-api.git'  // 代码仓库地址
            }
        }

        // 编译阶段
        stage('编译') {
            steps {
                sh 'mvn install'  // 使用 Maven 进行编译
            }
        }
        
        // 发布阶段
        stage('发布') {
            steps {
                sh '/deploy/app/bin/build-api.sh ${SERVER_LIST} ${VERSION} ${PROJECT} ${WORKSPACE}/${JAR_PATH} ${ENV} ${sup}'  // 执行发布脚本
                deleteDir()  // 清理工作目录
            }
        }  

        // 版本信息阶段
        stage('版本信息') {
            steps {
                sh 'echo 版本信息 ========== ${VERSION}'  // 输出版本信息
            }
        }
    }    
}
```
## shell 
jdk_1.8scp.sh
```
#!/bin/bash

PROJECT=$1
VERSION=$2
SERVER_LIST=$3
ENV=$4

echo "开始打包 ${PROJECT} ${VERSION}"

# 打包项目
tar -czf ${VERSION}${PROJECT}.tar.gz .

# 传输到服务器列表中的每个服务器
for server in $(echo ${SERVER_LIST} | tr ',' ' '); do
    echo "传输到 ${server}..."

    # 使用 ssh 命令检查目标目录是否存在，不存在则创建目录
    ssh root@${server} << EOF
        if [ ! -d "/deploy/app/${ENV}/${PROJECT}/bak" ]; then
            mkdir -p /deploy/app/${ENV}/${PROJECT}/bak
        fi
        if [ ! -d "/deploy/app/${ENV}/${PROJECT}/build" ]; then
            mkdir -p /deploy/app/${ENV}/${PROJECT}/build
        fi
        if [ ! -d "/deploy/app/${ENV}/${PROJECT}/run" ]; then
            mkdir -p /deploy/app/${ENV}/${PROJECT}/run
        fi
EOF

    # 检查目录是否成功创建，如果创建失败则报错并退出
    if [ $? -ne 0 ]; then
        echo "无法在 ${server} 上创建目录 /deploy/app/${ENV}/${PROJECT}/bak 或 /deploy/app/${ENV}/${PROJECT}/build 或 /deploy/app/${ENV}/${PROJECT}/run"
        exit 1
    fi

    # 传输文件到目标服务器的目录
    scp ${VERSION}${PROJECT}.tar.gz root@${server}:/deploy/app/${ENV}/${PROJECT}/bak/

    # 在目标服务器上解压文件并执行 Maven 构建
    ssh root@${server} << EOF
        tar -xzf /deploy/app/${ENV}/${PROJECT}/bak/${VERSION}${PROJECT}.tar.gz -C /deploy/app/${ENV}/${PROJECT}/build
        cd /deploy/app/${ENV}/${PROJECT}/build && mvn install
EOF

    # 检查 Maven 构建结果并记录日志
    if [ $? -eq 0 ]; then
        echo "在 ${server} 上执行 Maven 构建成功."
    else
        echo "在 ${server} 上执行 Maven 构建失败."
        # 可添加额外的处理逻辑，如退出脚本或记录错误
    fi

    # 备份最近10份，超过就删除掉
    ssh root@${server} << EOF
        cd /deploy/app/${ENV}/${PROJECT}/bak
        files=(\$(ls -t))
        file_count=\${#files[@]}
        if [ \$file_count -gt 10 ]; then
            files_to_delete=\${files[@]:10}
            for file in \${files_to_delete}; do
                rm -f \$file
            done
        fi
EOF

    # 清空并移动 target 发布文件
    ssh root@${server} << EOF
        rm -rf /deploy/app/${ENV}/${PROJECT}/run/*.jar
        mv /deploy/app/${ENV}/${PROJECT}/build/target/${PROJECT}.jar /deploy/app/${ENV}/${PROJECT}/run/
        rm -rf /deploy/app/${ENV}/${PROJECT}/build/*
EOF

    # 使用 supervisor 启动应用程序
    ssh root@${server} << EOF
        supervisorctl status
        supervisorctl restart single-enroll
        sleep 10
        supervisorctl status
EOF
done

# 删除本服务器目录下文件（请谨慎执行）
rm -rf /var/lib/docker/jenkins2/workspace/zou-edu-api-prod/single-enroll/*

echo "打包和传输完成"
```


## jenkins密钥反编译
密钥为

```
{AQAAABAAAAAgXontB+gAfa94Z2bn/64aYbsS9MxCEIYZq+h1DQcmTugt45nvAOq5l1TigGA}
```
去jenkins---系统管理-终端-执行代码----反编译代码为
```
import hudson.util.Secret

// 替换为从 hudson.util.Secret 文件中提取的加密字符串
def encryptedString = '{AQAAABAAAAAgXontB+gAfa94Z2bn/64aYbsS9MxCEIYZq+h1DQcmTugt45nvAOq5l1TigGA}'

// 解密密钥字符串
def decryptedString = Secret.fromString(encryptedString).getPlainText()

// 打印解密后的字符串
println("Decrypted String: " + decryptedString)
```



## build-api.sh 

```
#!/bin/bash

SERVER_LIST=$1
VERSION=$2
PROJECT=$3
JAR_PATH=$4
ENV=$5
BAK_DIR="/deploy/app/${ENV}/bak/${PROJECT}/"


publish(){
 
         if [ -f "${JAR_PATH}/${PROJECT}.jar" ]; then
               echo 'jar包文件存在,执行发布流程'

 	       #发送jar包到节点服务器
	       scp ${JAR_PATH}/${PROJECT}.jar root@$1:/deploy/app/${ENV}/jar
               scp ${JAR_PATH}/${PROJECT}.jar root@$1:/deploy/app/${ENV}/bak/${PROJECT}/${PROJECT}-${VERSION}.jar              
ssh -T $1 << EOF

             echo '=====在【'$1'】节点执行更新操作======'
             supervisorctl restart ${PROJECT}
 
	     if [ ! -d  "${BAK_DIR}"  ]; then
        	 echo '无对应备份目录...执行创建操作,发布失败'
                 mkdir -p ${BAK_DIR}
                 exit 1 
	     else
                   cd ${BAK_DIR}
        	   echo '保留10个最新的备份'
	           find ${BAK_DIR} -type f -printf '%T@ %p\n' | sort -nr | tail -n +11 | cut -d' ' -f2- | xargs rm -rf
   	  fi
		exit
EOF
            else
               echo 'jar包文件不存在'
         fi
}
IFS=","
arr=($SERVER_LIST)
for server_ip in ${SERVER_LIST[@]}
do
  publish $server_ip
done
```

## build-web.sh 

```
#!/bin/bash

SERVER_LIST=$1
VERSION=$2
PROJECT=$3
STATIC_PATH=$4
ENV=$5
BAK_DIR="/deploy/app/${PROJECT}/bak/"
publish(){
 
         if [ -f "${STATIC_PATH}/${PROJECT}.tar.gz" ]; then
               echo '静态资源包文件存在,执行发布流程'

 	       #发送包到节点服务器
	       scp ${STATIC_PATH}/${PROJECT}.tar.gz root@$1:/deploy/app/${PROJECT}/bak/${PROJECT}-${VERSION}.tar.gz
 
ssh -T $1 << EOF

             echo '=====在【'$1'】节点执行更新操作======'
 
	     if [ ! -d  "${BAK_DIR}"  ]; then
        	 echo '无对应备份目录...执行创建操作,发布未成功'
                 mkdir -p ${BAK_DIR}
                 exit 1
	     else
                   cd ${BAK_DIR}
                   tar -zxf ${PROJECT}-${VERSION}.tar.gz
                   
                   rm -rf ../dist
                   mv ./dist ../  
                 
        	   echo '保留10个最新的备份'
	           find ${BAK_DIR} -type f -printf '%T@ %p\n' | sort -nr | tail -n +11 | cut -d' ' -f2- | xargs rm -rf
   	  fi
		exit
EOF
            else
               echo '静态文件包包文件不存在'
         fi
}
IFS=","
arr=($SERVER_LIST)
for server_ip in ${SERVER_LIST[@]}
do
  publish $server_ip
done
```

## 
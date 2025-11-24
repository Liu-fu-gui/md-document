
## 配置
![20241213170908](https://liu-fu-gui.github.io/myimg/halo/20241213170908.png)

## 流水线
```
def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 年月日_构建ID 
    return new Date().format('yyyyMMdd') +"_${env.BUILD_ID}"
}

pipeline {
    agent any
    
    // 在 environment 引入上边定义的函数，以便于全局调用
    environment {
        VERSION = createVersion()
        PROJECT = "s-admin"
		sup = "kczt-"+"${PROJECT}"
		ENV = "kczt-api-test"
        JAR_PATH="${PROJECT}"+"/${PROJECT}-server"+"/target"
        SERVER_LIST="10.100.20.204"
		
    }
 

    stages {
        stage('拉取代码') {
            steps {
                git branch: '${branch}',
                credentialsId: '5c86e7a5-8db3-4ca9-8398-1322e0e8fae6',
                url: 'http://101.69.246.86:18099/edu-ms/open-course.git'
            }
        }

        
        
        stage('安装依赖') {
            steps {
                script {
                    if (params.NPM_INSTALL) {
                        echo '清理机器内缓存，但这样会导致变慢'
                        sh 'rm -rf ~/.m2/repository/org/open/*'
                    }
                    echo '直接构建'
                    sh 'mvn install -U'
                }
            }
        }
        
        stage('发布') {
            steps {
             sh '/deploy/app/bin/build-api-server.sh ${SERVER_LIST} ${VERSION} ${PROJECT} ${WORKSPACE}/${JAR_PATH} ${ENV} ${sup}'
                deleteDir()
            }
        }  

        stage('版本信息'){
            steps{
             sh  'echo  版本信息 ==========   ${VERSION} '
            }
        }
        
   }    
}
```
## 效果
![20241213171040](https://liu-fu-gui.github.io/myimg/halo/20241213171040.png)
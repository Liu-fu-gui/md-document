# jenkins pip插件使用
<!-- more -->
## 动态分支

1. Git Parameter Plugin：适用于手动选择分支。
2. Multibranch Pipeline Plugin：自动发现和构建分支。
3. Branch API Plugin：提供分支管理的 API。
4. GitHub Branch Source Plugin：针对 GitHub 的分支管理插件。

### 本场景使用的
![20241129214111](https://liu-fu-gui.github.io/myimg/halo/20241129214111.png)

![20241129214119](https://liu-fu-gui.github.io/myimg/halo/20241129214119.png)

```
branch
origin/(.*)
```



```
def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 年月日_构建ID 
    return new Date().format('yyyyMMdd') +"_${env.BUILD_ID}"
}

pipeline {
    agent any
    tools {
        jdk 'jdk1.8'  
    }
    // 在 environment 引入上边定义的函数，以便于全局调用
    environment {
        VERSION = createVersion()
        PROJECT = "single-enroll"
		sup = "dkdz-"+"${PROJECT}"+"-pre"
		ENV = "dkdz-api-pre"
        JAR_PATH="${PROJECT}"+"/target"
        SERVER_LIST="机器ip"
		
    }
 

    stages {
        stage('拉取代码') {
            steps {
                git branch: '${branch}',
                credentialsId: 'jenkins拉取gitlab认证',
                url: 'gitlaburl'
            }
        }

        stage('编译') {
            steps {
                sh 'mvn clean install -U'
            }
        }
        
        stage('发布') {
            steps {
                sh '/deploy/app/bin/build-api.sh ${SERVER_LIST} ${VERSION} ${PROJECT} ${WORKSPACE}/target ${ENV} ${sup}'
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
## 钉钉通知
### DingTalk
![20241129214428](https://liu-fu-gui.github.io/myimg/halo/20241129214428.png)
### 路由 ip/manage/dingtalk/
![20241129214435](https://liu-fu-gui.github.io/myimg/halo/20241129214435.png)


![20241129214444](https://liu-fu-gui.github.io/myimg/halo/20241129214444.png)

```
构建 URL：${BUILD_URL}

构建日志：${BUILD_URL}console

测试报告：${BUILD_URL}allure

提交分支：${branch}
```
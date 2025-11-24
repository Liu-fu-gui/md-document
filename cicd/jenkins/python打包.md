需求：在10.100.20.202传输文件到10.100.20.204 然后dockerfile 构建，run运行



Dockerfile

```
[root@java /deploy/app/qmzs-api-test/py/open-psp-ai]$ cat Dockerfile 
FROM python:3.10-slim
WORKDIR /app

COPY . /app
RUN pip install --no-cache-dir uvicorn fastapi openai

ENV PYTHONUNBUFFERED=1
ENV TECENT_API_KEY="sk-pLYhchP9avWgp2YwIIdyX1N49jbxxcvYXJXaIDfBAIpVmYzH"
ENV TECENT_BASE_URL="https://api.lkeap.cloud.tencent.com/v1"
ENV VOLC_API_KEY="d09f7438-1f7b-4fc1-a6b3-7e798f53ba56"
ENV VOLC_BASE_URL="https://ark.cn-beijing.volces.com/api/v3/bots"
ENV VOLC_MODEL_INTERNET="bot-20250220170138-v2zkq"
ENV VOLC_MODEL_PURE="bot-20250220175939-t8zxx"

CMD ["uvicorn", "ai_api:app", "--host", "0.0.0.0", "--port", "8000"]
EXPOSE 8000

```

jenkins pipeline

```
def createVersion() {
    // 定义一个版本号作为当次构建的版本，输出结果 年月日_构建ID
    return new Date().format('yyyyMMdd') + "_${env.BUILD_ID}"
}

pipeline {
    agent any

    // 在 environment 引入上边定义的函数，以便于全局调用
    environment {
        VERSION = createVersion()
        PROJECT = "open-psp-ai"
        sup = "qmzs-" + "${PROJECT}"
        ENV = "qmzs-api-test"
        SERVER_LIST = "10.100.20.204"
        DEPLOY_PATH = "/deploy/app/qmzs-api-test/py/open-psp-ai/"
        SSH_USER = "root"  
    }

    stages {
        stage('拉取代码') {
            steps {
                git branch: '${branch}',
                    credentialsId: '5c86e7a5-8db3-4ca9-8398-1322e0e8fae6',
                    url: 'http://101.69.246.86:18099/edu-ms/open-psp-ai.git'
            }
        }

        stage('传输文件到远程服务器') {
            steps {
                script {
                    // 使用 scp 或 rsync 将构建产物传输到远程服务器
                    sh """
                        scp -r . ${SSH_USER}@${SERVER_LIST}:${DEPLOY_PATH}
                    """
                }
            }
        }

        stage('在远程服务器上停止并移除旧容器') {
            steps {
                script {
                    // 停止并移除之前的容器（如果存在）
                    sh """
                        ssh ${SSH_USER}@${SERVER_LIST} '
                        if [ \$(docker ps -q -f "name=py-dev") ]; then
                            docker stop py-dev
                            docker rm py-dev
                        fi
                        '
                    """
                }
            }
        }

        stage('在远程服务器上构建 Docker 镜像') {
            steps {
                script {
                    // SSH 到远程服务器，构建 Docker 镜像
                    sh """
                        ssh ${SSH_USER}@${SERVER_LIST} 'cd ${DEPLOY_PATH} && docker build -t py-dev:${VERSION} .'
                    """
                }
            }
        }

        stage('在远程服务器上运行 Docker 容器') {
            steps {
                script {
                    // SSH 到远程服务器，运行 Docker 容器
                    sh """
                        ssh ${SSH_USER}@${SERVER_LIST} 'docker run -d -p 8000:8000 --name py-dev-${VERSION} py-dev:${VERSION}'
                    """
                }
            }
        }
    }

    post {
        success {
            echo "部署成功！"
        }
        failure {
            echo "部署失败！"
        }
    }
}
```


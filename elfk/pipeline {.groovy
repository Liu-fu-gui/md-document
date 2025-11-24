pipeline {
  agent {
    docker {
      image 'maven:3.8.6-jdk-8'
      args '-v /var/run/docker.sock:/var/run/docker.sock -v /var/jenkins_home/.m2:/var/jenkins_home/.m2'
    }
  }

  parameters {
    string(name: 'GIT_TAG', defaultValue: 'k8s', description: 'Git 分支或标签')
    string(name: 'GIT_URL', defaultValue: 'http://10.100.20.230/gxpt/courseshareplatform.git', description: 'Git 仓库地址')
    string(name: 'GIT_CREDENTIAL_ID', defaultValue: '422982b4-7ae1-4752-a612-0aa3b9ce0531', description: 'Git 凭据 ID')
    string(name: 'TARGET_MODULE', defaultValue: 'lano-bigdata', description: '构建的子模块名')
    string(name: 'HARBOR_HOST', defaultValue: '10.100.20.222', description: 'Harbor 镜像仓库地址（不要加 https://）')
    string(name: 'DOCKER_IMAGE', defaultValue: 'cloud_test/lano-bigdata30208', description: '镜像名称（含项目名）')
    string(name: 'HARBOR_CREDENTIAL_ID', defaultValue: '94658338-a152-4abc-9676-4ff6b6b8e053', description: 'Harbor 凭据 ID')
    string(name: 'APP_NAME', defaultValue: 'cloud_test/lano-bigdata30208', description: 'K8s 部署名')
    string(name: 'K8S_NAMESPACE', defaultValue: 'default', description: 'K8s 命名空间')
    booleanParam(name: 'SKIP_BUILD', defaultValue: false, description: '跳过 Maven 构建（调试用）')
  }

  environment {
    BUILD_VERSION = "${new Date().format('yyyyMMddHHmmss')}"
    FULL_IMAGE = ''
  }

  stages {
    stage('🧼 清理工作空间') {
      steps {
        cleanWs()
      }
    }

    stage('🧬 拉取代码') {
      steps {
        git branch: "${params.GIT_TAG}", credentialsId: "${params.GIT_CREDENTIAL_ID}", url: "${params.GIT_URL}"
      }
    }

    stage('🛠️ Maven 构建目标模块') {
  when {
    expression { return !params.SKIP_BUILD }
  }
  steps {
    script {
      def module = params.TARGET_MODULE?.trim()
      if (!module) {
        error "❌ 参数 TARGET_MODULE 为空，请传入要构建的子模块名。"
      }

      dir(module) {
        echo "📦 正在构建模块：${module}"
        sh "mvn clean package -DskipTests --settings /var/jenkins_home/.m2/settings.xml"

        def jarPath = sh(
          script: "find target -maxdepth 1 -name '*.jar' ! -name '*original*' | head -n1",
          returnStdout: true
        ).trim()

        if (!jarPath || !fileExists(jarPath)) {
          error "❌ 未找到构建生成的 JAR 文件。"
        }

        echo "✅ 已找到构建产物：${jarPath}"
        def jarFileName = jarPath.substring(jarPath.lastIndexOf('/') + 1)
        env.JAR_FILE_NAME = jarFileName

        // 拷贝到工作区根目录
        sh "cp ${jarPath} ../.."
      }

      // 在主目录进行 stash
      dir("${WORKSPACE}") {
        stash includes: "${env.JAR_FILE_NAME}", name: 'jar-package'
      }
    }
  }
}


    stage('🐳 构建并推送 Docker 镜像') {
      when {
        expression { return !params.SKIP_BUILD }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: "${params.HARBOR_CREDENTIAL_ID}", usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
          script {
            def fullImage = "${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${env.BUILD_VERSION}"
            env.FULL_IMAGE = fullImage

            def dockerDir = sh(script: "find . -name Dockerfile | head -n1 | xargs dirname", returnStdout: true).trim()
            echo "📂 使用 Dockerfile 构建路径：${dockerDir}"

            dir(dockerDir) {
              unstash 'jar-package'

              sh """
                echo "🔐 登录 Harbor..."
                docker login -u $HARBOR_USER -p $HARBOR_PASS https://${params.HARBOR_HOST}

                echo "🧱 构建镜像: ${fullImage}"
                docker build \
                  --build-arg TARGET_MODULE=${params.TARGET_MODULE} \
                  --build-arg JAR_FILE=${env.JAR_FILE_NAME} \
                  -t ${fullImage} .

                echo "📤 推送镜像: ${fullImage}"
                docker push ${fullImage}
                docker rmi ${fullImage}
              """
            }

            echo "🖼️ 构建完成，镜像地址：${fullImage}"
          }
        }
      }
    }

    stage('🚀 部署到 Kubernetes') {
      steps {
        script {
          sh """
            echo "🚀 开始部署 ${params.APP_NAME} 至命名空间 ${params.K8S_NAMESPACE}"
            chmod +x /var/jenkins_home/packup/deploy-k8s.sh
            /var/jenkins_home/packup/deploy-k8s.sh ${params.APP_NAME} ${FULL_IMAGE} ${params.K8S_NAMESPACE}
          """
        }
      }
    }
  }

  post {
    success {
      echo "✅ 发布成功！镜像地址：${FULL_IMAGE}"
    }
    failure {
      echo "❌ 发布失败，请检查构建或部署阶段日志。"
    }
  }
}

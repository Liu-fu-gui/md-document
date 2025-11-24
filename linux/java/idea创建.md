### **步骤 1：打开 IntelliJ IDEA**

1. 启动 IntelliJ IDEA。
2. 在欢迎界面，点击 **“New Project”**（新建项目）。

------

### **步骤 2：选择项目类型**

1. 在左侧菜单中选择 **Spring Initializr**（这是 Spring Boot 项目的初始化工具）。
2. 确保 **Project SDK** 选择了正确的 JDK 版本（例如 JDK 17 或 JDK 20）。
3. 点击 **Next**。

------

### **步骤 3：配置项目基本信息**

1. **Group**：填写组织名称，例如 `com.example`。
2. **Artifact**：填写项目名称，例如 `demo`。
3. **Name**：项目名称，通常和 Artifact 一致。
4. **Package name**：包名，通常和 Group + Artifact 一致，例如 `com.example.demo`。
5. **Packaging**：选择 `Jar`（默认选项，适合大多数 Spring Boot 项目）。
6. **Java Version**：选择你安装的 JDK 版本，例如 `17` 或 `20`。
7. 点击 **Next**。

------

### **步骤 4：选择依赖**

1. 在依赖选择界面，你可以根据需要添加 Spring Boot 的依赖。以下是一些常用的依赖：
   - **Spring Web**：用于开发 Web 应用（RESTful API）。
   - **Spring Data JPA**：用于数据库操作。
   - **Spring Boot DevTools**：开发工具，支持热部署。
   - **Thymeleaf**：用于前端模板渲染。
   - **Lombok**：简化代码，自动生成 Getter/Setter 等方法。
   - **Spring Security**：用于安全认证和授权。
2. 选择你需要的依赖后，点击 **Next**。

------

### **步骤 5：配置项目路径**

1. **Project location**：选择项目保存的路径。
2. 点击 **Finish**，IDEA 会自动创建项目并下载所需的依赖。

------

### **步骤 6：等待项目初始化**

1. IDEA 会自动下载 Maven 依赖并构建项目。
2. 构建完成后，你会看到一个标准的 Spring Boot 项目结构。

------

### **项目结构说明**

创建完成后，项目结构大致如下：

复制

```
demo
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               └── DemoApplication.java  // Spring Boot 启动类
│   │   └── resources
│   │       ├── application.properties  // 配置文件
│   │       └── static  // 静态资源（如 CSS、JS）
│   │       └── templates  // 模板文件（如 HTML）
│   └── test  // 测试代码
├── pom.xml  // Maven 配置文件
```

------

### **步骤 7：运行项目**

1. 打开 `src/main/java/com/example/demo/DemoApplication.java`。
2. 点击左侧的绿色三角形图标（或右键选择 `Run DemoApplication`）。
3. 如果一切正常，你会看到控制台输出 Spring Boot 的启动日志，最后显示 `Started DemoApplication in X seconds`。

------

### **步骤 8：测试项目**

1. 如果你添加了 **Spring Web** 依赖，可以创建一个简单的 REST 控制器来测试。
2. 在 `com.example.demo` 包下创建一个新的类 `HelloController.java`：

java

复制

```
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Spring Boot!";
    }
}
```

1. 重新运行项目，打开浏览器访问 `http://localhost:8080/hello`，你会看到页面显示 `Hello, Spring Boot!`。





### 结构优化

```
src/main/java/com/example/demo
├── controller
│   ├── HelloController.java
│   └── TestController.java
├── service
│   └── UserService.java
├── repository
│   └── UserRepository.java
├── entity
│   └── User.java
└── DemoApplication.java
```


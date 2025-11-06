# ![使用 Kotlin 和 Spring 的 RealWorld 示例应用](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### Spring Boot + MyBatis 代码库包含真实世界示例（CRUD、认证、高级模式等），遵循 [RealWorld](https://github.com/gothinkster/realworld-example-apps) 规范和 API。

此代码库旨在展示使用 Spring Boot + MyBatis 构建的完整全栈应用程序，包括 CRUD 操作、身份认证、路由、分页等功能。

有关此应用如何与其他前端/后端协作的更多信息，请访问 [RealWorld](https://github.com/gothinkster/realworld) 仓库。

# *新功能* GraphQL 支持  

遵循一些 DDD 原则。REST 或 GraphQL 只是一种适配器。领域层将始终保持一致。因此，此仓库同时实现了 GraphQL 和 REST。

GraphQL 架构位于 https://github.com/gothinkster/spring-boot-realworld-example-app/blob/master/src/main/resources/schema/schema.graphqls，可视化效果如下所示。

![](graphql-schema.png)

此实现使用了 [dgs-framework](https://github.com/Netflix/dgs-framework)，这是一个相当新的 Java GraphQL 服务器框架。

# 工作原理

该应用程序使用 Spring Boot（Web、MyBatis）。

* 使用领域驱动设计（Domain Driven Design）的理念来分离业务术语和基础设施术语。
* 使用 MyBatis 实现 [数据映射器（Data Mapper）](https://martinfowler.com/eaaCatalog/dataMapper.html) 模式进行持久化。
* 使用 [CQRS](https://martinfowler.com/bliki/CQRS.html) 模式来分离读模型和写模型。

代码组织如下：

1. `api` 是由 Spring MVC 实现的 Web 层
2. `core` 是业务模型，包括实体和服务
3. `application` 是用于查询数据传输对象的高级服务
4. `infrastructure` 包含所有实现类作为技术细节

# 安全性

与 Spring Security 集成，并添加其他过滤器用于 JWT 令牌处理。

密钥存储在 `application.properties` 中。

# 数据库

它使用 ~~H2 内存数据库~~ SQLite 数据库（便于本地测试，避免每次重启后丢失测试数据），可以在 `application.properties` 中轻松更改为任何其他数据库。

# 开始使用

您需要安装 Java 11。

    ./gradlew bootRun

要测试它是否正常工作，请在浏览器中打开 http://localhost:8080/tags。  
或者，您可以运行

    curl http://localhost:8080/tags

# 使用 [Docker](https://www.docker.com/) 试用

您需要安装 Docker。
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# 使用 RealWorld 前端试用

后端 API 的入口地址是 http://localhost:8080，**不是** http://localhost:8080/api，尽管一些前端文档可能这样建议。

# 运行测试

该仓库包含大量测试用例，涵盖 API 测试和仓库测试。

    ./gradlew test

# 代码格式化

使用 spotless 进行代码格式化。

    ./gradlew spotlessJavaApply

# 帮助

请 fork 并提交 PR 以改进项目。

netty-websocket-spring-boot-starter
===================================

### 简介

本项目帮助你在spring-boot中使用Netty来开发WebSocket服务器，在原有基础上添加对spring mvc支持改造，目前仅支持get方法和post方法(application/json格式)

[中文文档](https://gitlab.lizhi.fm/pongpong/netty-websocket-spring-boot-starter/-/blob/master/README_zh.md) (Chinese Docs)

### 要求

- jdk版本为1.8或1.8+


### 快速开始

### 1.仅需要websocket功能

- 引入依赖，且去除servlet依赖

```xml
<dependency>
    <groupId>com.adealink.weparty</groupId>
    <artifactId>netty-websocket-spring-boot-starter</artifactId>
    <version>0.15.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <artifactId>javax.servlet-api</artifactId>
            <groupId>javax.servlet</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

### 2.同时需要websocket和spring mvc功能

- 引入依赖

```xml
<dependency>
    <groupId>com.adealink.weparty</groupId>
    <artifactId>netty-websocket-spring-boot-starter</artifactId>
    <version>0.15.0-SNAPSHOT</version>
</dependency>
```

- BootStrap类新增@EnableHttpServletServer注解

```java
@SpringBootApplication
@EnableHttpServletServer
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
```# netty-websocket-spring-boot-starter

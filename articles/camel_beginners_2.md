---
title: "Apache Camel 入門 2"
emoji: "👋"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: [camel,java]
published: true
---
本記事は[Apache Camel 入門](https://zenn.dev/masatsugumatsus/articles/c57fbe6a0fc863)の続編です。
Spring Boot連携機能で、簡単な連携機能を作成してみます。

# Spring Boot のCamelサポート
- 2023年1月の段階では、CamelはjakartaEE対応がまだなので、Spring Boot 3.xは未サポート。
- Camel 4.0でSpring Boot 3はサポートされる。[ロードマップ](https://camel.apache.org/blog/2023/01/camel4roadmap/)
- CamelはRedhatが製品に組み込んでいるが、基本は伝統あるOSSなのでSpring サポートも頑張ってくれそう。

# Getting Started
- [Spring Initializer](https://start.spring.io/)で、Spring Bootのバージョンを2.x系、依存関係にApache Camelを選択
- Camelのバージョン管理をするために、build.gradleに、implementation platformを追加し、個別のバージョン番号を削除

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.8'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation platform('org.apache.camel:camel-bom:3.20.1')
    implementation platform( 'org.apache.camel.springboot:camel-spring-boot-bom:3.20.1')
    implementation 'org.apache.camel.springboot:camel-spring-boot-starter'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

# Timerログルートの作成
- 数秒おきにログ出力する簡単なルートを作成する
- [Timerコンポーネント](https://camel.apache.org/components/3.20.x/timer-component.html)を利用するので、下記の依存関係を追加する。どんな依存関係かはドキュメントを参照する
```groovy
    implementation 'org.apache.camel.springboot:camel-timer-starter'
```

### ルート定義
- RoutBuilderを継承して、configureメソッドをオーバーライドする。SpringのBeanとして登録したら自動で起動される
- URIの構造はドキュメントを参照
```java
package com.example.springcamel.timer;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TimerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        //3秒ごとにメッセージ生成,３回で終了
        from("timer:foo?fixedRate=true&period=3000&repeatCount=3")
                .log("hello");
    }
}
```

- 一応自動生成されているSpring Bootのメインクラス
```java
package com.example.springcamel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCamelApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCamelApplication.class, args);
	}

}
```

- 実行
```shell
./gradlew bootRun
```

こんな感じで3回出て止まるはず。(Spring Boot自体は止まってない)
```shell
2023-02-01 00:10:50.829  INFO 87572 --- [1 - timer://foo] route2                                   : hello
2023-02-01 00:10:53.822  INFO 87572 --- [1 - timer://foo] route2                                   : hello
2023-02-01 00:10:56.822  INFO 87572 --- [1 - timer://foo] route2                                   : hello
```

### EndpointDSLを利用する
- uriが間違えやすいので、タイプセーフなエンドポイントDSLを利用する
- 依存関係
```groovy
implementation "org.apache.camel:camel-endpointdsl"
```

- ルート定義の修正。EndpointRouteBuilderを継承する
```java
package com.example.springcamel.timer;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TimerRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        //3秒ごとにメッセージ生成,３回で終了
        from(timer("foo")
                .fixedRate(true)
                .period(3000)
                .repeatCount(3))
                .log("hello");
    }
}

```

# SpringのBeanを利用する
- beanコンポーネントを使ってSpringで管理されているBeanを使う。beanコンポーネントは依存関係追加は不要

## SpringのBean定義
- ルートでメッセージが伝播されるのを確認するため、Beanを２つ定義する
```java
package com.example.springcamel.bean;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloService {
    Logger logger= LoggerFactory.getLogger(HelloService.class);
    public String called(Message message){
        //timerはbodyを設定しないのでnull
        logger.info("body is {}",message.getBody());

        return "hello";
    }
}

```

```java
package com.example.springcamel.bean;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorldService {
    Logger logger= LoggerFactory.getLogger(WorldService.class);
    public String called(Message message){
        logger.info("body is {}",message.getBody());

        //Stringだと信じる
        return String.join(" ",message.getBody(String.class),"World");
    }
}
```

## ルート定義
```java
package com.example.springcamel.bean;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class BeanRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(timer("foo2").repeatCount(3).fixedRate(true).period(3000))
                .bean(HelloService.class) //実行後、HelloServiceのメソッドの戻り値がbodyに設定
                .bean(WorldService.class) //実行後、Hメソッドの戻り値がbodyに設定
                //expression言語を使ってbody部をログ出力
                .log("end body is ${body}");
    }
}

```

## 実行結果
```shell
2023-02-01 00:29:50.253  INFO 87778 --- [ - timer://foo2] c.example.springcamel.bean.HelloService  : body is null
2023-02-01 00:29:50.254  INFO 87778 --- [ - timer://foo2] c.example.springcamel.bean.WorldService  : body is hello
2023-02-01 00:29:50.255  INFO 87778 --- [ - timer://foo2] route1                                   : end body is hello World
```

## Beanの修正
- Beanは引数にMessageだけでなく、Body部ももらえる
```java
package com.example.springcamel.bean;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorldService {
    Logger logger= LoggerFactory.getLogger(WorldService.class);
    public String called(String hello){
        logger.info("body is {}",hello);
        return String.join(" ",hello,"World");
    }
}
```

-　実行結果
```shell
2023-02-01 00:32:42.496  INFO 87800 --- [ - timer://foo2] c.example.springcamel.bean.HelloService  : body is null
2023-02-01 00:32:42.496  INFO 87800 --- [ - timer://foo2] c.example.springcamel.bean.WorldService  : body is hello
2023-02-01 00:32:42.496  INFO 87800 --- [ - timer://foo2] route1                                   : end body is hello World
```


---
title: "Apache Camel 入門 2++"
emoji: "👋"
type: "tech" # tech: 技術記事 / idea: アイデア
topics: [camel,java]
published: true
---
本記事は[Apache Camel 入門](https://zenn.dev/masatsugumatsus/articles/c57fbe6a0fc863)の続編の続編です。
Spring Boot連携機能で、作ったものと同じものをQuarkus使って作成してみます。nativeコンパイルも簡単にできたのでそこまで。

# Quarkus のCamelサポート
- Camel Quarkusとして全面的にサポート
- [公式](https://camel.apache.org/camel-quarkus/2.15.x/index.html)

# Getting Started
- [code Quarkus io](https://start.spring.io/)で、Camel Core、Camel Timer、Camel Beanを選択

```groovy
plugins {
    id 'java'
    id 'io.quarkus'
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}")
    implementation enforcedPlatform("${quarkusPlatformGroupId}:quarkus-camel-bom:${quarkusPlatformVersion}")
    implementation 'org.apache.camel.quarkus:camel-quarkus-core'
    implementation 'org.apache.camel.quarkus:camel-quarkus-timer'
    implementation 'org.apache.camel.quarkus:camel-quarkus-bean'
    implementation 'org.apache.camel:camel-endpointdsl'
    implementation 'io.quarkus:quarkus-arc'
    testImplementation 'io.quarkus:quarkus-junit5'
}

group 'com.example'
version '1.0-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

test {
    systemProperty "java.util.logging.manager", "org.jboss.logmanager.LogManager"
}
compileJava {
    options.encoding = 'UTF-8'
    options.compilerArgs << '-parameters'
}

compileTestJava {
    options.encoding = 'UTF-8'
}

```

# Beanを使ったTimerログルートの作成
- 数秒おきにログ出力する簡単なルートを作成する
- [Quarkus Timerコンポーネント](https://camel.apache.org/camel-quarkus/2.15.x/reference/extensions/timer.html)
- [Quarkus Beanコンポーネント](https://camel.apache.org/camel-quarkus/2.15.x/reference/extensions/bean.html)
### ルート定義
- EndpointBuilderを継承して、configureメソッドをオーバーライドする。QuarkusのCDIに登録する
- URIの構造はドキュメントを参照
```java
package com.example.quarkuscamel;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TimerRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        from(timer("foo")
                .fixedRate(true)
                .period(3000)
                .repeatCount(3))
                .bean(HelloService.class)
                .bean(WorldService.class)
                .log("${body} !!");
    }
}
```

- HelloService
```java
package com.example.quarkuscamel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HelloService {
    public String hello(){
        return "Hello";
    }
}
```

- WorldService
```java
package com.example.quarkuscamel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorldService {

    public String world(String hello){
        return String.join(" ",hello,"World");
    }
}

```


- メインクラスは省略可能


- devモードで実行
```shell
./gradlew quarkusDev
```

結果
```shell
2023-02-02 23:06:31,029 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
2023-02-02 23:06:34,025 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
2023-02-02 23:06:37,026 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
```

### Native ビルド
```shell
./gradlew build -Dquarkus.package.type=native

```
- 実行
```shell
 ./build/quarkus-camel-1.0-SNAPSHOT-runner
```

-　結果
```shell
2023-02-02 23:06:31,029 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
2023-02-02 23:06:34,025 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
2023-02-02 23:06:37,026 INFO  [route1] (Camel (camel-1) thread #1 - timer://foo) Hello World !!
```

簡単にnativeアプリを作れた。素晴らしい。nativeでのコンテナイメージも簡単。k8sとかで動かすならこれで行きたい

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

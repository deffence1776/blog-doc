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

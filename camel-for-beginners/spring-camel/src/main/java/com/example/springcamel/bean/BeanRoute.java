package com.example.springcamel.bean;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class BeanRoute extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(timer("foo2").repeatCount(3).fixedRate(true).period(3000))
                .bean(HelloService.class)
                .bean(WorldService.class)
                .log("end body is ${body}");
    }
}

package com.example.springcamel.firststep;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SampleSimpleRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        //directコンポーネントが受信したメッセージをFileコンポーネントに渡すルートの定義
        from("direct:xxx")
                .to("file:dirName?fileName=xyz");
    }
}

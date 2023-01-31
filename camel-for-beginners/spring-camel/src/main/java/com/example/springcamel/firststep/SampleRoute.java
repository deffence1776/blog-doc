package com.example.springcamel.firststep;

import com.example.springcamel.firststep.input.RouteInput;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class SampleRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:xxx")
        //ボディ部の文字列をjsonとしてパースし、JavaBeanにマッピング
                .unmarshal().json(JsonLibrary.Jackson, RouteInput.class)
                //メッセージヘッダーに値を設定
                .setHeader("inputDirPath", simple("tmpDir"))
                .to("file:xyz");
    }
}

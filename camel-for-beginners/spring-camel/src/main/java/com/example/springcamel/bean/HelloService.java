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

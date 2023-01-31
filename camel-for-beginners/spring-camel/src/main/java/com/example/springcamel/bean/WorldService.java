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

        //Stringだと信じる
        return String.join(" ",hello,"World");
    }
}

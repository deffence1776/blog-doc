package com.example.quarkuscamel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HelloService {
    public String hello(){
        return "Hello";
    }
}

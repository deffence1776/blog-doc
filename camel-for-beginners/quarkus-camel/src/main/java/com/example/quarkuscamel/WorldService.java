package com.example.quarkuscamel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorldService {

    public String world(String hello){
        return String.join(" ",hello,"World");
    }
}

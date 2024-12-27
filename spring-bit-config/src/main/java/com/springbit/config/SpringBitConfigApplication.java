package com.springbit.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class SpringBitConfigApplication {

    public static void main(String[] args) {
        // Debug ssl handshake...
        //System.setProperty("javax.net.debug", "ssl:trustmanager");

        SpringApplication.run(SpringBitConfigApplication.class, args);
    }

}

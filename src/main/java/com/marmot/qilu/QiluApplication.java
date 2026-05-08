package com.marmot.qilu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QiluApplication {

    public static void main(String[] args) {
        SpringApplication.run(QiluApplication.class, args);
    }

}

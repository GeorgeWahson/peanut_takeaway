package com.wahson;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PeanutTakeawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeanutTakeawayApplication.class, args);
        log.info("main方法启动成功...");
    }

}

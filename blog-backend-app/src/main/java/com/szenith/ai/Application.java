package com.szenith.ai;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Configurable
public class Application {

    public static void main(String[] args){
        // 必须透传 args，否则 --server.port / --spring.profiles.active 等命令行配置会被静默丢弃
        SpringApplication.run(Application.class, args);
    }

}

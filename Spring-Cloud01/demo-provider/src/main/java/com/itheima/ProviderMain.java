package com.itheima;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.itheima.mapper")
@SpringBootApplication
public class ProviderMain {
    public static void main(String[] args) {
        SpringApplication.run(ProviderMain.class,args);
        System.out.println(("生产者启动"));
    }
}

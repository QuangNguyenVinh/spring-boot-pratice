package com.example.sbp;

import org.springframework.boot.SpringApplication;

public class TestSbpApplication {

    public static void main(String[] args) {
        SpringApplication.from(SbpApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

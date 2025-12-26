package com.example.sbp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MainController {

    @RequestMapping("/home")
    String home() {
        log.info("home() has been called");
        return "Hello World!";
    }
}

package com.tymofiivoitenko.telegram.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeResource {

    @GetMapping("/")
    public String home() {
        return "<h1>WELCOME</h1>";
    }
}
// src/main/java/com/example/demo/TestController.java
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String home() {
        String testEnv = System.getenv("TEST_ENV_VAR");
        return "Test Env: " + (testEnv != null ? testEnv : "Not Found");
    }
}
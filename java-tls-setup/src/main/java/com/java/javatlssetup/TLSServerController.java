package com.java.javatlssetup;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TLSServerController {
    @CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.3:3000"}, allowedHeaders = "*", allowCredentials = "true")
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, from TLS Server!";
    }
}

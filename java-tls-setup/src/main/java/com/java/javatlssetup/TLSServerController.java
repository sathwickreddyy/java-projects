package com.java.javatlssetup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TLSServerController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, from TLS Server!";
    }
}

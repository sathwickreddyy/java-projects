package com.java.javatlssetup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TLSServerController {

    @GetMapping("/hello")
    public String sayHello() {
        System.out.println("Called Bunny Server");
        return "Hello, from TLS Server!";
    }
}

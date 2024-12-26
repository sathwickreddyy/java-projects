package com.java.javatlssetupclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;



@RestController
public class TLSClientController {

    @Autowired
    WebClient webClient;

    @GetMapping("/clientToServer")
    public String clientToServer() {
        WebClient webClient = WebClient.create("http://localhost:8080");
        return webClient.get().uri("/hello").retrieve().bodyToMono(String.class).block();
    }

    @GetMapping("/clientToServerSecured")
    public String clientToServerHttps() {
        String response = null;
        try {
             response =  webClient.get().uri("https://localhost:8080/hello").retrieve().bodyToMono(String.class).block();
            return response;
        } catch (Exception e) {
            System.out.println("TLS Controller lo Error ra bunny "+e.getLocalizedMessage());
        }
        return response;
    }

}

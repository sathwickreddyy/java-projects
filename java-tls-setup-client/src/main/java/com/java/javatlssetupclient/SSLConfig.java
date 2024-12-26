package com.java.javatlssetupclient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

// Imports the trusts in SSL context helps when making API call to Server with SSL
@Configuration
public class SSLConfig {
    @Value("${bunny.client.trust-store-path}")
    private String trustStorePath;

    @Value("${bunny.client.trust-store-password}")
    private String trustStorePassword;

    @Value("${bunny.client.key-store-path}")
    private String keyStorePath;

    @Value("${bunny.client.key-store-password}")
    private String keyStorePassword;

    public SslContext buildSslContext() {
        SslContext sslContext = null;
        try(
                FileInputStream trustStoreFileInputStream = new FileInputStream(ResourceUtils.getFile(trustStorePath));
                FileInputStream keyStoreFileInputStream = new FileInputStream(ResourceUtils.getFile(keyStorePath));
        ) {
            KeyStore keyStore  = KeyStore.getInstance("jks");
            keyStore.load(keyStoreFileInputStream, keyStorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());


            KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(trustStoreFileInputStream, trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory  = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            sslContext = SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();
        }
        catch (Exception e) {
            System.out.println("Error ra bunny "+e.getLocalizedMessage());
        }
        return sslContext;
    }

    @Bean
    public WebClient webClient() {
        SslProvider sslProvider = SslProvider.builder().sslContext(buildSslContext()).build();
        reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create().secure(sslProvider);
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }
}

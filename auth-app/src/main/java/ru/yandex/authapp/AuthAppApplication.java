package ru.yandex.authapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.authapp.config.KeycloakProperties;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakProperties.class)
public class AuthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthAppApplication.class, args);
    }

}

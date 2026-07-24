package ru.yandex.authapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String realm;
    private String clientId;
    private String serverUrl;
    private String clientSecret;

    private Admin admin = new Admin();

    @Data
    public static class Admin {
        private String username;
        private String password;
    }
}

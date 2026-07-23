package ru.yandex.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] AUTH = {
            "/cart/**",
            "/orders/**",
            "/buy"
    };
    private static final String[] PERMIT_ALL = {
            "/",
            "/items/**",
            "/css/**",
            "/js/**",
            "/images/**"
    };

    @Bean
    public SecurityWebFilterChain security(ServerHttpSecurity http)  {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(PERMIT_ALL).permitAll()
                        .pathMatchers(AUTH).authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())

                .logout(logout -> logout.logoutUrl("/logout"))

                .build();
    }
}

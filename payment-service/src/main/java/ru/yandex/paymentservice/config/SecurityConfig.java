package ru.yandex.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange
                        (
                                exchange -> exchange

                        .pathMatchers("/actuator/**")
                        .permitAll()

                        /*
                         проверка чтения баланса
                         */
                        .pathMatchers(HttpMethod.GET,"/balance/**")
                        .hasAuthority("SCOPE_payments.read")

                        /*
                         проверка выполнения платежа
                         */
                        .pathMatchers(HttpMethod.POST, "/payment/**")
                        .hasAuthority("SCOPE_payments.write")

                        .anyExchange()
                        .authenticated()

                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()))

                .build();
    }
}

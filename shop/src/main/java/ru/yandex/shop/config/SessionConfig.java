package ru.yandex.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.time.Duration;
@Slf4j
@Configuration
@EnableRedisWebSession()
public class SessionConfig {


    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver() {
            @Override
            public void setSessionId(ServerWebExchange exchange, String sessionId) {
                log.info("=== SETTING SESSION COOKIE ===");
                log.info("Session ID: {}", sessionId);
                super.setSessionId(exchange, sessionId);
            }
        };

        resolver.setCookieName("SESSION");

        resolver.addCookieInitializer(builder -> {
            builder.path("/");
            builder.httpOnly(false); // временно false для отладки
            builder.sameSite("Lax");
            builder.secure(false);
            builder.maxAge(Duration.ofSeconds(1800));
        });

        return resolver;
    }
}

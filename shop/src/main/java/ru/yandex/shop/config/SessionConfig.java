package ru.yandex.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
@EnableRedisWebSession
public class SessionConfig {

    private final static String SESSION = "sessionId";
    private final static String LAX = "Lax";

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();

        resolver.setCookieName(SESSION);

        resolver.addCookieInitializer(cookie -> {
            cookie.path("/");
            cookie.httpOnly(true);
            cookie.sameSite(LAX);
        });

        return resolver;
    }

}

package ru.yandex.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
public class WebConfig implements WebFluxConfigurer {

    private final static String IMAGES = "/images/**";
    private final static String FILE = "file:uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(IMAGES)
                .addResourceLocations(FILE);

    }
}

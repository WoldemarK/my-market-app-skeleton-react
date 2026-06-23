package ru.yandex.mymarketappskeleton.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.config.ResourceHandlerRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebConfigTest {
    @Test
    void shouldRegisterResourceHandler() {

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/images/**")).thenReturn(registration);

        WebConfig webConfig = new WebConfig();
        webConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/images/**");
        verify(registration).addResourceLocations("file:uploads/");
    }
}
package ru.yandex.authapp.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.event.AuthorizationGrantedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEventListeners {

    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        var auth = event.getAuthentication().get();
        var resource = event.getObject();
        log.warn("Отказ в доступе: пользователь \"{}\" не имеет прав доступа к ресурсу {}",
                auth.getName(), resource);
    }

    @EventListener
    public void onAuthorizationGranted(AuthorizationGrantedEvent event) {
        var auth = event.getAuthentication().get();
        var resource = event.getObject();
        log.info("Успешная авторизация: пользователь \"{}\" получил доступ к ресурсу {}",
                auth.getName(), resource);

    }
}

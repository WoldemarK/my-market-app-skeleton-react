package ru.yandex.shop.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;

@Service
public class CartIdService {

    public String getCartId(Authentication authentication, WebSession session) {

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return authentication.getName();
        }
        return session.getId();
    }
}

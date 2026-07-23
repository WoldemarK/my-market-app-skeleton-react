package ru.yandex.authapp.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import ru.yandex.authapp.config.KeycloakProperties;
import ru.yandex.authapp.dto.RegisterRequest;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Keycloak keycloak;
    private final KeycloakProperties properties;

    public void register(RegisterRequest request) {

        RealmResource realm = keycloak.realm(properties.getRealm());

        UserRepresentation user = getUser(request);

        Response response = realm.users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("User already exists");
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        CredentialRepresentation credential = getCredential(request);

        realm.users()
                .get(userId)
                .resetPassword(credential);

        assignUserRole(realm, userId);

    }
    private void assignUserRole(RealmResource realm, String userId) {

        RoleRepresentation userRole = realm.roles()
                .get("USER")
                .toRepresentation();


        realm.users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(userRole));
    }
    private CredentialRepresentation getCredential(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        return credential;
    }

    private UserRepresentation getUser(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setLastName(request.lastname());
        user.setFirstName(request.firstname());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(Collections.emptyList());
        return user;
    }

}

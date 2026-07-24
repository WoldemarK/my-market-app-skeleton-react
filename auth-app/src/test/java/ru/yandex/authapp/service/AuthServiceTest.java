package ru.yandex.authapp.service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.authapp.config.KeycloakProperties;
import ru.yandex.authapp.dto.RegisterRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private KeycloakProperties properties;

    @Mock
    private RealmResource realm;

    @Mock
    private UsersResource users;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource roles;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(keycloak, properties);
    }

    @Test
    void register_success() {

        RegisterRequest request = new RegisterRequest(
                "Ivan",
                "Ivanov",
                "ivan@test.com",
                "password"
        );

        Response response = mock(Response.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();

        when(properties.getRealm()).thenReturn("test-realm");
        when(keycloak.realm("test-realm")).thenReturn(realm);
        when(realm.users()).thenReturn(users);
        when(users.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(users.get("user-id")).thenReturn(userResource);
        when(realm.roles()).thenReturn(roles);
        when(roles.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
        when(userResource.roles()).thenReturn(mock(RoleMappingResource.class));

        RoleMappingResource roleMapping = userResource.roles();

        when(roleMapping.realmLevel()).thenReturn(roleScopeResource);

        try (MockedStatic<CreatedResponseUtil> mocked = mockStatic(CreatedResponseUtil.class)) {

            mocked.when(() -> CreatedResponseUtil.getCreatedId(response)).thenReturn("user-id");

            authService.register(request);

            verify(users).create(any(UserRepresentation.class));
            verify(userResource).resetPassword(any(CredentialRepresentation.class));
            verify(roleScopeResource).add(anyList());
        }
    }
    @Test
    void register_userAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "Ivan",
                "Ivanov",
                "ivan@test.com",
                "password"
        );

        Response response = mock(Response.class);

        when(properties.getRealm()).thenReturn("test-realm");
        when(keycloak.realm("test-realm")).thenReturn(realm);
        when(realm.users()).thenReturn(users);
        when(users.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }
}
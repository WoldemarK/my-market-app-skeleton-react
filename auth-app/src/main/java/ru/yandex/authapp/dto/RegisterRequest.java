package ru.yandex.authapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest
        (
                String email,
                String password,
                @JsonProperty("first_name")
                String firstname,
                @JsonProperty("last_name")
                String lastname
        ) {
}

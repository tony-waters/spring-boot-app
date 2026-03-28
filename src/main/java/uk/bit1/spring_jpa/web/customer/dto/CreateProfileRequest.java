package uk.bit1.spring_jpa.web.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateProfileRequest(
        @Email
        @NotBlank
        String emailAddress,
        boolean marketingOptIn
) {}
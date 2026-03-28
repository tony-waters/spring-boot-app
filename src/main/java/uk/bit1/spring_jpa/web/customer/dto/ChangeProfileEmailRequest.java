package uk.bit1.spring_jpa.web.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeProfileEmailRequest(
        @Email
        @NotBlank
        String emailAddress
) {}
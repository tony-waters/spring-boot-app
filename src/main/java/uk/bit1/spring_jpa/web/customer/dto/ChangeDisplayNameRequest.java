package uk.bit1.spring_jpa.web.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeDisplayNameRequest(
        @NotBlank
        @Size(min = 2, max = 80)
        String displayName
) {}
package uk.bit1.spring_jpa.web.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeTicketDescriptionRequest(
        @NotBlank
        @Size(min = 10, max = 255)
        String description
) {}
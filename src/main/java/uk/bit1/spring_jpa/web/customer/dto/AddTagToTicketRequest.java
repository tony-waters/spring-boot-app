package uk.bit1.spring_jpa.web.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTagToTicketRequest(
        @NotBlank
        @Size(max = 50)
        String tagName
) {}
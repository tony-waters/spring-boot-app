package uk.bit1.spring_jpa.application.customer.query;

import uk.bit1.spring_jpa.domain.customer.TicketStatus;

import java.util.List;

public record TicketDetailView(
        Long id,
        String description,
        TicketStatus status,
        List<String> tagNames
) {}
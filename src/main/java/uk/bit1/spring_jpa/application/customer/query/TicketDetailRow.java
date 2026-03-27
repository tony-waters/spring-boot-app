package uk.bit1.spring_jpa.application.customer.query;

import uk.bit1.spring_jpa.domain.customer.TicketStatus;

public record TicketDetailRow(
        Long ticketId,
        String description,
        TicketStatus status,
        String tagName
) {}
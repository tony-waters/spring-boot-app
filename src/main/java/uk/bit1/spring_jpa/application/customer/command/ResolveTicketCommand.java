package uk.bit1.spring_jpa.application.customer.command;

public record ResolveTicketCommand(Long customerId, Long ticketId) {}
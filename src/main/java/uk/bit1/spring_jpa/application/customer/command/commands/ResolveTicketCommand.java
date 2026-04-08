package uk.bit1.spring_jpa.application.customer.command.commands;

public record ResolveTicketCommand(Long customerId, Long ticketId) {}
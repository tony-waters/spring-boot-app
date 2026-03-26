package uk.bit1.spring_jpa.application.customer.command;

public record ReopenTicketCommand(Long customerId, Long ticketId) {}
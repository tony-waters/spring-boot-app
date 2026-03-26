package uk.bit1.spring_jpa.application.customer.command;

public record CloseTicketCommand(Long customerId, Long ticketId) {}
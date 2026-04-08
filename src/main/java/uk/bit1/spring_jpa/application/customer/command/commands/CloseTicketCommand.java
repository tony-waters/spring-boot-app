package uk.bit1.spring_jpa.application.customer.command.commands;

public record CloseTicketCommand(Long customerId, Long ticketId) {}
package uk.bit1.spring_jpa.application.customer.command.commands;

public record RemoveTicketCommand(Long customerId, Long ticketId) {}
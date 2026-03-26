package uk.bit1.spring_jpa.application.customer.command;

public record RemoveTicketCommand(Long customerId, Long ticketId) {}
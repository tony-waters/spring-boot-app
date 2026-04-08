package uk.bit1.spring_jpa.application.customer.command.commands;

public record RemoveTagFromTicketCommand(Long customerId, Long ticketId, String tagName) {}
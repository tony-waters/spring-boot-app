package uk.bit1.spring_jpa.application.customer.command.commands;

public record AddTagToTicketCommand(Long customerId, Long ticketId, String tagName) {}
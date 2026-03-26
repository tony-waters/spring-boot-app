package uk.bit1.spring_jpa.application.customer.command;

public record AddTagToTicketCommand(Long customerId, Long ticketId, String tagName) {}
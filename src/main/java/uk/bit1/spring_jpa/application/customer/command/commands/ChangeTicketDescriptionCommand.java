package uk.bit1.spring_jpa.application.customer.command.commands;

public record ChangeTicketDescriptionCommand(Long customerId, Long ticketId, String description) {}
package uk.bit1.spring_jpa.application.customer.command;

public record ChangeTicketDescriptionCommand(Long customerId, Long ticketId, String description) {}
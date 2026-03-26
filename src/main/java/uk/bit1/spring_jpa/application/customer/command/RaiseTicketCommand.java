package uk.bit1.spring_jpa.application.customer.command;

public record RaiseTicketCommand(Long customerId, String description) {}
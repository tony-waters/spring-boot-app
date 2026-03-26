package uk.bit1.spring_jpa.application.customer.command;

public record StartTicketWorkCommand(Long customerId, Long ticketId) {}
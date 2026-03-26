package uk.bit1.spring_jpa.application.customer.command;

public record ChangeProfileEmailCommand(Long customerId, String emailAddress) {}
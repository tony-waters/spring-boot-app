package uk.bit1.spring_jpa.application.customer.command.commands;

public record ChangeProfileEmailCommand(Long customerId, String emailAddress) {}
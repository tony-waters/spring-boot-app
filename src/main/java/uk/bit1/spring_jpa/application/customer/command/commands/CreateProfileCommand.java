package uk.bit1.spring_jpa.application.customer.command.commands;

public record CreateProfileCommand(Long customerId, String emailAddress, boolean marketingOptIn) {}
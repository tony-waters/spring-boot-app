package uk.bit1.spring_jpa.application.customer.command;

public record CreateProfileCommand(Long customerId, String emailAddress, boolean marketingOptIn) {}
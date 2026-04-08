package uk.bit1.spring_jpa.application.customer.command.commands;

public record ChangeDisplayNameCommand(Long customerId, String displayName) {}
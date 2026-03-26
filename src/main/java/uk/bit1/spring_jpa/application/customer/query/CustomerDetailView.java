package uk.bit1.spring_jpa.application.customer.query;

public record CustomerDetailView(
        Long id,
        Long version,
        String displayName,
        String emailAddress,
        Boolean marketingOptIn
) {}
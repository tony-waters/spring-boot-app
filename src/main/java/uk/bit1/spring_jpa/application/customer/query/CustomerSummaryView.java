package uk.bit1.spring_jpa.application.customer.query;

public record CustomerSummaryView(
        Long id,
        String displayName
) {}
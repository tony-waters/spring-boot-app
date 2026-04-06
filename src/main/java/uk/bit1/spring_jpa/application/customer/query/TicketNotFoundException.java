package uk.bit1.spring_jpa.application.customer.query;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long customerId, Long ticketId) {
        super("Ticket not found for customer: customerId=%d, ticketId=%d"
                .formatted(customerId, ticketId));
    }
}
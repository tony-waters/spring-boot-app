package uk.bit1.spring_jpa.web.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.bit1.spring_jpa.application.customer.query.CustomerDetailView;
import uk.bit1.spring_jpa.application.customer.query.CustomerQueryService;
import uk.bit1.spring_jpa.application.customer.query.CustomerSummaryView;
import uk.bit1.spring_jpa.application.customer.query.TicketDetailView;
import uk.bit1.spring_jpa.application.customer.query.TicketListItemView;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
class CustomerQueryController {

    private final CustomerQueryService customerQueryService;

    @GetMapping
    Page<CustomerSummaryView> findAllCustomers(Pageable pageable) {
        return customerQueryService.findAllCustomers(pageable);
    }

    @GetMapping("/{customerId}")
    CustomerDetailView findCustomer(@PathVariable Long customerId) {
        return customerQueryService.findCustomerDetail(customerId);
    }

    @GetMapping("/{customerId}/tickets")
    List<TicketListItemView> findTickets(@PathVariable Long customerId) {
        return customerQueryService.findTicketsForCustomer(customerId);
    }

    @GetMapping("/{customerId}/tickets/{ticketId}")
    TicketDetailView findTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        return customerQueryService.findTicketDetail(customerId, ticketId);
    }
}
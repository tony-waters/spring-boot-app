package uk.bit1.spring_jpa.application.customer.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerQueryRepository customerQueryRepository;

    public Page<CustomerSummaryView> findAllCustomers(Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("displayName").ascending()
        );

        return customerQueryRepository.findAllSummaries(effectivePageable);
    }

    public CustomerDetailView findCustomerDetail(Long customerId) {
        return customerQueryRepository.findDetailById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    public List<TicketListItemView> findTicketsForCustomer(Long customerId) {
        return customerQueryRepository.findTicketsByCustomerId(customerId);
    }

    public TicketDetailView findTicketDetail(Long customerId, Long ticketId) {
        List<TicketDetailRow> rows = customerQueryRepository.findTicketDetailRows(customerId, ticketId);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ticket not found for customer: customerId=%d, ticketId=%d"
                            .formatted(customerId, ticketId)
            );
        }

        TicketDetailRow first = rows.getFirst();

        List<String> tagNames = rows.stream()
                .map(TicketDetailRow::tagName)
                .filter(name -> name != null)
                .toList();

        return new TicketDetailView(
                first.ticketId(),
                first.description(),
                first.status(),
                tagNames
        );
    }
}
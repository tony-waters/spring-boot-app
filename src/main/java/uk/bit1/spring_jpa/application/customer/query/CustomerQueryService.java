package uk.bit1.spring_jpa.application.customer.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerQueryRepository customerQueryRepository;

    public Page<CustomerSummaryView> findCustomers(String name, Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("displayName").ascending()
        );

        String normalizedName = normalizeFilter(name);

        return customerQueryRepository.findCustomerSummaries(normalizedName, effectivePageable);
    }

    public CustomerDetailView findCustomerDetail(Long customerId) {
        return customerQueryRepository.findDetailById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    public List<TicketListItemView> findTicketsForCustomer(Long customerId, TicketStatus status, String tagName) {
        String normalizedTagName = normalizeFilter(tagName);

        if (status != null && normalizedTagName != null) {
            throw new IllegalArgumentException("Filter by status or tag, not both");
        }

        if (status != null) {
            return customerQueryRepository.findTicketsByCustomerIdAndStatus(customerId, status);
        }

        if (normalizedTagName != null) {
            return customerQueryRepository.findTicketsByCustomerIdAndTagName(customerId, normalizedTagName);
        }

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

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}
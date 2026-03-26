package uk.bit1.spring_jpa.application.customer.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerQueryRepository customerQueryRepository;

    public List<CustomerSummaryView> findAllCustomers() {
        return customerQueryRepository.findAllSummaries();
    }

    public CustomerDetailView findCustomerDetail(Long customerId) {
        return customerQueryRepository.findDetailById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
    }

    public List<TicketListItemView> findTicketsForCustomer(Long customerId) {
        return customerQueryRepository.findTicketsByCustomerId(customerId);
    }
}
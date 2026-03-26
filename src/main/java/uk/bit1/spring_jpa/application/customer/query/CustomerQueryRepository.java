package uk.bit1.spring_jpa.application.customer.query;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import uk.bit1.spring_jpa.domain.customer.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerQueryRepository extends Repository<Customer, Long> {

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.CustomerSummaryView(
            c.id,
            c.displayName
        )
        from Customer c
        order by c.displayName
    """)
    List<CustomerSummaryView> findAllSummaries();

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.CustomerDetailView(
            c.id,
            c.version,
            c.displayName,
            p.emailAddress,
            p.marketingOptIn
        )
        from Customer c
        left join c.profile p
        where c.id = :customerId
    """)
    Optional<CustomerDetailView> findDetailById(Long customerId);

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.TicketListItemView(
            t.id,
            t.description,
            t.status
        )
        from Customer c
        join c.tickets t
        where c.id = :customerId
        order by t.id
    """)
    List<TicketListItemView> findTicketsByCustomerId(Long customerId);
}
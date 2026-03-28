package uk.bit1.spring_jpa.application.customer.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface CustomerQueryRepository extends Repository<Customer, Long> {

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.CustomerSummaryView(
            c.id,
            c.displayName
        )
        from Customer c
        where (:name is null or lower(c.displayName) like lower(concat('%', :name, '%')))
    """)
    Page<CustomerSummaryView> findCustomerSummaries(String name, Pageable pageable);

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

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.TicketListItemView(
            t.id,
            t.description,
            t.status
        )
        from Customer c
        join c.tickets t
        where c.id = :customerId
          and t.status = :status
        order by t.id
    """)
    List<TicketListItemView> findTicketsByCustomerIdAndStatus(Long customerId, TicketStatus status);

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.TicketListItemView(
            t.id,
            t.description,
            t.status
        )
        from Customer c
        join c.tickets t
        join t.tags tag
        where c.id = :customerId
          and lower(tag.name) = lower(:tagName)
        order by t.id
    """)
    List<TicketListItemView> findTicketsByCustomerIdAndTagName(Long customerId, String tagName);

    @Query("""
        select new uk.bit1.spring_jpa.application.customer.query.TicketDetailRow(
            t.id,
            t.description,
            t.status,
            tag.name
        )
        from Customer c
        join c.tickets t
        left join t.tags tag
        where c.id = :customerId
          and t.id = :ticketId
        order by tag.name
    """)
    List<TicketDetailRow> findTicketDetailRows(Long customerId, Long ticketId);
}
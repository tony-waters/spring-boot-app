package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}

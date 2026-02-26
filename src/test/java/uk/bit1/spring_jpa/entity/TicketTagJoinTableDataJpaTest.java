package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.repository.CustomerRepository;
import uk.bit1.spring_jpa.repository.TagRepository;
import uk.bit1.spring_jpa.repository.TicketRepository;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TicketTagJoinTableDataJpaTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired TagRepository tagRepository;

    @Test
    void addingTagPersistsJoinTableAssociation() {
        Tag urgent = tagRepository.saveAndFlush(new Tag("urgent"));
        Tag bug = tagRepository.saveAndFlush(new Tag("bug"));

        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");

        // persist customer+ticket first (cascade)
        customerRepository.saveAndFlush(c);
        Long ticketId = t.getId();
        assertThat(ticketId).isNotNull();

        // attach tags (editable in OPEN)
        t.addTag(urgent);
        t.addTag(bug);

        ticketRepository.saveAndFlush(t);

        Ticket reloaded = ticketRepository.findById(ticketId).orElseThrow();

        // access within @DataJpaTest transaction => LAZY ok
        assertThat(reloaded.getTags())
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("urgent", "bug");
    }

    @Test
    void tagsCannotBeEditedAfterResolvedOrClosedEvenInJpaContext() {
        Tag urgent = tagRepository.saveAndFlush(new Tag("urgent"));

        Customer c = new Customer("tonyW");
        Ticket t = c.raiseTicket("This is a valid description.");
        customerRepository.saveAndFlush(c);

        t.resolve();
        ticketRepository.saveAndFlush(t);

        assertThatThrownBy(() -> t.addTag(urgent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESOLVED");
    }
}

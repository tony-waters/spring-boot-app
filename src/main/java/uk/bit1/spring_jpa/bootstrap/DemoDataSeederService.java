package uk.bit1.spring_jpa.bootstrap;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.CustomerRepository;
import uk.bit1.spring_jpa.domain.tag.Tag;
import uk.bit1.spring_jpa.domain.tag.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataSeederService {

    private static final int CUSTOMER_COUNT = 5_000;
//    private static final int CUSTOMER_COUNT = 100;
    private static final int BATCH_SIZE = 250;
    private static final long RANDOM_SEED = 42L;

    private final CustomerRepository customerRepository;
    private final TagRepository tagRepository;
    private final EntityManager entityManager;

    private final Random random = new Random(RANDOM_SEED);

    @Transactional
    public void seedIfEmpty() {
        if (customerRepository.count() > 0) {
            log.info("DB has Customers! count: " + customerRepository.count());
            log.info("Seed skipped: data already exists");
            return;
        }

        log.info("Seeding started");

        List<Tag> tags = createTags();
        createCustomersWithTickets();

        entityManager.clear();

        enrichTickets(tags);

        log.info("Seed complete: {} customers", customerRepository.count());
    }

    private void createCustomersWithTickets() {
        List<Customer> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < CUSTOMER_COUNT; i++) {
            Customer customer = buildCustomer(i);
            batch.add(customer);

            if (batch.size() == BATCH_SIZE) {
                customerRepository.saveAll(batch);
                customerRepository.flush();
                batch.clear();
                log.info("Created {} customers", i + 1);
            }
        }

        if (!batch.isEmpty()) {
            customerRepository.saveAll(batch);
            customerRepository.flush();
        }
    }

    private Customer buildCustomer(int index) {
        String displayName = randomDisplayName(index);
        Customer customer = new Customer(displayName);

        if (chance(0.72)) {
            customer.createProfile(emailFor(displayName, index), chance(0.40));
        }

        int ticketCount = weightedTicketCount();
        for (int i = 0; i < ticketCount; i++) {
            customer.raiseTicket(randomTicketDescription());
        }

        return customer;
    }

    @Transactional
    protected void enrichTickets(List<Tag> tags) {
        List<Long> customerIds = entityManager.createQuery("""
            select c.id
            from Customer c
            order by c.id
            """, Long.class)
                .getResultList();

        int processed = 0;

        for (Long customerId : customerIds) {
            Customer customer = customerRepository.findAggregateById(customerId).orElseThrow();

            List<Long> ticketIds = entityManager.createQuery("""
                select t.id
                from Customer c
                join c.tickets t
                where c.id = :customerId
                order by t.id
                """, Long.class)
                    .setParameter("customerId", customerId)
                    .getResultList();

            for (Long ticketId : ticketIds) {

                // add tags while ticket is still editable
                int tagCount = random.nextInt(4); // 0-3 tags
                List<Tag> chosen = pickDistinctTags(tags, tagCount);
                for (Tag tag : chosen) {
                    customer.addTagToTicket(ticketId, tag);
                }

                // then move to final state
                int roll = random.nextInt(100);

                if (roll < 25) {
                    customer.startTicketWork(ticketId);
                } else if (roll < 45) {
                    customer.startTicketWork(ticketId);
                    customer.resolveTicket(ticketId);
                } else if (roll < 55) {
                    customer.startTicketWork(ticketId);
                    customer.resolveTicket(ticketId);
                    customer.closeTicket(ticketId);
                }
                // else leave OPEN
            }

            processed++;
            if (processed % BATCH_SIZE == 0) {
                customerRepository.flush();
                entityManager.clear();
                log.info("Enriched {} customers", processed);
            }
        }

        customerRepository.flush();
        entityManager.clear();
    }
    private List<Tag> createTags() {
        List<String> names = List.of(
                "bug", "urgent", "billing", "support", "onboarding",
                "api", "ui", "performance", "reporting", "data-import"
        );

        List<Tag> tags = new ArrayList<>();
        for (String name : names) {
            tags.add(tagRepository.save(new Tag(name)));
        }

        tagRepository.flush();
        return tags;
    }

    private List<Tag> pickDistinctTags(List<Tag> tags, int count) {
        List<Tag> copy = new ArrayList<>(tags);
        List<Tag> result = new ArrayList<>(count);

        for (int i = 0; i < count && !copy.isEmpty(); i++) {
            int index = random.nextInt(copy.size());
            result.add(copy.remove(index));
        }

        return result;
    }

    private String randomDisplayName(int index) {
        String[] firstNames = {
                "Tony", "Anthony", "Tom", "Toni", "Alice", "Bob", "Charlie", "David",
                "Emily", "Emma", "Olivia", "Liam", "Noah", "Ava", "Mia", "Sophia",
                "James", "Daniel", "Grace", "Ella", "Henry", "Isla", "Jack", "Leo", "Freya"
        };

        String[] lastNames = {
                "Smith", "Jones", "Taylor", "Brown", "Williams", "Davies", "Evans",
                "Thomas", "Roberts", "Johnson", "Walker", "Wright", "Hall", "Allen",
                "Young", "King"
        };

        String first = firstNames[random.nextInt(firstNames.length)];
        String last = lastNames[random.nextInt(lastNames.length)];

        if (index % 250 == 0) {
            first = "Tony";
        }

        return first + " " + last;
    }

    private String emailFor(String displayName, int index) {
        String local = displayName.toLowerCase()
                .replace(" ", ".")
                .replace("'", "");
        return local + "." + index + "@example.test";
    }

    private int weightedTicketCount() {
        int roll = random.nextInt(100);
        if (roll < 20) return 0;
        if (roll < 45) return 1 + random.nextInt(2);
        if (roll < 70) return 3 + random.nextInt(3);
        if (roll < 90) return 6 + random.nextInt(5);
        return 11 + random.nextInt(10);
    }

    private String randomTicketDescription() {
        String[] subjects = {
                "Unable to log in",
                "Billing mismatch on invoice",
                "Export fails on large dataset",
                "Search results are incorrect",
                "API returns unexpected response",
                "Performance issue on dashboard",
                "User onboarding flow is broken",
                "Report generation takes too long",
                "File import fails intermittently",
                "Notification email not received"
        };

        String[] qualifiers = {
                "since yesterday morning",
                "for one specific customer account",
                "when using the mobile browser",
                "after the latest release",
                "under moderate load",
                "during CSV upload",
                "on the reporting screen",
                "when filtering by status",
                "when searching by customer name",
                "after password reset"
        };

        return subjects[random.nextInt(subjects.length)] + " " +
                qualifiers[random.nextInt(qualifiers.length)];
    }

    private boolean chance(double probability) {
        return random.nextDouble() < probability;
    }
}
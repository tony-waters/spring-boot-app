package uk.bit1.spring_jpa.application.customer.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bit1.spring_jpa.application.customer.command.commands.*;
import uk.bit1.spring_jpa.application.customer.query.CustomerNotFoundException;
import uk.bit1.spring_jpa.domain.customer.Customer;
import uk.bit1.spring_jpa.domain.customer.CustomerRepository;
import uk.bit1.spring_jpa.domain.tag.Tag;
import uk.bit1.spring_jpa.domain.tag.TagRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerCommandService {

    private final CustomerRepository customerRepository;
    private final TagRepository tagRepository;

    public Long createCustomer(CreateCustomerCommand cmd) {
        Customer customer = new Customer(cmd.displayName());
        customerRepository.save(customer);
        return customer.getId();
    }

    public void changeDisplayName(ChangeDisplayNameCommand cmd) {
        loadCustomer(cmd.customerId()).changeDisplayName(cmd.displayName());
    }

    public void createProfile(CreateProfileCommand cmd) {
        loadCustomer(cmd.customerId()).createProfile(cmd.emailAddress(), cmd.marketingOptIn());
    }

    public void changeProfileEmail(ChangeProfileEmailCommand cmd) {
        loadCustomer(cmd.customerId()).changeProfileEmailAddress(cmd.emailAddress());
    }

    public void raiseTicket(RaiseTicketCommand cmd) {
        loadCustomer(cmd.customerId()).raiseTicket(cmd.description());
    }

    public void changeTicketDescription(ChangeTicketDescriptionCommand cmd) {
        loadCustomer(cmd.customerId()).changeTicketDescription(cmd.ticketId(), cmd.description());
    }

    public void startTicketWork(StartTicketWorkCommand cmd) {
        loadCustomer(cmd.customerId()).startTicketWork(cmd.ticketId());
    }

    public void resolveTicket(ResolveTicketCommand cmd) {
        loadCustomer(cmd.customerId()).resolveTicket(cmd.ticketId());
    }

    public void reopenTicket(ReopenTicketCommand cmd) {
        loadCustomer(cmd.customerId()).reopenTicket(cmd.ticketId());
    }

    public void closeTicket(CloseTicketCommand cmd) {
        loadCustomer(cmd.customerId()).closeTicket(cmd.ticketId());
    }

    public void addTagToTicket(AddTagToTicketCommand cmd) {
        Customer customer = loadCustomer(cmd.customerId());
        Tag tag = loadOrCreateTag(cmd.tagName());
        customer.addTagToTicket(cmd.ticketId(), tag);
    }

    public void removeTagFromTicket(RemoveTagFromTicketCommand cmd) {
        Customer customer = loadCustomer(cmd.customerId());
        Tag tag = findTag(cmd.tagName());
        customer.removeTagFromTicket(cmd.ticketId(), tag);
    }

    public void removeTicket(RemoveTicketCommand cmd) {
        loadCustomer(cmd.customerId()).removeTicket(cmd.ticketId());
    }

    private Customer loadCustomer(Long customerId) {
        return customerRepository.findAggregateById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private Tag loadOrCreateTag(String rawName) {
        String normalized = normalizeTagName(rawName);
        return tagRepository.findByName(normalized)
                .orElseGet(() -> tagRepository.save(new Tag(normalized)));
    }

    private Tag findTag(String rawName) {
        String normalized = normalizeTagName(rawName);
        return tagRepository.findByName(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + normalized));
    }

    private String normalizeTagName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("tagName must not be blank");
        }
        return rawName.strip().toLowerCase();
    }
}
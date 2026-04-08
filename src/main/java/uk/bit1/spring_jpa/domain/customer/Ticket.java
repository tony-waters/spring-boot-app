package uk.bit1.spring_jpa.domain.customer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.bit1.spring_jpa.domain.common.BaseEntity;
import uk.bit1.spring_jpa.domain.tag.Tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "ticket",
        indexes = @Index(name = "idx_ticket_customer_id", columnList = "customer_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Ticket extends BaseEntity {

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @Getter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ticket_tag",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Getter
    @Column(nullable = false, length = 255)
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    Ticket(Customer customer, String description) {
        this.customer = Objects.requireNonNull(customer, "customer must not be null");
        this.description = validateDescription(description);
        this.status = TicketStatus.OPEN;
    }

    Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    void changeDescription(String description) {
        requireEditable("changeDescription");
        this.description = validateDescription(description);
    }

    void addTag(Tag tag) {
        requireEditable("addTag");
        if (tag == null) return;
        tags.add(tag);
    }

    void removeTag(Tag tag) {
        requireEditable("removeTag");
        if (tag == null) return;
        tags.remove(tag);
    }

    void clearTags() {
        requireEditable("clearTags");
        tags.clear();
    }

    void startWork() {
        transitionTo(TicketStatus.IN_PROGRESS, "startWork", TicketStatus.OPEN);
    }

    void resolve() {
        transitionTo(TicketStatus.RESOLVED, "resolve", TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
    }

    void reopen() {
        transitionTo(TicketStatus.OPEN, "reopen", TicketStatus.RESOLVED);
    }

    void close() {
        transitionTo(TicketStatus.CLOSED, "close", TicketStatus.RESOLVED);
    }

    void markRemovedFromCustomer() {
        this.customer = null;
    }

    Long customerId() {
        return customer == null ? null : customer.getId();
    }

    private static String validateDescription(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        String stripped = value.strip();
        if (stripped.isBlank() || stripped.length() < 10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
        return stripped;
    }

    private void requireNotClosed(String action) {
        if (status == TicketStatus.CLOSED) {
            throw new IllegalStateException("Cannot " + action + " when ticket is CLOSED");
        }
    }

    private void requireEditable(String action) {
        requireNotClosed(action);
        if (status == TicketStatus.RESOLVED) {
            throw new IllegalStateException("Cannot " + action + " when ticket is RESOLVED");
        }
    }

    private void transitionTo(TicketStatus target, String action, TicketStatus... allowedFrom) {
        requireNotClosed(action);
        for (TicketStatus source : allowedFrom) {
            if (status == source) {
                status = target;
                return;
            }
        }
        throw new IllegalStateException("Cannot " + action + " when ticket is " + status);
    }
}
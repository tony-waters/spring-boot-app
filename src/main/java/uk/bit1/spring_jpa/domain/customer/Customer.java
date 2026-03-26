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
@Table(name = "customer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @Getter
    private Long id;

    @Getter
    @Column(name = "display_name", length = 80, nullable = false)
    private String displayName;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", unique = true)
    private Profile profile;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Ticket> tickets = new HashSet<>();

    public Customer(String displayName) {
        this.displayName = validateDisplayName(displayName);
    }

    Profile getProfileInternal() {
        return profile;
    }

    Set<Ticket> getTicketsInternal() {
        return Collections.unmodifiableSet(tickets);
    }

    public void changeDisplayName(String displayName) {
        this.displayName = validateDisplayName(displayName);
    }

    public boolean hasProfile() {
        return profile != null;
    }

    public String profileEmailAddress() {
        requireProfile();
        return profile.getEmailAddress();
    }

    public boolean profileMarketingOptIn() {
        requireProfile();
        return profile.isMarketingOptIn();
    }

    public int ticketCount() {
        return tickets.size();
    }

    public boolean hasTicket(Long ticketId) {
        return tickets.stream().anyMatch(ticket -> Objects.equals(ticket.getId(), ticketId));
    }

    public TicketStatus ticketStatus(Long ticketId) {
        return findTicket(ticketId).getStatus();
    }

    public boolean ticketHasTag(Long ticketId, Tag tag) {
        Objects.requireNonNull(tag, "tag must not be null");
        return findTicket(ticketId).getTags().contains(tag);
    }

    public int ticketTagCount(Long ticketId) {
        return findTicket(ticketId).getTags().size();
    }

    public String ticketDescription(Long ticketId) {
        return findTicket(ticketId).getDescription();
    }

    public void createProfile(String emailAddress, boolean marketingOptIn) {
        if (this.profile != null) {
            throw new IllegalStateException("Customer already has a profile");
        }
        this.profile = new Profile(emailAddress, marketingOptIn);
    }

    public void changeProfileEmailAddress(String emailAddress) {
        requireProfile();
        profile.changeEmailAddress(emailAddress);
    }

    public void optInToMarketing() {
        requireProfile();
        profile.optInToMarketing();
    }

    public void optOutOfMarketing() {
        requireProfile();
        profile.optOutOfMarketing();
    }

    public void removeProfile() {
        requireProfile();
        this.profile = null;
    }

    public void raiseTicket(String description) {
        tickets.add(new Ticket(this, description));
    }

    public void changeTicketDescription(Long ticketId, String description) {
        findTicket(ticketId).changeDescription(description);
    }

    public void startTicketWork(Long ticketId) {
        findTicket(ticketId).startWork();
    }

    public void resolveTicket(Long ticketId) {
        findTicket(ticketId).resolve();
    }

    public void reopenTicket(Long ticketId) {
        findTicket(ticketId).reopen();
    }

    public void closeTicket(Long ticketId) {
        findTicket(ticketId).close();
    }

    public void addTagToTicket(Long ticketId, Tag tag) {
        Objects.requireNonNull(tag, "tag must not be null");
        findTicket(ticketId).addTag(tag);
    }

    public void removeTagFromTicket(Long ticketId, Tag tag) {
        Objects.requireNonNull(tag, "tag must not be null");
        findTicket(ticketId).removeTag(tag);
    }

    public void clearTagsFromTicket(Long ticketId) {
        findTicket(ticketId).clearTags();
    }

    public void removeTicket(Long ticketId) {
        Ticket ticket = findTicket(ticketId);
        tickets.remove(ticket);
        ticket.markRemovedFromCustomer();
    }

    private Ticket findTicket(Long ticketId) {
        return tickets.stream()
                .filter(ticket -> Objects.equals(ticket.getId(), ticketId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket does not belong to customer: " + ticketId));
    }

    private void requireProfile() {
        if (profile == null) {
            throw new IllegalStateException("Customer has no profile");
        }
    }

    private static String validateDisplayName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("displayName must have a value");
        }
        return value.strip();
    }
}
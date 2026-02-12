package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_tag_name", columnNames = "name")
)
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Ticket> tickets = new HashSet<>();

    protected Tag() {}

    public Tag(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank");
        this.name = name;
    }

    public String getName() { return name; }

    public Set<Ticket> getTickets() {
        return java.util.Collections.unmodifiableSet(tickets);
    }

    // package-private to stop random callers messing with invariants
    void addTicketInternal(Ticket ticket) {
        tickets.add(ticket);
    }

    void removeTicketInternal(Ticket ticket) {
        tickets.remove(ticket);
    }
}


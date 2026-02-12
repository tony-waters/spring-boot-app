package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uc_tag_name", columnNames = "name")
)
public class Tag extends BaseEntity {

    @Getter // no setter by design
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // getter below, no setter by design
    @ManyToMany(mappedBy = "tags")
    private Set<Ticket> tickets = new HashSet<>();

    protected Tag() {}

    public Tag(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank");
        this.name = name;
    }

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


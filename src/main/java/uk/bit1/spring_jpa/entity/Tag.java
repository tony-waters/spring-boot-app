package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uc_tag_name", columnNames = "name")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Id
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter  // no setter by design
    private Long id;

    @Getter // no setter by design
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // getter below, no setter by design
    @ManyToMany(mappedBy = "tags")
    private Set<Ticket> tickets = new HashSet<>();

    // ---- Constructors ----

    public Tag(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tag name must not be blank");
        this.name = name;
    }

    // ---- Getters ----

//    // TODO: this could be enormous - use Repository
//    public Set<Ticket> getTickets() {
//        return java.util.Collections.unmodifiableSet(tickets);
//    }

    // ---- Domain logic - Maintain relationship invariants for Tag -> Ticket ----

        // (Handled by Ticket entity)

    // ---- Domain logic - Maintain local state transition invariants ----

        // TODO: handle local state transition

    // ---- Internal helper methods ----

    void addTicketInternal(Ticket ticket) {
        if(ticket == null) throw new IllegalArgumentException("Ticket must not be null");
        tickets.add(ticket);
    }

    void removeTicketInternal(Ticket ticket) {
        tickets.remove(ticket);
    }
}


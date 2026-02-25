package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
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

    // unmodifiable getter below - no setter on Collection by design
    @ManyToMany(mappedBy = "tags")
    private Set<Ticket> tickets = new HashSet<>();

    @Getter // no setter by design
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // ---- Constructors ----

    public Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        // strip and normailise to lower case
        this.name = name.strip().toLowerCase();
    }

    // ---- Collection getters ----

    Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    // ---- Tag -> Ticket relationship ----
    // (public control of relationship handled by Ticket entity)

    void addTicketInternal(Ticket ticket) {
        tickets.add(ticket);
    }

    void removeTicketInternal(Ticket ticket) {
        tickets.remove(ticket);
    }

}


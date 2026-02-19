package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity {

    // Optimistic locking
    @Getter  // no setter by design
    @Version
    private Long version;

    @Getter  // no setter by design
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter  // no setter by design
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

    // ---- Hooks to set createdAt and updatedAt values ----

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ---- equals() and hashCode() ----

    public abstract Long getId();

    // override equals() and hashCode() to compare DB id
    // ... needs to be 'proxy safe'
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        // compare the real entity class, not the proxy class
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        BaseEntity that = (BaseEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        // stable across proxies and before/after initialization
        // ... though not the best performing approach
        return Hibernate.getClass(this).hashCode();
//        return Objects.hash(Hibernate.getClass(this), getId());
    }
}

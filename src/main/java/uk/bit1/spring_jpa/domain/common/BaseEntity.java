package uk.bit1.spring_jpa.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.Hibernate;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {

    @Getter
    @Version
    private Long version;

    @Getter
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public abstract Long getId();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        BaseEntity that = (BaseEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    // This implementation is widely recommended for JPA entities (including by experts such as Vlad Mihalcea) when equals()
    // is based on the entity's database identity:
    //
    // - Stable before and after persistence.
    // - Works correctly with Hibernate proxies.
    // - Avoids breaking HashSet/HashMap when IDs are assigned.
    // Produces many hash collisions, but that's generally considered an acceptable trade-off for correctness in JPA
    //  entities.
    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
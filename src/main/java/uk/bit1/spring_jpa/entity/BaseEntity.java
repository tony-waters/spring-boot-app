package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @SequenceGenerator(name="global_seq", sequenceName="global_seq", allocationSize=50)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="global_seq")
    @Getter
    private Long id;

    // Optimistic locking
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
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // No setters for id/version/timestamps by design.

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if(o == null)  return false;
        if (!(o instanceof BaseEntity that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}

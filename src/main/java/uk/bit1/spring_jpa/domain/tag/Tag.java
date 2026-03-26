package uk.bit1.spring_jpa.domain.tag;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.bit1.spring_jpa.domain.common.BaseEntity;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uc_tag_name", columnNames = "name")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @Getter
    private Long id;

    @Getter
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public Tag(String name) {
        this.name = normalize(name);
    }

    public void rename(String name) {
        this.name = normalize(name);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        return value.strip().toLowerCase();
    }
}
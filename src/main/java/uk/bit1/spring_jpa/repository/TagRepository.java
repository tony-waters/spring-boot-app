package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}

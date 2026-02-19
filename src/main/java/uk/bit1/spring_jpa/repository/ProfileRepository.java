package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}

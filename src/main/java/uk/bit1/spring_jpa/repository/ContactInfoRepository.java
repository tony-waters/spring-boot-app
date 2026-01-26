package uk.bit1.spring_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.bit1.spring_jpa.entity.ContactInfo;

import java.util.Optional;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    Optional<ContactInfo> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

}

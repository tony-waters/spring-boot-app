package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import uk.bit1.spring_jpa.repository.TagRepository;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TagUniqueConstraintDataJpaTest {

    @Autowired TagRepository tagRepository;

    @Test
    void tagNameMustBeUniqueInDatabase() {
        tagRepository.saveAndFlush(new Tag("urgent"));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(new Tag("  URGENT  ")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

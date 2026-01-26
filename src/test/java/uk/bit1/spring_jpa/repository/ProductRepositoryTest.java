package uk.bit1.spring_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import uk.bit1.spring_jpa.entity.Product;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired ProductRepository productRepository;

    @Test
    void findByNameIgnoreCase_findsMatchingProduct() {
        Product tea = new Product("Tea", "Yorkshire");
        em.persist(tea);
        em.flush();
        em.clear();

        assertThat(productRepository.findByNameIgnoreCase("TEA")).isPresent();
        assertThat(productRepository.findByNameIgnoreCase("tea")).isPresent();
        assertThat(productRepository.findByNameIgnoreCase("TeA")).isPresent();

        Product found = productRepository.findByNameIgnoreCase("tEa").orElseThrow();
        assertThat(found.getName()).isEqualTo("Tea");
        assertThat(found.getDescription()).isEqualTo("Yorkshire");
    }

    @Test
    void existsByNameIgnoreCase_returnsTrueWhenPresent() {
        em.persist(new Product("Biscuits", "Hobnobs"));
        em.flush();
        em.clear();

        assertThat(productRepository.existsByNameIgnoreCase("biscuits")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("BISCUITS")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("coffee")).isFalse();
    }
}

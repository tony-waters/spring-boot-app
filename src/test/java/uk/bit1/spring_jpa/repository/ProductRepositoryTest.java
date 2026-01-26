package uk.bit1.spring_jpa.repository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

    @Autowired ProductRepository productRepository;
    @Autowired TestEntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Statistics getStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        return statistics;
    }

    @Test
    void findByNameIgnoreCase_findsMatchingProduct() {
        Product tea = new Product("Tea", "Yorkshire");
        entityManager.persist(tea);
        entityManager.flush();
        entityManager.clear();

//        Statistics statistics = getStatistics();
        assertThat(productRepository.findByNameIgnoreCase("TEA")).isPresent();
//        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1); // check only one SELECT statement was issued
        assertThat(productRepository.findByNameIgnoreCase("tea")).isPresent();
        assertThat(productRepository.findByNameIgnoreCase("TeA")).isPresent();


        Product found = productRepository.findByNameIgnoreCase("tEa").orElseThrow();
        assertThat(found.getName()).isEqualTo("Tea");
        assertThat(found.getDescription()).isEqualTo("Yorkshire");
    }

    @Test
    void existsByNameIgnoreCase_returnsTrueWhenPresent() {
        entityManager.persist(new Product("Biscuits", "Hobnobs"));
        entityManager.flush();
        entityManager.clear();

        assertThat(productRepository.existsByNameIgnoreCase("biscuits")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("BISCUITS")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("coffee")).isFalse();
    }
}

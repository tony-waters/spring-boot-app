package uk.bit1.spring_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.bit1.spring_jpa.entity.ContactInfo;
import uk.bit1.spring_jpa.entity.Customer;
import uk.bit1.spring_jpa.entity.Order;
import uk.bit1.spring_jpa.entity.Product;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositorySmokeTest {

    @Autowired CustomerRepository customerRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ContactInfoRepository contactInfoRepository;

    @Test
    void canPersistAndQueryCustomerByContactInfoEmail() {
        Customer c = new Customer("Waters", "Tony");
        c.setContactInfo(new ContactInfo("tony@example.com", "07123456789"));

        customerRepository.saveAndFlush(c);

        assertThat(customerRepository.findByContactInfoEmailIgnoreCase("TONY@EXAMPLE.COM"))
                .isPresent();
    }

    @Test
    void canQueryOrdersByCustomerId() {
        Customer c = new Customer("Jones", "Belinda");
        Order o1 = new Order("One");
        Order o2 = new Order("Two");
        c.addOrder(o1);
        c.addOrder(o2);

        Customer saved = customerRepository.saveAndFlush(c);

        assertThat(orderRepository.findByCustomerId(saved.getId())).hasSize(2);
    }

    @Test
    void canQueryProductByName() {
        Product p = productRepository.saveAndFlush(new Product("Tea", "Yorkshire"));
        assertThat(productRepository.findByNameIgnoreCase("TEA")).contains(p);
    }
}

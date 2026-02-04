package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class JpaPressureTest {

    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void seed() {
        for (int c = 0; c < 200; c++) {
            Customer customer = new Customer("Last" + c, "First" + c);

            // ContactInfo
            ContactInfo ci = new ContactInfo();
            ci.setEmail("user" + c + "@example.com");
            ci.setPhone("07" + c);
            customer.setContactInfo(ci);

            // Orders
            for (int o = 0; o < 20; o++) {
                Order order = new Order("Order " + o);
                customer.addOrder(order);

                // Products
                for (int p = 0; p < 5; p++) {
                    Product prod = entityManager.persist(new Product("P" + p, "Desc" + p));
                    order.addProduct(prod);
                }
            }

            entityManager.persist(customer);
        }
        entityManager.flush();
        entityManager.clear();
    }
}

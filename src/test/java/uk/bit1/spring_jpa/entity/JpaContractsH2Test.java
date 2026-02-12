//package uk.bit1.spring_jpa.entity;
//
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import uk.bit1.spring_jpa.repository.CustomerRepository;
//import uk.bit1.spring_jpa.repository.OrderRepository;
//
//import java.time.Instant;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//class JpaContractsH2Test {
//
//    @Autowired CustomerRepository customerRepository;
//    @Autowired OrderRepository orderRepository;
//    @Autowired EntityManager em;
//
//    // ---- Helpers so your tests survive refactors ----
//
//    private Customer newCustomer(String last, String first) {
//        return new Customer(last, first);
//    }
//
//    private Order attachOrder(Customer customer, String description) {
//        // Use whatever your domain API is TODAY.
//        // If you later rename methods, fix ONLY here.
//        return customer.createOrder(description);
//        // or:
//        // Order o = new Order(customer, description);
//        // customer.addOrder(o);
//        // return o;
//    }
//
//    private void removeOrderForDeletion(Customer customer, Order order) {
//        // Again: one place to update.
//        customer.removeOrderForDeletion(order);
//        // or customer.deleteOrder(order);
//        // or customer.removeOrder(order);
//    }
//
//    // ---- Contracts ----
//
//    @Test
//    void contract_persist_and_load_customer() {
//        Customer c = newCustomer("Doe", "Jane");
//        c = customerRepository.saveAndFlush(c);
//
//        em.clear();
//
//        Customer reloaded = customerRepository.findById(c.getId()).orElseThrow();
//        assertThat(reloaded.getLastName()).isEqualTo("Doe");
//        assertThat(reloaded.getFirstName()).isEqualTo("Jane");
//    }
//
//    @Test
//    void contract_add_order_wires_both_sides() {
//        Customer c = newCustomer("Doe", "Jane");
//        Order o = attachOrder(c, "Test order");
//
//        assertThat(c.getOrders()).contains(o);
//        assertThat(o.getCustomer()).isSameAs(c);
//
//        customerRepository.saveAndFlush(c);
//        em.clear();
//
//        Customer reloaded = customerRepository.findById(c.getId()).orElseThrow();
//        assertThat(reloaded.getOrders()).hasSize(1);
//        Order reloadedOrder = reloaded.getOrders().iterator().next();
//        assertThat(reloadedOrder.getCustomer().getId()).isEqualTo(reloaded.getId());
//    }
//
//    @Test
//    void contract_move_order_between_customers() {
//        Customer a = customerRepository.saveAndFlush(newCustomer("Alpha", "Alice"));
//        Customer b = customerRepository.saveAndFlush(newCustomer("Beta", "Bob"));
//
//        Order o = attachOrder(a, "Move me");
//        customerRepository.saveAndFlush(a);
//
//        // move (generic): just attach to new customer using your domain API
//        b.addOrder(o);
//        customerRepository.saveAndFlush(b);
//
//        em.clear();
//
//        Customer aReload = customerRepository.findById(a.getId()).orElseThrow();
//        Customer bReload = customerRepository.findById(b.getId()).orElseThrow();
//
//        assertThat(aReload.getOrders()).isEmpty();
//        assertThat(bReload.getOrders()).hasSize(1);
//
//        Order moved = bReload.getOrders().iterator().next();
//        assertThat(moved.getCustomer().getId()).isEqualTo(bReload.getId());
//    }
//
//    @Test
//    void contract_orphanRemoval_deletes_child_on_remove() {
//        Customer c = newCustomer("Doe", "Janet");
//        Order o = attachOrder(c, "Delete me");
//        customerRepository.saveAndFlush(c);
//        Long orderId = o.getId();
//
//        em.clear();
//
//        Customer managed = customerRepository.findById(c.getId()).orElseThrow();
//        Order managedOrder = managed.getOrders().iterator().next();
//
//        removeOrderForDeletion(managed, managedOrder);
//        customerRepository.saveAndFlush(managed);
//
//        em.clear();
//
//        assertThat(orderRepository.findById(orderId)).isEmpty();
//    }
//
//    @Test
//    void contract_version_increments_on_update() {
//        Customer c = customerRepository.saveAndFlush(newCustomer("Doe", "Jane"));
//        Long v1 = c.getVersion();
//
//        c.setLastName("Doe-Updated");
//        customerRepository.saveAndFlush(c);
//
//        assertThat(c.getVersion()).isGreaterThan(v1);
//    }
//
//    @Test
//    void contract_timestamps_set_and_updated() {
//        Customer c = customerRepository.saveAndFlush(newCustomer("Doe", "Jane"));
//        Instant created = c.getCreatedAt();
//        Instant updated = c.getUpdatedAt();
//
//        assertThat(created).isNotNull();
//        assertThat(updated).isNotNull();
//
//        c.setFirstName("Jane2");
//        customerRepository.saveAndFlush(c);
//
//        assertThat(c.getUpdatedAt()).isAfterOrEqualTo(updated);
//    }
//}

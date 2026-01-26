package uk.bit1.spring_jpa.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrphanRemovalJpaTest {

    @PersistenceContext
    EntityManager em;

    @Test
    void orphanRemoval_removingOrderFromCustomer_deletesOrderRow() {
        // create graph
        Customer c = new Customer("Smith", "Emily");
        Order o = new Order("Orphan me");
        c.addOrder(o);

        // persist only the root; cascade persists the order
        em.persist(c);
        em.flush();

        Long orderId = o.getId();
        assertThat(orderId).isNotNull();

        // IMPORTANT: operate on managed entities
        Customer managedCustomer = em.find(Customer.class, c.getId());
        Order managedOrder = em.find(Order.class, orderId);

        // remove from the parent's collection (entity method maintains both sides)
        managedCustomer.removeOrder(managedOrder);

        em.flush();
        em.clear();

        // If orphanRemoval works, the Order row is gone
        Order deleted = em.find(Order.class, orderId);
        assertThat(deleted).isNull();
    }

    @Test
    void orphanRemoval_removingContactInfoFromCustomer_deletesContactInfoRow() {
        Customer c = new Customer("Waters", "Tony");
        ContactInfo info = new ContactInfo("tony@example.com", "07123456789");
        c.setContactInfo(info); // protected; test is in same package

        em.persist(c);
        em.flush();

        Long contactId = info.getId();
        assertThat(contactId).isNotNull();

        Customer managedCustomer = em.find(Customer.class, c.getId());
        managedCustomer.setContactInfo(null);

        em.flush();
        em.clear();

        ContactInfo deleted = em.find(ContactInfo.class, contactId);
        assertThat(deleted).isNull();
    }

    @Test
    void deletingOrder_removesJoinTableRows_order_products() {
        // Products have NO cascade from Order in your mapping, so persist them first
        Product p1 = new Product("Tea", "Yorkshire");
        Product p2 = new Product("Biscuits", "Hobnobs");
        em.persist(p1);
        em.persist(p2);

        Customer c = new Customer("Brown", "Esther");
        Order o = new Order("Shopping");
        c.addOrder(o);

        // Owning side adds products -> creates join table rows
        o.addProduct(p1);
        o.addProduct(p2);

        // Persist root; cascade persists the order
        em.persist(c);
        em.flush();

        Long orderId = o.getId();
        assertThat(orderId).isNotNull();

        // Confirm join table rows exist
        long before = ((Number) em.createNativeQuery(
                        "select count(*) from order_products where order_id = ?")
                .setParameter(1, orderId)
                .getSingleResult()).longValue();

        assertThat(before).isEqualTo(2L);

        // Trigger deletion of Order via orphanRemoval by removing it from the Customer aggregate
        Customer managedCustomer = em.find(Customer.class, c.getId());
        Order managedOrder = em.find(Order.class, orderId);
        managedCustomer.removeOrder(managedOrder);

        em.flush();
        em.clear();

        // Order row should be deleted (orphan removal)
        assertThat(em.find(Order.class, orderId)).isNull();

        // Join table rows should also be gone
        long after = ((Number) em.createNativeQuery(
                        "select count(*) from order_products where order_id = ?")
                .setParameter(1, orderId)
                .getSingleResult()).longValue();

        assertThat(after).isEqualTo(0L);
    }

    @Test
    void deletingProduct_removesJoinTableRows_order_products() {
        // Create and persist products first (no cascade from Order->Product)
        Product p1 = new Product("Tea", "Yorkshire");
        Product p2 = new Product("Biscuits", "Hobnobs");
        em.persist(p1);
        em.persist(p2);

        Customer c = new Customer("Brown", "Esther");
        Order o = new Order("Shopping");
        c.addOrder(o);

        o.addProduct(p1);
        o.addProduct(p2);

        em.persist(c);
        em.flush();

        Long orderId = o.getId();
        Long productId = p1.getId();
        assertThat(orderId).isNotNull();
        assertThat(productId).isNotNull();

        // Sanity: join rows exist for p1
        long before = ((Number) em.createNativeQuery(
                        "select count(*) from order_products where product_id = ?")
                .setParameter(1, productId)
                .getSingleResult()).longValue();
        assertThat(before).isEqualTo(1L);

        // Work with managed entities
        Order managedOrder = em.find(Order.class, orderId);
        Product managedProduct = em.find(Product.class, productId);

        // Provider-neutral: break association from owning side first
        managedOrder.removeProduct(managedProduct);

        em.flush();

        // Now delete the product
        em.remove(managedProduct);

        em.flush();
        em.clear();

        // Product row should be deleted
        assertThat(em.find(Product.class, productId)).isNull();

        // Join table rows referencing product_id should be gone
        long after = ((Number) em.createNativeQuery(
                        "select count(*) from order_products where product_id = ?")
                .setParameter(1, productId)
                .getSingleResult()).longValue();
        assertThat(after).isEqualTo(0L);
    }

}

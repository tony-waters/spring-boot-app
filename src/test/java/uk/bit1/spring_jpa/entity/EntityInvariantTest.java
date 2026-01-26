package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityInvariantTest {

    // ----------------------------
    // Customer <-> Order invariants
    // ----------------------------
    @Nested
    class CustomerOrder {

        @Test
        void addOrder_setsBothSides() {
            Customer c = new Customer("Jones", "Belinda");
            Order o = new Order("First order");

            c.addOrder(o);

            assertSame(c, o.getCustomer(), "Order.customer should be set");
            assertTrue(c.getOrders().contains(o), "Customer.orders should contain the order");
        }

        @Test
        void removeOrder_clearsBothSides() {
            Customer c = new Customer("Smith", "Emily");
            Order o = new Order("Remove me");

            c.addOrder(o);
            c.removeOrder(o);

            assertNull(o.getCustomer(), "Order.customer should be null after removal");
            assertFalse(c.getOrders().contains(o), "Customer.orders should not contain removed order");
        }

        @Test
        void setCustomer_movesOrderBetweenCustomers() {
            Customer c1 = new Customer("A", "One");
            Customer c2 = new Customer("B", "Two");
            Order o = new Order("Move me");

            c1.addOrder(o);
            assertSame(c1, o.getCustomer());
            assertTrue(c1.getOrders().contains(o));

            // protected: accessible because test is in same package
            o.setCustomer(c2);

            assertSame(c2, o.getCustomer());
            assertFalse(c1.getOrders().contains(o), "Old customer should no longer contain order");
            assertTrue(c2.getOrders().contains(o), "New customer should contain order");
        }

        @Test
        void removeAllOrders_clearsBothSides() {
            Customer c = new Customer("Brown", "Esther");
            Order o1 = new Order("One");
            Order o2 = new Order("Two");

            c.addOrder(o1);
            c.addOrder(o2);

            c.removeAllOrders();

            assertTrue(c.getOrders().isEmpty(), "Customer.orders should be empty");
            assertNull(o1.getCustomer(), "Order1.customer should be null");
            assertNull(o2.getCustomer(), "Order2.customer should be null");
        }
    }

    // ----------------------------
    // Order <-> Product invariants
    // ----------------------------
    @Nested
    class OrderProduct {

        @Test
        void addProduct_setsBothSides() {
            Order o = new Order("Shopping");
            Product p = new Product("Tea", "Yorkshire");

            o.addProduct(p);

            assertTrue(o.getProducts().contains(p), "Order.products should contain product");
            assertTrue(p.getOrders().contains(o), "Product.orders should contain order");
        }

        @Test
        void removeProduct_clearsBothSides() {
            Order o = new Order("Shopping");
            Product p = new Product("Biscuits", "Hobnobs");

            o.addProduct(p);
            o.removeProduct(p);

            assertFalse(o.getProducts().contains(p), "Order.products should not contain removed product");
            assertFalse(p.getOrders().contains(o), "Product.orders should not contain order after removal");
        }

        @Test
        void removeAllProducts_clearsBothSides() {
            Order o = new Order("Shopping");
            Product p1 = new Product("Tea", "Yorkshire");
            Product p2 = new Product("Biscuits", "Hobnobs");

            o.addProduct(p1);
            o.addProduct(p2);

            o.removeAllProducts();

            assertTrue(o.getProducts().isEmpty(), "Order.products should be empty");
            assertFalse(p1.getOrders().contains(o), "p1 should not reference the order");
            assertFalse(p2.getOrders().contains(o), "p2 should not reference the order");
        }
    }

    // -----------------------------------
    // Customer <-> ContactInfo invariants
    // -----------------------------------
    @Nested
    class CustomerContactInfo {

        @Test
        void setContactInfo_linksBothSides() {
            Customer c = new Customer("Waters", "Tony");
            ContactInfo info = new ContactInfo("tony@example.com", "07123456789");

            // protected: accessible because test is in same package
            c.setContactInfo(info);

            assertSame(info, c.getContactInfo(), "Customer.contactInfo should be set");
            assertSame(c, info.getCustomer(), "ContactInfo.customer should be set");
        }

        @Test
        void replacingContactInfo_unlinksOldOne() {
            Customer c = new Customer("Waters", "Tony");
            ContactInfo oldInfo = new ContactInfo("old@example.com", "000");
            ContactInfo newInfo = new ContactInfo("new@example.com", "111");

            c.setContactInfo(oldInfo);
            c.setContactInfo(newInfo);

            assertSame(newInfo, c.getContactInfo(), "Customer should reference new ContactInfo");
            assertSame(c, newInfo.getCustomer(), "New ContactInfo should reference customer");
            assertNull(oldInfo.getCustomer(), "Old ContactInfo should be unlinked");
        }

        @Test
        void setContactInfo_null_unlinksExisting() {
            Customer c = new Customer("Waters", "Tony");
            ContactInfo info = new ContactInfo("tony@example.com", "07123456789");

            c.setContactInfo(info);
            c.setContactInfo(null);

            assertNull(c.getContactInfo(), "Customer.contactInfo should be null");
            assertNull(info.getCustomer(), "ContactInfo.customer should be null after unlink");
        }
    }

    // ----------------------------
    // Negative / misuse / guard tests
    // ----------------------------
    @Nested
    class NegativeCases {

        @Test
        void addOrder_null_isNoOp() {
            Customer c = new Customer("X", "Y");
            c.addOrder(null);
            assertTrue(c.getOrders().isEmpty());
        }

        @Test
        void removeOrder_null_isNoOp() {
            Customer c = new Customer("X", "Y");
            c.removeOrder(null);
            assertTrue(c.getOrders().isEmpty());
        }

        @Test
        void addProduct_null_isNoOp() {
            Order o = new Order("Test");
            o.addProduct(null);
            assertTrue(o.getProducts().isEmpty());
        }

        @Test
        void removeProduct_null_isNoOp() {
            Order o = new Order("Test");
            o.removeProduct(null);
            assertTrue(o.getProducts().isEmpty());
        }

        @Test
        void addSameOrderTwice_isIdempotent() {
            Customer c = new Customer("X", "Y");
            Order o = new Order("Same");

            c.addOrder(o);
            c.addOrder(o);

            assertEquals(1, c.getOrders().size(), "Should not duplicate in Set");
            assertSame(c, o.getCustomer(), "Still linked");
        }

        @Test
        void addSameProductTwice_isIdempotent() {
            Order o = new Order("Test");
            Product p = new Product("Tea", "Yorkshire");

            o.addProduct(p);
            o.addProduct(p);

            assertEquals(1, o.getProducts().size(), "Should not duplicate in Set");
            assertTrue(p.getOrders().contains(o), "Still linked");
        }

        @Test
        void removeNonMemberOrder_isNoOp() {
            Customer c = new Customer("X", "Y");
            Order o = new Order("Not added");

            c.removeOrder(o);

            assertTrue(c.getOrders().isEmpty());
            assertNull(o.getCustomer());
        }

        @Test
        void removeNonMemberProduct_isNoOp() {
            Order o = new Order("Test");
            Product p = new Product("Not added", "N/A");

            o.removeProduct(p);

            assertTrue(o.getProducts().isEmpty());
            assertFalse(p.getOrders().contains(o));
        }

        @Test
        void setCustomer_null_detachesFromOldCustomer() {
            Customer c = new Customer("X", "Y");
            Order o = new Order("Detach");

            c.addOrder(o);
            assertSame(c, o.getCustomer());

            // protected: accessible in same package
            o.setCustomer(null);

            assertNull(o.getCustomer());
            assertFalse(c.getOrders().contains(o), "Customer should no longer contain the order");
        }

        @Test
        void publicCollectionGetters_areUnmodifiable() {
            Customer c = new Customer("X", "Y");
            Order o = new Order("Test");
            c.addOrder(o);

            Set<Order> orders = c.getOrders();
            assertThrows(UnsupportedOperationException.class, () -> orders.add(new Order("Nope")));

            Product p = new Product("Tea", "Yorkshire");
            o.addProduct(p);

            Set<Product> products = o.getProducts();
            assertThrows(UnsupportedOperationException.class, () -> products.remove(p));

            Set<Order> productOrders = p.getOrders();
            assertThrows(UnsupportedOperationException.class, productOrders::clear);
        }

        @Test
        void manyToMany_stress_addRemove_doesNotRecurse_orLeaveDanglingLinks() {
            Order o = new Order("Stress");
            Product p = new Product("Tea", "Yorkshire");

            // If recursion is accidentally reintroduced (Product calling back into Order),
            // this will typically StackOverflow very quickly.
            for (int i = 0; i < 10_000; i++) {
                o.addProduct(p);
                assertEquals(1, o.getProducts().size(), "Should remain idempotent");
                assertTrue(p.getOrders().contains(o), "Inverse side should reflect link");

                o.removeProduct(p);
                assertTrue(o.getProducts().isEmpty(), "Owning side should be empty after remove");
                assertFalse(p.getOrders().contains(o), "Inverse side should be cleared after remove");
            }
        }

    }
}

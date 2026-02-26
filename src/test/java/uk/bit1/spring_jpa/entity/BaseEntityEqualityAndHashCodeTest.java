package uk.bit1.spring_jpa.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class BaseEntityEqualityAndHashCodeTest {

    @Test
    void transientEntitiesAreNotEqual() {
        Customer a = new Customer("tonyW");
        Customer b = new Customer("tonyW");

        // both ids null => should NOT be equal
        assertThat(a).isNotEqualTo(b);
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void entitiesWithSameClassAndSameIdAreEqual() throws Exception {
        Customer a = new Customer("tonyW");
        Customer b = new Customer("johnS");

        setId(a, 42L);
        setId(b, 42L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void entitiesWithDifferentClassSameIdAreNotEqual() throws Exception {
        Customer c = new Customer("tonyW");
        Tag t = new Tag("urgent");

        setId(c, 7L);
        setId(t, 7L);

        assertThat(c).isNotEqualTo(t);
    }

    @Test
    void hashCodeMustBeStableAcrossIdAssignmentBecauseWeUseSets() throws Exception {
        Customer c = new Customer("tonyW");

        int before = c.hashCode();

        // simulate persistence assigning an id
        setId(c, 123L);

        int after = c.hashCode();

        // If this fails, Sets/Maps can break after persistence.
        assertThat(after).isEqualTo(before);
    }

    @Test
    void setMembershipMustStillWorkAfterIdAssignment() throws Exception {
        Customer c = new Customer("tonyW");
        Set<Customer> set = new HashSet<>();
        set.add(c);

        // simulate persistence assigning id
        setId(c, 123L);

        // If hashCode changes, contains/remove may fail.
        assertThat(set).contains(c);
        assertThat(set.remove(c)).isTrue();
        assertThat(set).doesNotContain(c);
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }
}

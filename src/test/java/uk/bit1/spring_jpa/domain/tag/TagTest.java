package uk.bit1.spring_jpa.domain.tag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {

    @Test
    void constructor_normalizes_name() {
        Tag tag = new Tag("  UrGent ");

        assertThat(tag.getName()).isEqualTo("urgent");
    }

    @Test
    void constructor_rejects_null_name() {
        assertThatThrownBy(() -> new Tag(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag name must not be blank");
    }

    @Test
    void constructor_rejects_blank_name() {
        assertThatThrownBy(() -> new Tag("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag name must not be blank");
    }

    @Test
    void rename_normalizes_name() {
        Tag tag = new Tag("bug");

        tag.rename("  Support ");

        assertThat(tag.getName()).isEqualTo("support");
    }

    @Test
    void rename_rejects_null_name() {
        Tag tag = new Tag("bug");

        assertThatThrownBy(() -> tag.rename(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag name must not be blank");
    }

    @Test
    void rename_rejects_blank_name() {
        Tag tag = new Tag("bug");

        assertThatThrownBy(() -> tag.rename("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag name must not be blank");
    }
}
package uk.bit1.spring_jpa.web.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.bit1.spring_jpa.application.customer.command.CustomerCommandService;
import uk.bit1.spring_jpa.application.customer.query.CustomerNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerCommandController.class)
@Import(RestExceptionHandler.class)
class CustomerCommandControllerWebMvcTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private CustomerCommandService customerCommandService;

    @Test
    void create_customer_returns_201_and_location_header() throws Exception {
        given(customerCommandService.createCustomer(any())).willReturn(42L);

        mockMvc.perform(post("/api/customers")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Tony"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/customers/42"));
    }

    @Test
    void create_customer_rejects_blank_display_name() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.displayName").exists());
    }

    @Test
    void change_display_name_returns_204() throws Exception {
        mockMvc.perform(put("/api/customers/1/display-name")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Anthony"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_profile_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/profile")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "emailAddress": "tony@example.com",
                                  "marketingOptIn": true
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_profile_rejects_invalid_email() throws Exception {
        mockMvc.perform(post("/api/customers/1/profile")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "emailAddress": "not-an-email",
                                  "marketingOptIn": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.emailAddress").exists());
    }

    @Test
    void change_profile_email_returns_204() throws Exception {
        mockMvc.perform(put("/api/customers/1/profile/email")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "emailAddress": "anthony@example.com"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void raise_ticket_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "This is a valid ticket"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void raise_ticket_rejects_short_description() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void change_ticket_description_returns_204() throws Exception {
        mockMvc.perform(put("/api/customers/1/tickets/10/description")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "This is another valid description"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void start_ticket_work_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/start-work"))
                .andExpect(status().isNoContent());
    }

    @Test
    void resolve_ticket_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/resolve"))
                .andExpect(status().isNoContent());
    }

    @Test
    void reopen_ticket_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/reopen"))
                .andExpect(status().isNoContent());
    }

    @Test
    void close_ticket_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/close"))
                .andExpect(status().isNoContent());
    }

    @Test
    void add_tag_to_ticket_returns_204() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/tags")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "tagName": "bug"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void add_tag_to_ticket_rejects_blank_tag_name() throws Exception {
        mockMvc.perform(post("/api/customers/1/tickets/10/tags")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "tagName": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.tagName").exists());
    }

    @Test
    void remove_tag_from_ticket_returns_204() throws Exception {
        mockMvc.perform(delete("/api/customers/1/tickets/10/tags/bug"))
                .andExpect(status().isNoContent());
    }

    @Test
    void remove_ticket_returns_204() throws Exception {
        mockMvc.perform(delete("/api/customers/1/tickets/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returns_409_when_service_throws_illegal_state() throws Exception {
        org.mockito.BDDMockito.willThrow(new IllegalStateException("Customer already has a profile"))
                .given(customerCommandService)
                .createProfile(any());

        mockMvc.perform(post("/api/customers/1/profile")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "emailAddress": "tony@example.com",
                                  "marketingOptIn": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Customer already has a profile"));
    }

    @Test
    void returns_404_when_customer_missing() throws Exception {
        org.mockito.BDDMockito.willThrow(new CustomerNotFoundException(999L))
                .given(customerCommandService)
                .changeDisplayName(any());

        mockMvc.perform(put("/api/customers/999/display-name")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Anthony"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
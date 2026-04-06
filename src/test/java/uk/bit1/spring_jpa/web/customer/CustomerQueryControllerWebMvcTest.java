package uk.bit1.spring_jpa.web.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.bit1.spring_jpa.application.customer.query.CustomerDetailView;
import uk.bit1.spring_jpa.application.customer.query.CustomerNotFoundException;
import uk.bit1.spring_jpa.application.customer.query.CustomerQueryService;
import uk.bit1.spring_jpa.application.customer.query.CustomerSummaryView;
import uk.bit1.spring_jpa.application.customer.query.TicketDetailView;
import uk.bit1.spring_jpa.application.customer.query.TicketListItemView;
import uk.bit1.spring_jpa.domain.customer.TicketStatus;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerQueryController.class)
@Import(RestExceptionHandler.class)
class CustomerQueryControllerWebMvcTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private CustomerQueryService customerQueryService;

    @Test
    void find_customers_returns_page() throws Exception {
        var page = new PageImpl<>(
                List.of(
                        new CustomerSummaryView(1L, "Alice"),
                        new CustomerSummaryView(2L, "Bob")
                ),
                PageRequest.of(0, 2),
                3
        );

        given(customerQueryService.findCustomers(eq(null), any()))
                .willReturn(page);

        mockMvc.perform(get("/api/customers?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].displayName").value("Alice"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].displayName").value("Bob"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void find_customers_passes_name_filter() throws Exception {
        var page = new PageImpl<>(
                List.of(new CustomerSummaryView(1L, "Tony")),
                PageRequest.of(0, 10),
                1
        );

        given(customerQueryService.findCustomers(eq("ton"), any()))
                .willReturn(page);

        mockMvc.perform(get("/api/customers?name=ton&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayName").value("Tony"));
    }

    @Test
    void find_customer_returns_detail() throws Exception {
        given(customerQueryService.findCustomerDetail(1L))
                .willReturn(new CustomerDetailView(
                        1L,
                        0L,
                        "Tony",
                        "tony@example.com",
                        true
                ));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.displayName").value("Tony"))
                .andExpect(jsonPath("$.emailAddress").value("tony@example.com"))
                .andExpect(jsonPath("$.marketingOptIn").value(true));
    }

    @Test
    void find_tickets_returns_unfiltered_list() throws Exception {
        given(customerQueryService.findTicketsForCustomer(1L, null, null))
                .willReturn(List.of(
                        new TicketListItemView(10L, "First valid ticket", TicketStatus.OPEN),
                        new TicketListItemView(11L, "Second valid ticket", TicketStatus.RESOLVED)
                ));

        mockMvc.perform(get("/api/customers/1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].status").value("RESOLVED"));
    }

    @Test
    void find_tickets_filters_by_status() throws Exception {
        given(customerQueryService.findTicketsForCustomer(1L, TicketStatus.OPEN, null))
                .willReturn(List.of(
                        new TicketListItemView(10L, "First valid ticket", TicketStatus.OPEN)
                ));

        mockMvc.perform(get("/api/customers/1/tickets?status=OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void find_tickets_filters_by_tag() throws Exception {
        given(customerQueryService.findTicketsForCustomer(1L, null, "bug"))
                .willReturn(List.of(
                        new TicketListItemView(10L, "First valid ticket", TicketStatus.OPEN)
                ));

        mockMvc.perform(get("/api/customers/1/tickets?tag=bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].description").value("First valid ticket"));
    }

    @Test
    void find_ticket_returns_detail() throws Exception {
        given(customerQueryService.findTicketDetail(1L, 10L))
                .willReturn(new TicketDetailView(
                        10L,
                        "First valid ticket",
                        TicketStatus.OPEN,
                        List.of("bug", "urgent")
                ));

        mockMvc.perform(get("/api/customers/1/tickets/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("First valid ticket"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.tagNames[0]").value("bug"))
                .andExpect(jsonPath("$.tagNames[1]").value("urgent"));
    }

    @Test
    void find_customer_returns_not_found_when_customer_missing() throws Exception {
        given(customerQueryService.findCustomerDetail(999L))
                .willThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }
}
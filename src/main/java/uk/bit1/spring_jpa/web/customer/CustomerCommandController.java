package uk.bit1.spring_jpa.web.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bit1.spring_jpa.application.customer.command.*;
import uk.bit1.spring_jpa.application.customer.command.commands.*;
import uk.bit1.spring_jpa.web.customer.dto.*;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
class CustomerCommandController {

    private final CustomerCommandService customerCommandService;

    @PostMapping
    ResponseEntity<Void> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Long customerId = customerCommandService.createCustomer(
                new CreateCustomerCommand(request.displayName())
        );

        return ResponseEntity.created(URI.create("/api/customers/" + customerId)).build();
    }

    @PutMapping("/{customerId}/display-name")
    ResponseEntity<Void> changeDisplayName(
            @PathVariable Long customerId,
            @Valid @RequestBody ChangeDisplayNameRequest request
    ) {
        customerCommandService.changeDisplayName(
                new ChangeDisplayNameCommand(customerId, request.displayName())
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/profile")
    ResponseEntity<Void> createProfile(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateProfileRequest request
    ) {
        customerCommandService.createProfile(
                new CreateProfileCommand(customerId, request.emailAddress(), request.marketingOptIn())
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}/profile/email")
    ResponseEntity<Void> changeProfileEmail(
            @PathVariable Long customerId,
            @Valid @RequestBody ChangeProfileEmailRequest request
    ) {
        customerCommandService.changeProfileEmail(
                new ChangeProfileEmailCommand(customerId, request.emailAddress())
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets")
    ResponseEntity<Void> raiseTicket(
            @PathVariable Long customerId,
            @Valid @RequestBody RaiseTicketRequest request
    ) {
        customerCommandService.raiseTicket(
                new RaiseTicketCommand(customerId, request.description())
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}/tickets/{ticketId}/description")
    ResponseEntity<Void> changeTicketDescription(
            @PathVariable Long customerId,
            @PathVariable Long ticketId,
            @Valid @RequestBody ChangeTicketDescriptionRequest request
    ) {
        customerCommandService.changeTicketDescription(
                new ChangeTicketDescriptionCommand(customerId, ticketId, request.description())
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets/{ticketId}/start-work")
    ResponseEntity<Void> startTicketWork(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        customerCommandService.startTicketWork(
                new StartTicketWorkCommand(customerId, ticketId)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets/{ticketId}/resolve")
    ResponseEntity<Void> resolveTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        customerCommandService.resolveTicket(
                new ResolveTicketCommand(customerId, ticketId)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets/{ticketId}/reopen")
    ResponseEntity<Void> reopenTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        customerCommandService.reopenTicket(
                new ReopenTicketCommand(customerId, ticketId)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets/{ticketId}/close")
    ResponseEntity<Void> closeTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        customerCommandService.closeTicket(
                new CloseTicketCommand(customerId, ticketId)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/tickets/{ticketId}/tags")
    ResponseEntity<Void> addTagToTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId,
            @Valid @RequestBody AddTagToTicketRequest request
    ) {
        customerCommandService.addTagToTicket(
                new AddTagToTicketCommand(customerId, ticketId, request.tagName())
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{customerId}/tickets/{ticketId}/tags/{tagName}")
    ResponseEntity<Void> removeTagFromTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId,
            @PathVariable String tagName
    ) {
        customerCommandService.removeTagFromTicket(
                new RemoveTagFromTicketCommand(customerId, ticketId, tagName)
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{customerId}/tickets/{ticketId}")
    ResponseEntity<Void> removeTicket(
            @PathVariable Long customerId,
            @PathVariable Long ticketId
    ) {
        customerCommandService.removeTicket(
                new RemoveTicketCommand(customerId, ticketId)
        );
        return ResponseEntity.noContent().build();
    }
}
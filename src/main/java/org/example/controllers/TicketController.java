package org.example.controllers;


import org.apache.coyote.Response;
import org.example.dto.*;
import org.example.models.*;
import org.example.repositories.TicketAssignmentHistoryRepository;
import org.example.repositories.UserRepository;
import org.example.services.ChatMessageService;
import org.example.services.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    TicketService ticketService;
    private final ChatMessageService chatService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    public TicketController(
            TicketService ticketService,
            ChatMessageService chatService,
            UserRepository userRepository
    ){
        this.chatService = chatService;
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TicketResponse> getAll(
            @RequestParam(required = false) TicketStatus status,
            Authentication authentication
    ) {
        log.info("getAll method for the user: {}", authentication.getName());

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User Not found"));

        if(user.getRole() == UserRole.CUSTOMER) {

            List<TicketResponse> customersTickets = ticketService.getByCustomerUser(user.getId());

            if(status  != null) {
                  customersTickets = customersTickets
                          .stream()
                          .filter(ticketResponse -> ticketResponse.status().equals(status))
                          .toList();
            }

            return customersTickets;
        }

        else if(user.getRole() == UserRole.AGENT) {

            List<TicketResponse> agentsTickets = ticketService.getByAgentUser(user.getId());

            if(status != null) {
                agentsTickets = agentsTickets
                        .stream()
                        .filter(ticketResponse -> ticketResponse.status().equals(status))
                        .toList();
            }

            return agentsTickets;
        }

        else if (user.getRole() == UserRole.ADMIN) {

            if(status != null) {
                return ticketService.getByStatus(status);
            }
            return ticketService.getAll();
        }

        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("Tickets accessed by user: {}", authentication.getName());

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User role: {}", user.getRole());

        TicketResponse ticket = ticketService.getById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (user.getRole() == UserRole.CUSTOMER) {
            if(!ticketService.isTicketOwner(ticket.id(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }
        else if (user.getRole() == UserRole.AGENT) {
            if(!ticketService.isAssignedAgent(ticket.id(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCESS DENIED");
            }
        }


        return ticket;
    }

    @PostMapping
    public Ticket createTicket(@RequestBody CreateTicketResponse request){

        return ticketService.createTicket(
                request.customerId(),
                request.title(),
                request.description(),
                request.priority(),
                request.category()
        );
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if(user.getRole() == UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        else if(user.getRole() == UserRole.AGENT) {
            if (!ticketService.isAssignedAgent(id, user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
            }
        }
        return ticketService.updateStatus(id, request.status(), user.getId());
    }

    @PatchMapping("/{id}/assign")
    public TicketResponse assignAgent(
            @PathVariable Long id,
            @RequestBody AssignAgentRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.AGENT) {
            if(!ticketService.isAssignedAgent(id, user.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "One can change only its own tickets!");
        }

        return ticketService.assingAgent(id, request.agentId(), user.getId());
    }

    @GetMapping("/{id}/assigned-history")
    public List<TicketAssignmentHistoryResponse> getAssignedHistory(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.CUSTOMER) {
            if (!ticketService.isTicketOwner(id, user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not the owner of the ticket!");
            }
        }
        else if(user.getRole() == UserRole.AGENT) {
            if (!ticketService.isAssignedAgent(id, user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the currently assigned agent!");
            }
        }
        return ticketService.getAssignmentHistory(id);
    }

    @GetMapping("/agent/{agentId}")
    public List<TicketResponse> getByAgentId(
            @PathVariable Long agentId
    ) {
        return ticketService.getByAgentId(agentId);
    }

    @GetMapping("/customer/{customerId}")
    public List<TicketResponse> getByCustomerId(
            @PathVariable Long customerId
    ) {
        return ticketService.getByCustomerId(customerId);
    }

    @GetMapping("/{id}/history")
    public List<TicketStatusHistoryResponse> getStatusHistory(
            @PathVariable Long id
    ) {
        return ticketService.getHistory(id);
    }

    @PostMapping("/{id}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(
                id,
                request.senderUserId(),
                request.message()
        );
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> getMessages(
            @PathVariable Long id
    ) {
        return chatService.getMessages(id);
    }

    @PatchMapping("/{ticketId}/messages/{messageId}/read")
    public ChatMessageResponse changeRead(
            @PathVariable Long ticketId,
            @PathVariable Long messageId
    ) {
        return chatService.markAsRead(ticketId, messageId);
    }

    @GetMapping("/stats")
    public TicketStatsResponse getTicketStats() {
        return ticketService.getTicketStats();
    }

    private record SendMessageRequest(
            Long senderUserId,
            String message
    ) {
    }

    private record AssignAgentRequest(
            Long agentId
    ){}
    private record CreateTicketResponse(
            Long customerId,
            String title,
            String description,
            TicketPriority priority,
            String category
    ){}

    private record UpdateStatusRequest(
            TicketStatus status
    ) {}
}

package org.example.controllers;


import org.example.dto.ChatMessageResponse;
import org.example.dto.TicketResponse;
import org.example.dto.TicketStatusHistoryResponse;
import org.example.models.Ticket;
import org.example.models.TicketPriority;
import org.example.models.TicketStatus;
import org.example.models.TicketStatusHistory;
import org.example.repositories.TicketStatusHistoryRepository;
import org.example.services.ChatMessageService;
import org.example.services.TicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    TicketService ticketService;
    private final ChatMessageService chatService;

    public TicketController(
            TicketService ticketService,
            ChatMessageService chatService
    ){
        this.chatService = chatService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getAll(
            @RequestParam(required = false) TicketStatus status
    ) {
        if (status != null) {
            return ticketService.getByStatus(status);
        }
        return ticketService.getAll();
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
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
            @RequestBody UpdateStatusRequest request
    ) {
        return ticketService.updateStatus(id, request.status(), request.changedByUserId());
    }

    @PatchMapping("/{id}/assign")
    public TicketResponse assignAgent(
            @PathVariable Long id,
            @RequestBody AssignAgentRequest request
    ) {
        return ticketService.assingAgent(id, request.agentId());
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
            TicketStatus status,
            Long changedByUserId
    ) {}
}

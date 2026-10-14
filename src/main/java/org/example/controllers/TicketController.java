package org.example.controllers;


import org.example.dto.TicketResponse;
import org.example.models.Ticket;
import org.example.models.TicketPriority;
import org.example.models.TicketStatus;
import org.example.models.TicketStatusHistory;
import org.example.repositories.TicketStatusHistoryRepository;
import org.example.services.TicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    TicketService ticketService;

    public TicketController(TicketService ticketService ){
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getAll() {
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

package org.example.controllers;


import org.example.models.Ticket;
import org.example.models.TicketPriority;
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
    public List<Ticket> getAll() {
        return ticketService.getAll();
    }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id) {
        return ticketService.getById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    @PostMapping("/create")
    public Ticket createTicket(@RequestBody CreateTicketResponse request){

        return ticketService.createTicket(
                request.customerId(),
                request.title(),
                request.description(),
                request.priority(),
                request.category()
        );
    }

    private record CreateTicketResponse(
            Long customerId,
            String title,
            String description,
            TicketPriority priority,
            String category
    ){}
}

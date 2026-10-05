package org.example.services;


import org.example.models.*;
import org.example.repositories.AgentRepository;
import org.example.repositories.CustomerRepository;
import org.example.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
//    private final AgentRepository agentRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository
//            AgentRepository agentRepository
            ) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
//        this.agentRepository = agentRepository;
    }

    public List<Ticket> getAll() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getById(Long id) {
        return ticketRepository.findById(id);
    }

    public Ticket createTicket(
            Long customerId,
            String title,
            String description,
            TicketPriority priority,
            String category
    ) {

        Ticket ticket = new Ticket();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setAssignedAgent(null);
        ticket.setCustomer(customer);

        return ticketRepository.save(ticket);
    }
}

package org.example.services;


import org.example.dto.TicketResponse;
import org.example.models.*;
import org.example.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            TicketStatusHistoryRepository ticketStatusHistoryRepository,
            UserRepository userRepository
            ) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll() {

        return ticketRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<TicketResponse> getById(Long id) {
        return ticketRepository.findById(id).map(this::toResponse);
    }


    @Transactional
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

    @Transactional
    public TicketResponse updateStatus(Long ticketId, TicketStatus status, Long changeByUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);

        User user = userRepository.findById(changeByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketStatus oldStatus = ticket.getStatus();

        if (oldStatus == status)
            return toResponse(ticket);

        ticket.setStatus(status);

        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicket(ticket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(status);
        history.setChangedByUser(user);

        ticketStatusHistoryRepository.save(history);

        Ticket saved = ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCustomer().getId(),
                ticket.getAssignedAgent() != null
                        ? ticket.getAssignedAgent().getId()
                        : null,
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }
}

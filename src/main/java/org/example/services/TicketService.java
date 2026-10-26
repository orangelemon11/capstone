package org.example.services;


import org.example.dto.TicketResponse;
import org.example.dto.TicketStatusHistoryResponse;
import org.example.models.*;
import org.example.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.example.models.Agent;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final UserRepository userRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            TicketStatusHistoryRepository ticketStatusHistoryRepository,
            UserRepository userRepository,
            AgentRepository agentRepository
            ) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
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

        TicketStatus oldStatus = ticket.getStatus();

        ticket.setStatus(status);

        User user = userRepository.findById(changeByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));



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

        return toResponse(saved);
    }

    @Transactional
    public TicketResponse assingAgent(Long ticketId, Long agentId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket nout found"));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow( ()-> new RuntimeException("Agent not found"));

        ticket.setAssignedAgent(agent);

        Ticket saved = ticketRepository.save(ticket);

        return toResponse(ticket);

    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByCustomerId(Long customerId) {
        return ticketRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByAgentId(Long agentId) {
        return ticketRepository.findByAssignedAgentId(agentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketStatusHistoryResponse> getHistory(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Ticket history not found");
        }

        return ticketStatusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
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

    private TicketStatusHistoryResponse toHistoryResponse(
            TicketStatusHistory history
    ) {
        return new TicketStatusHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedByUser().getId(),
                history.getComment(),
                history.getChangedAt()
        );
    }
}

package org.example.services;


import org.example.dto.TicketAssignmentHistoryResponse;
import org.example.dto.TicketResponse;
import org.example.dto.TicketStatsResponse;
import org.example.dto.TicketStatusHistoryResponse;
import org.example.models.*;
import org.example.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final NotificationRepository notificationRepository;
    public final TicketAssignmentHistoryRepository ticketAssignmentHistoryRepository;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            TicketStatusHistoryRepository ticketStatusHistoryRepository,
            UserRepository userRepository,
            AgentRepository agentRepository,
            NotificationRepository notificationRepository,
            TicketAssignmentHistoryRepository ticketAssignmentHistoryRepository
            ) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.agentRepository = agentRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.ticketAssignmentHistoryRepository = ticketAssignmentHistoryRepository;
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

        User user = userRepository.findById(changeByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (oldStatus == status)
            return toResponse(ticket);

        Notification notif = new Notification();

        notif.setTicket(ticket);
        notif.setMessage(String.format("New notification for the ticket id: "+ ticketId));
        notif.setTitle("Status changed");
        notif.setUser(ticket.getCustomer().getUser());
        notif.setRead(false);
        notif.setType(NotificationType.STATUS_CHANGED);

        notificationRepository.save(notif);

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
    public TicketResponse assingAgent(Long ticketId, Long agentId, Long changedByUserId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket nout found"));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow( ()-> new RuntimeException("Agent not found"));

        Agent currentAgent = ticket.getAssignedAgent();

        User agentUser = agent.getUser();

        if(currentAgent != null && currentAgent.getId().equals(agentId)){
            return toResponse(ticket); //nothing changed
        }

        User changedByUser = userRepository.findById(changedByUserId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        TicketAssignmentHistory assignmentHistory = new TicketAssignmentHistory();
        assignmentHistory.setTicket(ticket);
        assignmentHistory.setPreviousAgent(currentAgent);
        assignmentHistory.setNewAgent(agent);
        assignmentHistory.setChangedBy(changedByUser);
        assignmentHistory.setChangedAt(LocalDateTime.now());
        ticketAssignmentHistoryRepository.save(assignmentHistory);

        Notification noti = new Notification();
        noti.setTicket(ticket);
        noti.setType(NotificationType.TICKET_ASSIGNED);
        noti.setRead(false);
        noti.setTitle("You were assigned to this ticket");
        noti.setUser(agentUser);
        noti.setMessage("You were assigned to some new ticket");
        notificationRepository.save(noti);



        ticket.setAssignedAgent(agent);

        Ticket saved = ticketRepository.save(ticket);

        return toResponse(saved);

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

    public boolean isTicketOwner(Long ticketId, Long userId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found!"));

        return ticket.getCustomer().getUser().getId().equals(userId);
    }

    public boolean isAssignedAgent(Long ticketId, Long userId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found!"));

        if (ticket.getAssignedAgent() == null)
            return false;

        return ticket.getAssignedAgent().getUser().getId().equals(userId);
    }

    @Transactional(readOnly = true)
    public TicketStatsResponse getTicketStats() {

        TicketStatsResponse response = new TicketStatsResponse(
                ticketRepository.count(),
                ticketRepository.countByStatus(TicketStatus.OPEN),
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS),
                ticketRepository.countByStatus(TicketStatus.WAITING_FOR_CUSTOMER),
                ticketRepository.countByStatus(TicketStatus.RESOLVED),
                ticketRepository.countByStatus(TicketStatus.CLOSED)

        );

        return response;
    }

    public List<TicketResponse> getByCustomerUser(Long userId) {

        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getCustomer().getUser().getId().equals(userId))
                .map(this::toResponse)
                .toList();

    }

    public List<TicketResponse> getByAgentUser(Long userId) {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> ticket.getAssignedAgent() != null)
                .filter(ticket -> ticket.getAssignedAgent().getUser().getId().equals(userId))
                .map(this::toResponse)
                .toList();
    }

    public List<TicketAssignmentHistoryResponse> getAssignmentHistory(Long ticketId) {
        return ticketAssignmentHistoryRepository.findByTicket_Id(ticketId)
                .stream()
                .map(this::toAssignmentHistoryResponse)
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

    private TicketAssignmentHistoryResponse toAssignmentHistoryResponse(
            TicketAssignmentHistory history
    ) {
        return new TicketAssignmentHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getPreviousAgent() != null
                        ? history.getPreviousAgent().getId()
                        : null,
                history.getNewAgent().getId(),
                history.getChangedBy().getId(),
                history.getChangedAt()
        );
    }
}

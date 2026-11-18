package org.example.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_assignment_history")
public class TicketAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_agent_id")
    private Agent previousAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_agent_id", nullable = false)
    private Agent newAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public Long getId() {
        return id;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public Agent getNewAgent() {
        return newAgent;
    }

    public void setNewAgent(Agent newAgent) {
        this.newAgent = newAgent;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Agent getPreviousAgent() {
        return previousAgent;
    }

    public void setPreviousAgent(Agent previousAgent) {
        this.previousAgent = previousAgent;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
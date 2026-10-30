package org.example.services;


import org.example.dto.ChatMessageResponse;
import org.example.models.*;
import org.example.repositories.ChatMessageRepository;
import org.example.repositories.NotificationRepository;
import org.example.repositories.TicketRepository;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public ChatMessageResponse sendMessage(
            Long ticketId,
            Long senderUserId,
            String message
    ) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()->new RuntimeException("Ticket not found"));

        User user = userRepository.findById(senderUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is blank");
        }

        if(ticket.getAssignedAgent() != null) {
            Notification notif = new Notification();

            Long agentUserId = ticket.getAssignedAgent().getUser().getId();
            User agentUser = ticket.getAssignedAgent().getUser();

            Long customerUserId = ticket.getCustomer().getUser().getId();
            User customerUser = ticket.getCustomer().getUser();

            if(senderUserId.equals(agentUserId)) {
                notif.setUser(customerUser);
            } else if (senderUserId.equals(customerUserId)) {
                notif.setUser(agentUser);
            } else {
                throw new RuntimeException("Wrong sender id");
            }

            notif.setTitle(String.format("new update on the ticket with id: #" + ticketId));
            notif.setMessage(message);
            notif.setType(NotificationType.NEW_MESSAGE);
            notif.setTicket(ticket);
            notif.setRead(false);

            notificationRepository.save(notif);
        }

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setTicket(ticket);
        chatMessage.setSenderUser(user);
        chatMessage.setMessage(message);
        chatMessage.setRead(false);

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Ticket not found");
        }

        return chatMessageRepository.findByTicketIdOrderBySentAtAsc(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse markAsRead(Long ticketId, Long messageId) {

        if(!ticketRepository.existsById(ticketId) || !chatMessageRepository.existsById(messageId)) {
            throw new RuntimeException("Message not found");
        }

        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Chat message not found"));

        Ticket ticket = chatMessage.getTicket();

        logger.info("Ticket id z ticket.getId {}", ticket.getId());
        logger.info("Ticket id z url {}", ticketId);

        if (!ticket.getId().equals(ticketId)) {
            logger.error("Wrong ticket id");
            throw new RuntimeException("Wrong ticket id");
        }

        chatMessage.setRead(true);

        var saved = chatMessageRepository.save(chatMessage);

        logger.info("read status fo the message with id {}, changed to true", messageId);

        return toResponse(saved);

    }

    private ChatMessageResponse toResponse(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getTicket().getId(),
                chatMessage.getSenderUser().getId(),
                chatMessage.getMessage(),
                chatMessage.getSentAt(),
                chatMessage.getRead()
        );
    }
}

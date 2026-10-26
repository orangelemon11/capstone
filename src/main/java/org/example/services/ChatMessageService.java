package org.example.services;


import org.example.dto.ChatMessageResponse;
import org.example.models.ChatMessage;
import org.example.models.Ticket;
import org.example.models.User;
import org.example.repositories.ChatMessageRepository;
import org.example.repositories.TicketRepository;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
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

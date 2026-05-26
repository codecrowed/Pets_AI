package jiangxiaopeng.ai.conversation.application.service;

import jiangxiaopeng.ai.conversation.application.command.CreateChatCommand;
import jiangxiaopeng.ai.conversation.application.dto.*;
import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;

@Service
@Transactional
public class ChatApplicationService {

    private final ChatSessionRepository chatSessionRepository;

    public ChatApplicationService(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    public ChatSummaryDto createChat(CreateChatCommand command) {
        ChatSession session = ChatSession.create(
                new UserId(command.userId()), command.title(), command.model());
        session = chatSessionRepository.save(session);
        return new ChatSummaryDto(session.getUid().value(), session.getTitle(), session.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public ChatListResponse listChats(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ChatSession> sessionPage = chatSessionRepository.findByUserIdAndStatus(userId, "ACTIVE", pageable);

        List<ChatSummaryDto> summaries = sessionPage.getContent().stream()
                .map(s -> new ChatSummaryDto(s.getUid().value(), s.getTitle(), s.getUpdatedAt()))
                .toList();

        List<ChatListResponse.ChatGroup> groups = groupByDate(summaries);

        PageInfo pageInfo = new PageInfo(page, size,
                sessionPage.getTotalElements(), sessionPage.getTotalPages(), sessionPage.hasNext());

        return new ChatListResponse(groups, pageInfo);
    }

    @Transactional(readOnly = true)
    public ChatSummaryDto getChat(String chatId, Long userId) {
        ChatSession session = chatSessionRepository.findByUid(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(new UserId(userId));
        return new ChatSummaryDto(session.getUid().value(), session.getTitle(), session.getUpdatedAt());
    }

    public ChatSummaryDto updateChat(String chatId, Long userId, String title, String model) {
        ChatSession session = chatSessionRepository.findByUid(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(new UserId(userId));
        session.updateTitle(title);
        session.updateModel(model);
        session = chatSessionRepository.save(session);
        return new ChatSummaryDto(session.getUid().value(), session.getTitle(), session.getUpdatedAt());
    }

    public void deleteChat(String chatId, Long userId) {
        ChatSession session = chatSessionRepository.findByUid(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(new UserId(userId));
        session.softDelete();
        chatSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public ChatListResponse searchChats(Long userId, String keyword, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ChatSession> sessionPage = chatSessionRepository.searchByUserIdAndKeyword(userId, keyword, pageable);

        List<ChatSummaryDto> summaries = sessionPage.getContent().stream()
                .map(s -> new ChatSummaryDto(s.getUid().value(), s.getTitle(), s.getUpdatedAt()))
                .toList();

        List<ChatListResponse.ChatGroup> groups = groupByDate(summaries);
        PageInfo pageInfo = new PageInfo(page, size,
                sessionPage.getTotalElements(), sessionPage.getTotalPages(), sessionPage.hasNext());

        return new ChatListResponse(groups, pageInfo);
    }

    private List<ChatListResponse.ChatGroup> groupByDate(List<ChatSummaryDto> summaries) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate sevenDaysAgo = today.minusDays(7);

        Map<String, List<ChatSummaryDto>> grouped = new LinkedHashMap<>();

        for (ChatSummaryDto summary : summaries) {
            LocalDate date = summary.updatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            String label;
            if (date.equals(today)) {
                label = "今天";
            } else if (date.equals(yesterday)) {
                label = "昨天";
            } else if (date.isAfter(sevenDaysAgo)) {
                label = "近7天";
            } else {
                label = date.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA));
            }
            grouped.computeIfAbsent(label, k -> new ArrayList<>()).add(summary);
        }

        return grouped.entrySet().stream()
                .map(e -> new ChatListResponse.ChatGroup(e.getKey(), e.getValue()))
                .toList();
    }
}

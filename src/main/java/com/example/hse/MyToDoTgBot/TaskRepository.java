package com.example.hse.MyToDoTgBot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByChatIdAndCompletedFalseOrderByNotifyDateAsc(Long chatId);
    List<Task> findByChatIdAndCompletedFalseOrderByNotifyDateDesc(Long chatId);
    List<Task> findByNotifyDateBeforeAndCompletedFalse(LocalDateTime date);
    Optional<Task> findByIdAndChatId(Long id, Long chatId);

}

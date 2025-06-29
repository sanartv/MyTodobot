package com.example.hse.MyToDoTgBot;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final TaskService taskService;
    private final ToDoBot toDoBot;

    @Scheduled(fixedRate = 60000)
    public void checkNotifications() {
        List<Task> tasks = taskService.getTasksForNotification(LocalDateTime.now());
        for (Task task : tasks) {
            String message = " Напоминание!\n" +
                     task.getTitle() + "\n" +
                    " Сейчас: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            if (task.getComment() != null && !task.getComment().isEmpty()) {
                message += "\n" + task.getComment();
            }

            toDoBot.sendMessage(task.getChatId(), message);
            taskService.markAsCompleted(task.getId(), task.getChatId());
        }
    }
}

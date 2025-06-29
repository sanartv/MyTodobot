package com.example.hse.MyToDoTgBot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository repository;

    public Task createTask(Long chatId, String title, LocalDateTime notifyDate, String comment) {
        Task task = new Task(null,chatId,title,notifyDate,comment,false,LocalDateTime.now());
        return repository.save(task);
    }

    public List<Task> getActiveTasks(Long chatId) {
        return repository.findByChatIdAndCompletedFalseOrderByNotifyDateAsc(chatId);
    }

    public List<Task> getCompletedTasks(Long chatId) {
        return repository.findByChatIdAndCompletedFalseOrderByNotifyDateDesc(chatId);
    }

    public boolean markAsCompleted(Long taskId, Long chatId) {
        Optional<Task> taskOpt = repository.findByIdAndChatId(taskId, chatId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setCompleted(true);
            repository.save(task);
            return true;
        }
        return false;
    }

    public boolean deleteTask(Long taskId, Long chatId) {
        Optional<Task> taskOpt = repository.findByIdAndChatId(taskId, chatId);
        if (taskOpt.isPresent()) {
            repository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public List<Task> getTasksForNotification(LocalDateTime now) {
        return repository.findByNotifyDateBeforeAndCompletedFalse(now);
    }
}

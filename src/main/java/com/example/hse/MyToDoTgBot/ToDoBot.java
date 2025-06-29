package com.example.hse.MyToDoTgBot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ToDoBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;
    private final TaskService taskService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ToDoBot(BotProperties botProperties, TaskService taskService) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.taskService = taskService;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            Long chatId = msg.getChatId();
            String text = msg.getText();

            if (text.startsWith("/start")) {
                handleStartCommand(chatId);
            } else if (text.startsWith("/add")) {
                handleAddCommand(chatId, text);
            } else if (text.startsWith("/active")) {
                handleActiveCommand(chatId);
            } else if (text.startsWith("/completed")) {
                handleCompletedCommand(chatId);
            } else if (text.startsWith("/done")) {
                handleDoneCommand(chatId, text);
            } else if (text.startsWith("/delete")) {
                handleDeleteCommand(chatId,text);
            } else if (text.startsWith("/help")) {
                sendHelpMessage(chatId);
            } else {
                sendMessage(chatId, " Неизвестная команда. Введите /help для списка команд ");
            }
        }
    }

    private void handleStartCommand(Long chatId) {
        String start = """
                Приветствую! Я бот помощник, помогу спланировать твое время.
                
                Чтобы увидеть мои доступные команды, отправь мне /help.
                """;
        sendMessage(chatId,start);
    }

    private void handleAddCommand(Long chatId, String text) {
        try {
            String[] parts = text.substring(5).split(";", 3);
            if (parts.length < 2) throw new IllegalArgumentException();

            String title = parts[0].trim();
            LocalDateTime date = parseDateTime(parts[1].trim());
            String comment = parts.length > 2 ? parts[2].trim() : "";

            Task task = taskService.createTask(chatId, title, date, comment);
            String response = "✅  Задача добавлена!\n" +
                    "ID: " + task.getId() + "\n" +
                    "Название: " + task.getTitle() + "\n" +
                    "Когда: " + task.getNotifyDate().format(formatter);
            sendMessage(chatId, response);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка формата. Пример: \n" +
                    "/add Название; Дата в формате: ДД.ММ.ГГГГ ЧЧ:мм; комментарий (если есть) ");
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private void handleActiveCommand(Long chatId) {
        List<Task> tasks = taskService.getActiveTasks(chatId);
        if (tasks.isEmpty()) {
            sendMessage(chatId, " У вас нет активных задач. ");
            return;
        }

        StringBuilder sb = new StringBuilder(" Активные задачи: \n\n");
        for (Task task : tasks) {
            sb.append("🆔 ID: ").append(task.getId()).append("\n")
                    .append("📌 Название: ").append(task.getTitle()).append("\n")
                    .append("⏰ Когда: ").append(task.getNotifyDate().format(formatter)).append("\n");
            if (task.getComment() != null && !task.getComment().isEmpty()) {
                sb.append("💬 Комментарий: ").append(task.getComment()).append("\n");
            }
            sb.append("\n");
        }
        sendMessage(chatId, sb.toString());
    }

    private void handleCompletedCommand(Long chatId) {
        List<Task> tasks = taskService.getCompletedTasks(chatId);
        if (tasks.isEmpty()) {
            sendMessage(chatId, " У вас нет завершенных задач. ");
            return;
        }
        StringBuilder sb = new StringBuilder("✅ Завершенные задачи:\n\n");
        for (Task task : tasks) {
            sb.append("🆔 ID: ").append(task.getId()).append("\n")
                    .append("📌 Название: ").append(task.getTitle()).append("\n")
                    .append("⏰ Было на: ").append(task.getNotifyDate().format(formatter)).append("\n\n");
        }
        sendMessage(chatId, sb.toString());
    }

    private void handleDoneCommand(Long chatId, String text) {
        try {
            Long taskId = Long.parseLong(text.substring(6).trim());
            if (taskService.markAsCompleted(taskId, chatId)) {
                sendMessage(chatId, " Задача помечена как выполненная. ");
            } else {
                sendMessage(chatId, " Задача не найдена или уже завершена. ");
            }
        } catch (Exception e) {
            sendMessage(chatId, " Неверный формат. Используйте : /done [ID_задачи]");
        }
    }

    private void handleDeleteCommand(Long chatId, String text) {
        try {
            Long taskId = Long.parseLong(text.substring(6).trim());
            if (taskService.deleteTask(taskId,chatId)) {
                sendMessage(chatId, " Задача удалена. ");
            } else {
                sendMessage(chatId," Задача не найдена. ");
            }
        } catch (Exception e) {
            sendMessage(chatId, " Неверный формат. Используйте : /delete [ID_задачи]");
        }
    }

    private void sendHelpMessage(Long chatId) {
        String help = """
                Доступные команды: 
                
                /add [Название]; [Дата ДД.ММ.ГГГГ ЧЧ:мм]; [Комментарий]
                        Добавить новую задачу
                        Пример: /add Встреча; 15.12.2023 14:30; Кабинет 305
                
                        /active - Показать активные задачи
                        /completed - Показать завершенные задачи
                        /done [ID] - Пометить задачу как выполненную
                        /delete [ID] - Удалить задачу
                        /help - Показать это сообщение
                """;
        sendMessage(chatId,help);
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Component
    @ConfigurationProperties(prefix = "telegram.bot")
    @Data
    public static class BotProperties {
        private String token;
        private String username;
    }
}

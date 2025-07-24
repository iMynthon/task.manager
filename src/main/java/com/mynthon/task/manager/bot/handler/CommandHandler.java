package com.mynthon.task.manager.bot.handler;

import com.mynthon.task.manager.common.feign.TaskFeignClient;
import com.mynthon.task.manager.task.dto.request.TaskRequest;
import com.mynthon.task.manager.task.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.dto.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;
import static com.mynthon.task.manager.bot.utils.StringUtils.*;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    private static final Map<Long, String> stateUserEdit = new HashMap<>();
    private static final Map<Long, TaskRequest> createTaskRequest = new HashMap<>();

    private final TaskFeignClient taskFeignClient;

    public SendMessage handlerMessage(String message, String username, Long chatId) {
        String state = stateUserEdit.get(chatId) == null ? "" : stateUserEdit.get(chatId);
        if (state.equals(EDIT_TASK_NAME) || state.equals(EDIT_TASK_CONTENT)) {
            return saveEditHandler(state, message, username, chatId);
        }
        return switch (message) {
            case START -> startMessage(username, chatId);
            case REGISTERED -> inlineKeyboardNewUser(username, chatId);
            case ADD_TASK -> inlineKeyboardNewTask(chatId);
            case TASK -> getTasks(chatId, username);
            default -> SendMessage.builder().chatId(chatId).text(username).text("Неизвестная команда").build();
        };
    }

    public SendMessage handleCallback(Long chatId, String callbackData) {
        switch (callbackData) {
            case EDIT_TASK_NAME -> {
                stateUserEdit.put(chatId, callbackData);
                return new SendMessage(chatId.toString(), "Введите название задачи:");
            }
            case EDIT_TASK_CONTENT -> {
                stateUserEdit.put(chatId, callbackData);
                return new SendMessage(chatId.toString(), "Введите описание задачи:");
            }
            default -> {
                return new SendMessage(chatId.toString(), "Неизвестное действие");
            }
        }
    }

    public SendMessage getTasks(Long chatId, String username) {
        AtomicInteger countIncrement = new AtomicInteger(1);
        AllTaskResponse response = taskFeignClient.findByMeAll(username);
        String message = response.listTasks().stream()
                .map(task -> String.format(
                        "%d. %s",
                        countIncrement.getAndIncrement(),
                        createOutputTask(task)
                ))
                .collect(Collectors.joining("\n\n"));
        if (response.listTasks().isEmpty()) {
            return new SendMessage(chatId.toString(), "Task list is Empty");
        }
        return new SendMessage(chatId.toString(), message);
    }

    private String createOutputTask(TaskResponse response) {
        return String.format(
                """
                🆔 %s
                🔹 %s
                📝 %s
                %s %s
                🕒 %s
                """,
                response.id(),
                response.name(),
                response.content(),
                response.isCompleted() ? "✅" : "❌",
                response.isCompleted() ? "Выполнено" : "В процессе",
                response.createAt()
        );
    }

    public SendMessage saveEditHandler(String state, String message, String username, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        TaskRequest request = createTaskRequest.getOrDefault(chatId, new TaskRequest());
        if (state.equals(EDIT_TASK_NAME)) {
            request.setName(message);
            request.setNickname(username);
            createTaskRequest.put(chatId, request);
            sendMessage.setChatId(chatId);
            sendMessage.setText(TASK_NAME_CREATE);
        } else if (state.equals(EDIT_TASK_CONTENT)) {
            request.setContent(message);
            request.setNickname(username);
            createTaskRequest.put(chatId, request);
            sendMessage.setChatId(chatId);
            sendMessage.setText(TASK_CONTENT_CREATE);
        }
        if (request.isComplete()) {
            taskFeignClient.save(request);
            createTaskRequest.remove(chatId);
            stateUserEdit.remove(chatId);
            sendMessage.setChatId(chatId);
            sendMessage.setText(TASK_CREATE);
            return sendMessage;
        }
        return sendMessage;
    }

    private SendMessage startMessage(String username, Long chatId) {
        String startMessage = String.format("Привествую тебя %s в моем task manager bot, " +
                "здесь можно будет ставить себе задачи и настраивать напоминаия о них", username);
        return SendMessage.builder()
                .chatId(chatId)
                .text(startMessage)
                .build();
    }

    private SendMessage inlineKeyboardNewUser(String username, Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(String.format("""
                        Привет %s
                        Заполни форму регистрации\
                        
                        Выберите поле для редактирования:""", username))
                .parseMode("MarkDownV2")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("✏️ Email")
                                        .callbackData("edit_email_user")
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("✏️ password")
                                        .callbackData("edit_password_user")
                                        .build()))
                        .build())
                .build();
    }

    private SendMessage inlineKeyboardNewTask(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Форма создания задачи\nВыберите поле для редактирования:")
                .parseMode("MarkdownV2")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(InlineKeyboardButton.builder()
                                        .text("✏️ Название")
                                        .callbackData("edit_task_name")
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("✏️ Описание")
                                        .callbackData("edit_task_content")
                                        .build()))
                        .build())
                .build();
    }
}

package com.mynthon.task.manager.bot.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.common.feign.TaskFeignClient;
import com.mynthon.task.manager.task.api.dto.request.TaskDeleteRequest;
import com.mynthon.task.manager.task.api.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.api.dto.request.TaskRequest;
import com.mynthon.task.manager.task.api.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.api.dto.response.TaskResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;
import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.EDIT_TASK_CONTENT;
import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.EDIT_TASK_NAME;
import static com.mynthon.task.manager.bot.utils.StringUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskHandler {

    public static final Map<Long, String> stateUserTaskEdit = new HashMap<>();
    private static final Map<Long, TaskRequest> createTaskRequest = new HashMap<>();
    private final TaskFeignClient taskFeignClient;

    public SendMessage handleTaskEditor(Long chatId, String taskRequest) {
        switch (taskRequest) {
            case EDIT_TASK_NAME -> {
                log.info("Изменение названия задачи - {} - {}", chatId, taskRequest);
                stateUserTaskEdit.put(chatId, taskRequest);
                return new SendMessage(chatId.toString(), "Введите название задачи:");
            }
            case EDIT_TASK_CONTENT -> {
                log.info("Изменение описания задачи - {} - {}", chatId, taskRequest);
                stateUserTaskEdit.put(chatId, taskRequest);
                return new SendMessage(chatId.toString(), "Введите описание задачи:");
            }
            default -> {
                return new SendMessage(chatId.toString(), "Неизвестное действие");
            }
        }
    }

    public SendMessage getTasks(Long chatId, String username) {
        log.info("Вывод всех созданных задач пользователя - {}", username);
        try {
            AllTaskResponse response = taskFeignClient.findByMeAll(username);
            String message = response.listTasks().stream()
                    .map(task -> String.format(
                            "%s", createOutputTask(task)
                    ))
                    .collect(Collectors.joining("\n\n"));
            if (response.listTasks().isEmpty()) {
                return new SendMessage(chatId.toString(), "Список задач пуст");
            }
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(message + "\n\n<b>Общее количество задач: " + response.listTasks().size() + "</b>")
                    .parseMode("HTML")
                    .build();
        } catch (Exception fe){
            log.info("Ошибка запроса - {}",fe.getMessage());
            String message = "";
            if(fe.getMessage().contains("404")){
                message = "[{" + fe.getMessage().substring(fe.getMessage().lastIndexOf(":") + 1);
            }
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(message.isEmpty() ? fe.getMessage() : message)
                    .build();
        }
    }

    private String createOutputTask(TaskResponse response) {
        return String.format("""
                        <b>№:</b> <b>%s</b>
                        <b>Название:</b> %s
                        <b>Описание:</b> %s
                        <b>Статус:</b> %s
                        <b>Создано:</b> <i>%s</i>
                        """, response.id(), escapeHtml(response.name()), escapeHtml(response.content()),
                response.isCompleted() ? "<b>Выполнено</b>" : "<b>В процессе: -> </b>" + TASK_COMPLETE  + response.id(),
                escapeHtml(response.createAt().toString())) + "<b>Удалить: -></b> " + TASK_DELETE + response.id();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public SendMessage saveEditHandler(String state,String message, String username, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        TaskRequest request = createTaskRequest.getOrDefault(chatId, new TaskRequest());
        if (state.equals(EDIT_TASK_NAME)) {
            request.setName(message);
            sendMessage = createSendMessageFromTask(chatId,request,username,EDIT_TASK_NAME);
        } else if (state.equals(EDIT_TASK_CONTENT)) {
            request.setContent(message);
            sendMessage = createSendMessageFromTask(chatId,request,username,EDIT_TASK_CONTENT);
        }
        if (request.isComplete()) {
            TaskResponse response = taskFeignClient.save(request);
            createTaskRequest.remove(chatId);
            stateUserTaskEdit.remove(chatId);
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            sendMessage.setChatId(chatId);
            sendMessage.setText(String.format("%s: id - %s, name - %s",TASK_CREATE,response.id(),response.name()));
            return sendMessage;
        }
        return sendMessage;
    }

    private SendMessage createSendMessageFromTask(Long chatId,TaskRequest request,
                                                  String username,String operationTask){
        request.setUsername(username);
        request.setChatId(chatId);
        stateUserTaskEdit.remove(chatId);
        createTaskRequest.put(chatId, request);
        log.info("Сохранение описания задачи - {} - {}", username, request);
        return SendMessage.builder()
                .chatId(chatId)
                .text(operationTask.equals(EDIT_TASK_NAME) ? TASK_NAME_CREATE : TASK_CONTENT_CREATE)
                .replyMarkup(request.isComplete() ? null :
                        keyboardEditTask(operationTask.equals(EDIT_TASK_NAME) ? EDIT_TASK_NAME : EDIT_TASK_CONTENT))
                .build();
    }


    public SendMessage taskCommandIsCompleteAndDelete(Long chatId, String message, String username){
        String result;
        int id = Integer.parseInt(message.substring(message.contains(TASK_COMPLETE) ? TASK_COMPLETE.length() : TASK_DELETE.length()));
        if(message.contains(TASK_COMPLETE)) {
            result = taskFeignClient.updateIsCompleted(new TaskIsCompleted(id, username, true));
        } else {
            result = taskFeignClient.deleteMeTask(new TaskDeleteRequest(id, username));
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text(result.isEmpty() ? "Некорреткный запрос" : result)
                .build();
    }

    public SendMessage inlineKeyboardNewTask(Long chatId) {
        log.info("Создание задачи - {}", chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text("Форма создания задачи\nВыберите поле для редактирования:")
                .parseMode("MarkdownV2")
                .replyMarkup(keyboardEditTask(""))
                .build();
    }

    private ReplyKeyboardMarkup keyboardEditTask(String edit){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow key1 = new KeyboardRow();
        log.info("Создание кнопок для выбора операции");
        if(edit.equals(EDIT_TASK_NAME)){
            key1.add(EDIT_TASK_CONTENT);
        } else if (edit.equals(EDIT_TASK_CONTENT)){
            key1.add(EDIT_TASK_NAME);
        } else {
            key1.add(EDIT_TASK_NAME);
            key1.add(EDIT_TASK_CONTENT);
        }
        keyboardMarkup.setKeyboard(List.of(key1));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        return keyboardMarkup;
    }
}

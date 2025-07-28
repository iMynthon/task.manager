package com.mynthon.task.manager.bot.handler;
import com.mynthon.task.manager.common.feign.TaskFeignClient;
import com.mynthon.task.manager.task.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.dto.request.TaskRequest;
import com.mynthon.task.manager.task.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.dto.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;
import static com.mynthon.task.manager.bot.utils.StringUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandHandler {

    private static final Map<Long, String> stateUserEdit = new HashMap<>();
    private static final Map<Long, TaskRequest> createTaskRequest = new HashMap<>();

    private final TaskFeignClient taskFeignClient;

    public SendMessage handlerMessage(String message, String username, Long chatId) {
        String state = stateUserEdit.get(chatId) == null ? "" : stateUserEdit.get(chatId);
        if(state.equals(EDIT_TASK_NAME) || state.equals(EDIT_TASK_CONTENT)){
            log.info("Создание задачи пользователем - {} - {}",username,message);
            return saveEditHandler(state,message,username,chatId);
        }
        log.info("Поймано сообщение пользователя - {} - {}", username, message);
        if(message.contains(TASK_COMPLETE)){
           return taskCommandIsComplete(chatId,message,username);
        }
        return switch (message) {
            case START -> startMessage(username, chatId);
            case HELP -> helpMessage(chatId);
            case REGISTERED -> inlineKeyboardNewUser(username, chatId);
            case ADD_TASK -> inlineKeyboardNewTask(chatId);
            case TASK -> getTasks(chatId, username);
            case EDIT_TASK_NAME,EDIT_TASK_CONTENT -> handleTaskEditor(chatId,message);
            default -> SendMessage.builder().chatId(chatId).text(username).text("Неизвестная команда").build();
        };
    }

    private SendMessage handleTaskEditor(Long chatId, String taskRequest) {
        switch (taskRequest) {
            case EDIT_TASK_NAME -> {
                log.info("Изменение названия задачи - {} - {}", chatId, taskRequest);
                stateUserEdit.put(chatId, taskRequest);
                return new SendMessage(chatId.toString(), "Введите название задачи:");
            }
            case EDIT_TASK_CONTENT -> {
                log.info("Изменение описания задачи - {} - {}", chatId, taskRequest);
                stateUserEdit.put(chatId, taskRequest);
                return new SendMessage(chatId.toString(), "Введите описание задачи:");
            }
            default -> {
                return new SendMessage(chatId.toString(), "Неизвестное действие");
            }
        }
    }

    private SendMessage getTasks(Long chatId, String username) {
        log.info("Вывод всех созданных задач пользователя - {}", username);
        AllTaskResponse response = taskFeignClient.findByMeAll(username);
        String message = response.listTasks().stream()
                .map(task -> String.format(
                        "%s", createOutputTask(task)
                ))
                .collect(Collectors.joining("\n\n"));
        if (response.listTasks().isEmpty()) {
            return new SendMessage(chatId.toString(), "Task list is Empty");
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text(message + "<b>\nОбщее количество задач: " + response.listTasks().size() + "</b>")
                .parseMode("HTML")
                .build();
    }

    private String createOutputTask(TaskResponse response) {
        return String.format("""
                        <b>№:</b> <b>%s</b>
                        <b>Название:</b> %s
                        <b>Описание:</b> %s
                        <b>Статус:</b> %s
                        <b>Создано:</b> <i>%s</i>
                        """, response.id(), escapeHtml(response.name()), escapeHtml(response.content()),
                response.isCompleted() ? "Выполнено" : "В процессе -> " + TASK_COMPLETE  + response.id(),
                escapeHtml(response.createAt().toString()));
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private SendMessage saveEditHandler(String state,String message, String username, Long chatId) {
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
            taskFeignClient.save(request);
            createTaskRequest.remove(chatId);
            stateUserEdit.remove(chatId);
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            sendMessage.setChatId(chatId);
            sendMessage.setText(TASK_CREATE);
            return sendMessage;
        }
        return sendMessage;
    }

    private SendMessage createSendMessageFromTask(Long chatId,TaskRequest request,
                                                  String username,String operationTask){
        request.setUsername(username);
        request.setChatId(chatId);
        stateUserEdit.remove(chatId);
        createTaskRequest.put(chatId, request);
        log.info("Сохранение описания задачи - {} - {}", username, request);
        return SendMessage.builder()
                .chatId(chatId)
                .text(operationTask.equals(EDIT_TASK_NAME) ? TASK_NAME_CREATE : TASK_CONTENT_CREATE)
                .replyMarkup(request.isComplete() ? null :
                        createKeyboardMarkup(operationTask.equals(EDIT_TASK_NAME) ? EDIT_TASK_NAME : EDIT_TASK_CONTENT))
                .build();
    }

    private SendMessage startMessage(String username, Long chatId) {
        String startMessage = String.format("Привествую тебя %s в моем task manager bot, " +
                "здесь можно будет ставить себе задачи и настраивать напоминаия о них.\n" +
                "Для ознакомления с командами и посмотреть примеры и описания к ним, выполните команду - /help", username);
        return SendMessage.builder()
                .chatId(chatId)
                .text(startMessage)
                .build();
    }

    private SendMessage taskCommandIsComplete(Long chatId,String message,String username){
        Integer id = Integer.parseInt(message.substring(message.length() - 1));
        String result = taskFeignClient.updateIsCompleted(new TaskIsCompleted(id,username,true));
        return SendMessage.builder()
                .chatId(chatId)
                .text(result)
                .build();
    }

    private SendMessage helpMessage(Long chatId) {
        String commandsHelp = """
                /start -> начало вашей бота с вами. Отображает зарегистрированы вы в системе иили нет.
                /help -> Просмотр всех команд бота
                /add_task -> Добавление задачи. Вводится по очереди 2 поля: {название задачи} - {описание задачи}.
                /tasks -> Просмотр всех своих задач.
                """;
        return new SendMessage(chatId.toString(), commandsHelp);
    }

    private SendMessage inlineKeyboardNewUser(String username, Long chatId) {
        log.info("Регистрация пользователя - {}", username);
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
        log.info("Создание задачи - {}", chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text("Форма создания задачи\nВыберите поле для редактирования:")
                .parseMode("MarkdownV2")
                .replyMarkup(createKeyboardMarkup(""))
                .build();
    }

    private ReplyKeyboardMarkup createKeyboardMarkup(String edit){
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

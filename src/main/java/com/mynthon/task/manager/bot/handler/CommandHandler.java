package com.mynthon.task.manager.bot.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.mynthon.task.manager.bot.handler.ReminderHandler.stateUserReminderEdit;
import static com.mynthon.task.manager.bot.handler.TaskHandler.stateUserTaskEdit;
import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandHandler {

    private final TaskHandler taskHandler;
    private final ReminderHandler reminderHandler;

    public SendMessage handlerMessage(String message, String username, Long chatId) {
        String state = checkoutState(chatId);
        if(message.contains(TASK_COMPLETE) || message.contains(TASK_DELETE)
                || message.contains(REMINDER_READ) || message.contains(REMINDER_DELETE)){
            return completeDeleteAndRead(username,message,chatId);
        }
        if(!state.isBlank()) {
            return stateMessageTaskOrReminder(state, username, message, chatId);
        }
        return switch (message) {
            case START -> startMessage(username, chatId);
            case HELP -> helpMessage(chatId);
            case ADD_TASK -> taskHandler.inlineKeyboardNewTask(chatId);
            case TASK -> taskHandler.getTasks(chatId, username);
            case EDIT_TASK_NAME,EDIT_TASK_CONTENT -> taskHandler.handleTaskEditor(chatId,message);
            case REMINDER_TASK -> reminderHandler.inlineKeyboardNewReminder(chatId);
            case REMINDER -> reminderHandler.getReminders(chatId,username);
            case REMINDER_TASK_ID, REMINDER_TIME -> reminderHandler.handlerReminderEditor(chatId,message);
            default -> SendMessage.builder().chatId(chatId).text(username).text("Неизвестная команда").build();
        };
    }

    private String checkoutState(Long chatId){
        if(stateUserTaskEdit.get(chatId) != null){
            return stateUserTaskEdit.get(chatId);
        } else if(stateUserReminderEdit.get(chatId) != null){
            return stateUserReminderEdit.get(chatId);
        } else {
            return "";
        }
    }

    private SendMessage completeDeleteAndRead(String username, String message, Long chatId){
        log.info("Выполнение команды {} - пользователем - {}",message,username);
        SendMessage sendMessage = new SendMessage();
        if(message.contains(TASK_COMPLETE) || message.contains(TASK_DELETE)){
            sendMessage = taskHandler.taskCommandIsCompleteAndDelete(chatId, message, username);
        }
        else if(message.contains(REMINDER_READ) || message.contains(REMINDER_DELETE)){
            sendMessage = reminderHandler.readAndDeleteReminder(chatId,message);
        }
        return sendMessage;
    }

    private SendMessage stateMessageTaskOrReminder(String state, String username, String message, Long chatId){
        SendMessage sendMessage = new SendMessage();
        if(state.equals(EDIT_TASK_NAME) || state.equals(EDIT_TASK_CONTENT)){
            log.info("Создание задачи пользователем - {} - {}",username,message);
            sendMessage = taskHandler.saveEditHandler(state, message, username, chatId);
        } else if (state.equals(REMINDER_TASK_ID) || state.equals(REMINDER_TIME)) {
            log.info("Создание напоминание для пользователя - {}",username);
            sendMessage = reminderHandler.createReminder(chatId,username,message,state);
        }
        return sendMessage;
    }

    private SendMessage startMessage(String username, Long chatId) {
        String startMessage = String.format("Привествую тебя %s в моем task manager bot, " +
                "здесь можно будет ставить себе задачи и настраивать напоминаия о них.\n" +
                "Для ознакомления с командами и посмотреть примеры и описания к ним, выполните команду -> /help", username);
        return SendMessage.builder()
                .chatId(chatId)
                .text(startMessage)
                .build();
    }

    private SendMessage helpMessage(Long chatId) {
        String commandsHelp = """
                /help -> Просмотр всех команд бота.
                /add_task -> Добавление задачи. Вводится по очереди 2 поля: <b>{Название задачи} - {Описание задачи}</b>.
                /tasks -> Просмотр всех своих задач, так же можно сразу нажать команды <b>complete</b> и <b>delete</b> что бы отметить задачу отмеченной или удалить ее.
                /reminders -> Просмотр запланированных напоминаний.
                /reminder_task -> Поставить напоминание или запланировать выполнение задачи. Вводится оп очереди 2 поля: <b>{Индентификатор задачи} - {Время напоминания}</b>.
                Еще 2 команды выполняются по нажатию на них, вводить их не надо -> /complete и /delete, они выполняться автоматически.
                """;
        return SendMessage.builder()
                .chatId(chatId)
                .text(commandsHelp)
                .parseMode("HTML")
                .build();
    }
}

package com.mynthon.task.manager.reminder.api.service;
import com.mynthon.task.manager.reminder.api.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.api.dto.response.AllReminderResponse;
import com.mynthon.task.manager.reminder.api.dto.response.ReminderResponse;
import com.mynthon.task.manager.common.mapper.ReminderMapper;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.reminder.internal.repository.ReminderRepository;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.task.api.service.TaskService;
import com.mynthon.task.manager.user.api.service.UserService;
import com.mynthon.task.manager.user.internal.repository.projections.UserIdProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.mynthon.task.manager.reminder.internal.model.ReminderStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final ReminderMapper reminderMapper;

    @Transactional(readOnly = true)
    public AllReminderResponse getAllReminder(String username){
        UserIdProjection userId = userService.userIdProjection(username);
        return reminderMapper.listEntityToListResponse(reminderRepository.findByUserId(userId.getId()));
    }

    @Transactional
    public void save(ReminderRequest request){
        Reminder reminder = reminderMapper.requestToEntity(request);
        Task task = taskService.findById(request.getTaskId());
        reminder.setTask(task);
        reminder.setUser(task.getUser());
        reminderRepository.save(reminder);
    }

    public String deleteReminder(Integer id){
        reminderRepository.deleteById(id);
        return String.format("Напоминание под идентификатором - %s успешно удалено",id);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> checkWaitingReminder(){
        List<Reminder> reminderList = reminderRepository.findByStatusAndTime(LocalDateTime.now());
        return reminderList.isEmpty() ? Collections.emptyList() : reminderList.stream().map(reminderMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public void setReminderStatus(Integer id){
        reminderRepository.setStatusReminder(SENT.toString(),id);
    }

    @Transactional
    public String acceptedReminder(Integer id){
        reminderRepository.setStatusReminder(ACCEPTED.toString(),id);
        return String.format("Напоминание под индентификатором - {%s} успешно принято",id);
    }
}

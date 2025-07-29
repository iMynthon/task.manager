package com.mynthon.task.manager.reminder.internal.service;

import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.reminder.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.dto.response.AllReminderResponse;
import com.mynthon.task.manager.reminder.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.internal.mapper.ReminderMapper;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.reminder.internal.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;

    public ReminderResponse save(ReminderRequest request){
        Reminder reminder = reminderRepository.save(reminderMapper.requestToEntity(request));
        ReminderResponse response = reminderMapper.entityToResponse(reminder);
        return response;
    }

    public AllReminderResponse findByUsernameAndTaskId(String username, Integer id){
        List<Reminder> reminder = reminderRepository.findByUserUsernameAndTaskId(username,id);
        return reminderMapper.listEntityToListResponse(reminder);
    }

    public String deleteReminder(Integer id){
        reminderRepository.deleteById(id);
        return String.format("Напоминание под идентификатором - %s успешно удалено",id);
    }
}

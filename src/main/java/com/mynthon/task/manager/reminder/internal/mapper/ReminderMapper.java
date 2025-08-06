package com.mynthon.task.manager.reminder.internal.mapper;

import com.mynthon.task.manager.reminder.api.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.api.dto.response.AllReminderResponse;
import com.mynthon.task.manager.reminder.api.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.modulith.Modulithic;

import java.util.List;

@Modulithic
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReminderMapper {

    @Mapping(target = "user.username",source = "username")
    @Mapping(target = "user.chatId",source = "chatId")
    @Mapping(target = "task.id",source = "taskId")
    Reminder requestToEntity(ReminderRequest request);

    @Mapping(target = "username",source = "user.username")
    @Mapping(target = "chatId",source = "user.chatId")
    @Mapping(target = "taskName",source = "task.name")
    ReminderResponse entityToResponse(Reminder reminder);

    default AllReminderResponse listEntityToListResponse(List<Reminder> reminderList){
        return new AllReminderResponse(reminderList.stream()
                .map(this::entityToResponse).toList());
    }
}

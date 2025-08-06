package com.mynthon.task.manager.reminder.api.dto.request;


import com.mynthon.task.manager.reminder.internal.model.ReminderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ReminderRequest{

    private String username;

    private Long chatId;

    private Integer taskId;

    private LocalDateTime time;

    private ReminderStatus status;

    public boolean isComplete() {
        return username != null && chatId != null && taskId != null && time != null;
    }
}

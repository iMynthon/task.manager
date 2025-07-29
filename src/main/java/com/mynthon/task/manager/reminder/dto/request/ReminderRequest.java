package com.mynthon.task.manager.reminder.dto.request;


import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ReminderRequest{

    private String username;

    private Long chatId;

    private String taskName;

    private LocalDateTime time;

    public boolean isComplete() {
        return username != null && chatId != null && taskName != null && time != null;
    }
}

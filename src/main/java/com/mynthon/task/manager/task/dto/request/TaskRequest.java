package com.mynthon.task.manager.task.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TaskRequest{

    private String name;
    @EqualsAndHashCode.Exclude
    private String content;
    private String username;
    private Long chatId;

    public boolean isComplete() {
        return name != null && content != null && username != null;
    }
}

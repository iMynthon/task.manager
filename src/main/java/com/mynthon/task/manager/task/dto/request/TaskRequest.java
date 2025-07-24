package com.mynthon.task.manager.task.dto.request;

import lombok.*;

import java.util.Objects;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskRequest{

    private String name;
    private String content;
    private String nickname;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRequest that = (TaskRequest) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(content, that.content) &&
                Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content, nickname);
    }

    public boolean isComplete() {
        return name != null && content != null && nickname != null;
    }
}

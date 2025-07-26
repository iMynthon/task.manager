package com.mynthon.task.manager.common.configuration;

import com.mynthon.task.manager.task.internal.model.TaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.TASK_EVENTS_EXCHANGE;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.TASK_QUEUE_KEY;

@Configuration
@Slf4j
public class EventConfig {

    @Bean
    public EventExternalizationConfiguration externalizedConfiguration() {
        return EventExternalizationConfiguration.externalizing()
                .select(event -> getClass().getPackageName().contains("task"))
                .route(TaskEvent.class,
                        event -> RoutingTarget.forTarget(TASK_EVENTS_EXCHANGE)
                                .andKey(TASK_QUEUE_KEY))
                .build();
    }
}



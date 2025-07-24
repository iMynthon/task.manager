package com.mynthon.task.manager.common.configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final String ADD_TASK_QUEUE = "queue.add.task";
    private final String MODULE_TASK_TOPIC = "module.task.topic";
    private final String MODULE_TASK_EVENT = "module.task.event";

    @Bean
    public Queue userAddTaskQueue(){
        return QueueBuilder.durable(ADD_TASK_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange addTaskTopicExchange(){
        return ExchangeBuilder.topicExchange(MODULE_TASK_TOPIC)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange directExchangeAddTaskQueue(){
        return ExchangeBuilder.directExchange(MODULE_TASK_EVENT)
                .durable(true)
                .build();
    }

    @Bean
    public Binding userAddTaskQueueBinding(){
        return BindingBuilder.bind(userAddTaskQueue())
                .to(directExchangeAddTaskQueue())
                .with(ADD_TASK_QUEUE);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .build();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

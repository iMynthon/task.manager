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

    public static final String TASK_QUEUE_KEY = "key.task.queue";
    public static final String TASK_EVENTS_TOPIC = "module.task.events.topic";
    public static final String TASK_EVENTS_EXCHANGE = "module.task.events.exchange";

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(TASK_QUEUE_KEY)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TASK_QUEUE_KEY + ".dlq")
                .build();
    }

    @Bean
    public TopicExchange taskEventsTopicExchange() {
        return ExchangeBuilder.topicExchange(TASK_EVENTS_TOPIC)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange taskEventsExchange() {
        return ExchangeBuilder.directExchange(TASK_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding taskQueueBinding() {
        return BindingBuilder.bind(taskQueue())
                .to(taskEventsExchange())
                .with(TASK_QUEUE_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .build();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Queue taskQueueDlq() {
        return QueueBuilder.durable(TASK_QUEUE_KEY + ".dlq").build();
    }
}

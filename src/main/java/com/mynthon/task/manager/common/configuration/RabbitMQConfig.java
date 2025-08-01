package com.mynthon.task.manager.common.configuration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE = "key.task.queue";
    public static final String REMINDER_QUEUE = "key.reminder.queue";
    public static final String USER_REMINDER_QUEUE = "key.reminder.task.queue";


    public static final String TASK_RT_KEY = "*.task.queue";
    public static final String REMINDER_RT_KEY = "*.reminder.queue";
    public static final String USER_REMINDER_RT_KEY = "*.reminder.task.queue";

    public static final String MAIN_EVENTS_TOPIC = "task.manager.events.exchange";
    public static final String REMINDER_EVENTS_EXCHANGE = "module.reminder.delayed.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    @Bean
    public Queue userQueue(){
        return QueueBuilder.durable(USER_REMINDER_QUEUE)
                .build();
    }

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(TASK_QUEUE)
                .build();
    }

    @Bean
    public Queue reminderQueue(){
        return QueueBuilder.durable(REMINDER_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange topicTaskManager() {
        return ExchangeBuilder.topicExchange(MAIN_EVENTS_TOPIC)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange delayedReminderExchange() {
        return ExchangeBuilder.directExchange(REMINDER_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding reminderBind(){
        return BindingBuilder.bind(reminderQueue())
                .to(topicTaskManager())
                .with(REMINDER_RT_KEY);
    }

    @Bean
    public Binding userBind(){
        return BindingBuilder.bind(userQueue())
                .to(delayedReminderExchange())
                .with(USER_REMINDER_RT_KEY);
    }

    @Bean
    public Binding taskBind(){
        return BindingBuilder.bind(taskQueue())
                .to(topicTaskManager())
                .with(TASK_RT_KEY);
    }

    @Bean
    public Queue taskQueueDlq() {
        return QueueBuilder.durable("error.message.dlq")
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE) // Указываем наш DLX
                .withArgument("x-dead-letter-routing-key", "error.message.dlq")
                .build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(taskQueueDlq())
                .to(dlxExchange())
                .with("error.message.dlq");
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                DLX_EXCHANGE,
                "error.message.dlq"
        );
    }
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public DirectRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageRecoverer messageRecoverer,Jackson2JsonMessageConverter messageConverter) {

        DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(messageRecoverer)
                .build();

        factory.setAdviceChain(interceptor);
        return factory;
    }
}

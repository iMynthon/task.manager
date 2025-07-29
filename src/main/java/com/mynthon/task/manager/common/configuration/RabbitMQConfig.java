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

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE_KEY = "key.task.queue";
    public static final String REMINDER_QUEUE_KEY = "key.reminder.queue";
    public static final String USER_REMINDER_TASK = "key.reminder.task.queue";

    public static final String TASK_EVENTS_EXCHANGE = "module.task.events.exchange";
    public static final String REMINDER_EVENTS_EXCHANGE = "module.reminder.events.exchange";

    public static final String DLX_EXCHANGE = "dlx.exchange";

    @Bean
    public Queue userQueue(){
        return QueueBuilder.durable(USER_REMINDER_TASK)
                .build();
    }

    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(TASK_QUEUE_KEY)
                .build();
    }

    @Bean
    public Queue reminderQueue(){
        return QueueBuilder.durable(REMINDER_QUEUE_KEY)
                .build();
    }

    @Bean
    public DirectExchange taskEventsExchange() {
        return ExchangeBuilder.directExchange(TASK_EVENTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange reminderTopicExchange(){
        return ExchangeBuilder.topicExchange(REMINDER_EVENTS_EXCHANGE)
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
    public Binding[] reminderQueueBinding() {
        return new Binding[]{
                BindingBuilder.bind(reminderQueue())
                        .to(reminderTopicExchange())
                        .with(REMINDER_QUEUE_KEY),
                BindingBuilder.bind(userQueue())
                        .to(reminderTopicExchange())
                        .with(USER_REMINDER_TASK)};
    }

    @Bean
    public Queue taskQueueDlq() {
        return QueueBuilder.durable(TASK_QUEUE_KEY + ".dlq")
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE) // Указываем наш DLX
                .withArgument("x-dead-letter-routing-key", TASK_QUEUE_KEY + ".dlq")
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
                .with(TASK_QUEUE_KEY + ".dlq");
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                DLX_EXCHANGE,
                TASK_QUEUE_KEY + ".dlq"
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

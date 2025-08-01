package com.mynthon.task.manager;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.*;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.USER_REMINDER_RT_KEY;

@SpringBootTest
public class RabbitMQTest {

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Test
    public void SendTaskMessageTest(){
        long delayMillis = 60000;
        rabbitTemplate.convertAndSend(REMINDER_EVENTS_EXCHANGE, USER_REMINDER_RT_KEY,"test", msg -> {
            msg.getMessageProperties().setDelayLong(delayMillis);
            return msg;
        });
    }
}

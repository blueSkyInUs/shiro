package com.init.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author lesson
 * @date 2017/12/5 10:17
 */
@Component
@RabbitListener(queues = "lesson")
public class HelloRabbitListener {


    @RabbitHandler
    public void handle(String hello){
        System.out.println(hello);
    }
}

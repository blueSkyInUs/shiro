package com.init.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lesson
 * @date 2017/12/5 16:02
 */
@Component
public class RabbitmqConfig implements InitializingBean {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProtobufMessageConvert protobufMessageConvert;


    /**
     * 采用protobuf 节省流量 也更快
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        rabbitTemplate.setMessageConverter(protobufMessageConvert);
    }
}

package com.cyr.ojbackendquestionservice.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/19 14:59
 */
@Component
public class MyMessageProducer {

	@Resource
	private RabbitTemplate rabbitTemplate;

	public void sendMessage(String exchange, String routingKey, String message) {
		rabbitTemplate.convertAndSend(exchange, routingKey, message);
	}

}
package com.cyr.ojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/19 14:52
 */
@Slf4j
public class InitRabbitMq {
	public static void doInit() {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			String EXCHANGE_NAME = "code_exchange";
			channel.exchangeDeclare(EXCHANGE_NAME, "direct");

			// 创建队列，随机分配一个队列名称
			String queueName = "code_queue";
			channel.queueDeclare(queueName, true, false, false, null);
			channel.queueBind(queueName, EXCHANGE_NAME, "my_routingKey");
			log.info("消息队列创建成功");
		} catch (Exception e) {
			log.error("消息队列创建失败" + e);
		}
	}
}

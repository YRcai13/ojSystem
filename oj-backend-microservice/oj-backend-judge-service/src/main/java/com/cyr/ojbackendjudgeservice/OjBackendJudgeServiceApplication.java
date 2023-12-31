package com.cyr.ojbackendjudgeservice;

import com.cyr.ojbackendjudgeservice.rabbitmq.InitRabbitMq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 17:27
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.cyr")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cyr.ojbackendserviceclient.service"})
public class OjBackendJudgeServiceApplication {
	public static void main(String[] args) {
		InitRabbitMq.doInit();
		SpringApplication.run(OjBackendJudgeServiceApplication.class, args);
	}

}

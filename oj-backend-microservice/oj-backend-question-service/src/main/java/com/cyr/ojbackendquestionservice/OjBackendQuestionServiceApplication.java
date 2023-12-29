package com.cyr.ojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 17:27
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.cyr.ojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.cyr")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cyr.ojbackendserviceclient.service"})
public class OjBackendQuestionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(OjBackendQuestionServiceApplication.class, args);
	}

}

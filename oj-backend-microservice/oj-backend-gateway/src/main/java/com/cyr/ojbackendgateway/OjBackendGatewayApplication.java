package com.cyr.ojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 20:22
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OjBackendGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(OjBackendGatewayApplication.class, args);
	}
}

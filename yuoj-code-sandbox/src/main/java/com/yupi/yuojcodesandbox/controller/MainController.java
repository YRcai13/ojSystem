package com.yupi.yuojcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 15:27
 */
@RestController("/")
public class MainController {

	@GetMapping("/health")
	public String healthCheck() throws InterruptedException {
		return "ok";
	}
}

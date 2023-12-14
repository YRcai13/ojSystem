package com.yupi.yuojcodesandbox.controller;

import com.yupi.yuojcodesandbox.MyJavaDockerCodeSandbox;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 15:27
 */
@RestController("/")
public class MainController {

	@Resource
	private MyJavaDockerCodeSandbox myJavaDockerCodeSandbox;

	@GetMapping("/health")
	public String healthCheck() throws InterruptedException {
		return "ok";
	}

	@PostMapping("/executeCode")
	public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
		if (executeCodeRequest == null) {
			throw new RuntimeException("请求参数为空");
		}
		ExecuteCodeResponse executeCodeResponse = myJavaDockerCodeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
		return executeCodeResponse;
	}
}

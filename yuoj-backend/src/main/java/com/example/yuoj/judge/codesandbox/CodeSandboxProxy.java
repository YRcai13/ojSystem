package com.example.yuoj.judge.codesandbox;

import com.example.yuoj.judge.codesandbox.CodeSandbox;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 20:09
 */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

	private CodeSandbox codeSandbox;

	public CodeSandboxProxy(CodeSandbox codeSandbox) {
		this.codeSandbox = codeSandbox;
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		log.info("代码沙箱请求信息：" + executeCodeRequest);
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		log.info("代码沙箱响应信息：" + executeCodeResponse);
		return executeCodeResponse;
	}
}

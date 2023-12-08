package com.example.yuoj.judge.codesandbox.impl;

import com.example.yuoj.judge.codesandbox.CodeSandbox;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:35
 */
public class RemoteCodeSandbox implements CodeSandbox {

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		System.out.println("远程沙箱");
		return null;
	}
 	
}

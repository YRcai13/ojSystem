package com.cyr.ojbackendjudgeservice.judge.codesandbox.impl;


import com.cyr.ojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:35
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		System.out.println("第三方沙箱");
		return null;
	}
}

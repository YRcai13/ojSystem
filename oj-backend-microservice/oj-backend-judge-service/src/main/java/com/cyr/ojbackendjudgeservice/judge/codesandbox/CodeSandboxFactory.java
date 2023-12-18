package com.cyr.ojbackendjudgeservice.judge.codesandbox;

import com.cyr.ojbackendjudgeservice.judge.codesandbox.impl.ExampleCodeSandbox;
import com.cyr.ojbackendjudgeservice.judge.codesandbox.impl.RemoteCodeSandbox;
import com.cyr.ojbackendjudgeservice.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:47
 */

public class CodeSandboxFactory {
	public static CodeSandbox newInstance(String type) {
		switch (type) {
			case "example":
				return new ExampleCodeSandbox();
			case "remote":
				return new RemoteCodeSandbox();
			case "thirdParty":
				return new ThirdPartyCodeSandbox();
			default:
				return new ExampleCodeSandbox();
		}
	}
}

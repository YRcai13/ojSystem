package com.yupi.yuojcodesandbox.controller;

import com.yupi.yuojcodesandbox.CodeSandbox;
import com.yupi.yuojcodesandbox.JavaNativeCodeSandbox;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 16:17
 */
public class Main {
	public static void main(String[] args) {
		CodeSandbox codeSandbox = new JavaNativeCodeSandbox();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(null);

	}
}

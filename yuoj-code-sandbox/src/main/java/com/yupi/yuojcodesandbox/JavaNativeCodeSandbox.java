package com.yupi.yuojcodesandbox;

import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * @author caiyu
 * @version 1.0
 * @description 实现模板类的java原生代码沙箱
 * @date 2023/12/13 20:37
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		System.out.println("这是实现模板类的java原生代码沙箱");
		return super.executeCode(executeCodeRequest);
	}
}

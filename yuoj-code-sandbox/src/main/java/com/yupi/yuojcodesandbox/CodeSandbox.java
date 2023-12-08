package com.yupi.yuojcodesandbox;


import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:31
 */
public interface CodeSandbox {
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
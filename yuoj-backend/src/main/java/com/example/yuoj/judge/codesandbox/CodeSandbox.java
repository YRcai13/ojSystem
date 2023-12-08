package com.example.yuoj.judge.codesandbox;

import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:31
 */
public interface CodeSandbox {
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
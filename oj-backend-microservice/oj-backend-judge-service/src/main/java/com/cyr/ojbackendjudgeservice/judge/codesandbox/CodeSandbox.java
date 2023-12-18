package com.cyr.ojbackendjudgeservice.judge.codesandbox;


import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:31
 */
public interface CodeSandbox {
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
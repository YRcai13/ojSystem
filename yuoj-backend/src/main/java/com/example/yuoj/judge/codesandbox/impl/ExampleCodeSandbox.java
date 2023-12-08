package com.example.yuoj.judge.codesandbox.impl;

import com.example.yuoj.judge.codesandbox.CodeSandbox;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.example.yuoj.judge.codesandbox.model.JudgeInfo;
import com.example.yuoj.model.enums.JudgeInfoMessageEnum;
import com.example.yuoj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:35
 */
public class ExampleCodeSandbox implements CodeSandbox {

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		List<String> inputList = executeCodeRequest.getInputList();

		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(inputList);
		executeCodeResponse.setMessage("测试执行成功");
		executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
		judgeInfo.setTime(100L);
		judgeInfo.setMemory(100L);
		executeCodeResponse.setJudgeInfo(judgeInfo);

		return executeCodeResponse;
	}

}

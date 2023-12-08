package com.example.yuoj.judge.startegy;

import cn.hutool.json.JSONUtil;
import com.example.yuoj.model.dto.question.JudgeCase;
import com.example.yuoj.model.dto.question.JudgeConfig;
import com.example.yuoj.judge.codesandbox.model.JudgeInfo;
import com.example.yuoj.model.entity.Question;
import com.example.yuoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 10:24
 */
public class DefaultJudgeStrategy implements JudgeStrategy{
	
	@Override
	public JudgeInfo doJudge(JudgeContext judgeContext) {
		JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
		List<String> outputList = judgeContext.getOutputList();
		List<String> inputList = judgeContext.getInputList();
		List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
		Question question = judgeContext.getQuestion();
		
		Long memory = judgeInfo.getMemory();
		Long time = judgeInfo.getTime();
		JudgeInfo judgeInfoResponse = new JudgeInfo();
		judgeInfoResponse.setTime(time);
		judgeInfoResponse.setMemory(memory);
		JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
		
		if (outputList.size() != inputList.size()) {
			judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
			judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
			return judgeInfoResponse;
		}
		for (int i = 0; i < judgeCaseList.size(); i++) {
			JudgeCase judgeCase = judgeCaseList.get(i);
			if (!judgeCase.getOutput().equals(outputList.get(i))) {
				judgeInfoMessageEnum = judgeInfoMessageEnum.WRONG_ANSWER;
				judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
				return judgeInfoResponse;
			}
		}
		String judgeConfigStr = question.getJudgeConfig();
		JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
		Long needMemoryLimit = judgeConfig.getMemoryLimit();
		Long needTimeLimit = judgeConfig.getTimeLimit();
		if (memory > needMemoryLimit) {
			judgeInfoMessageEnum = judgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED_EXCEEDED;
			judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
			return judgeInfoResponse;
		}
		if (time > needTimeLimit) {
			judgeInfoMessageEnum = judgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
			judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
			return judgeInfoResponse;
		}
		judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
		return judgeInfoResponse;
	}
}

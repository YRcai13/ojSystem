package com.cyr.ojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.cyr.ojbackendcommon.common.ErrorCode;
import com.cyr.ojbackendcommon.exception.BusinessException;
import com.cyr.ojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.cyr.ojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.cyr.ojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.cyr.ojbackendjudgeservice.judge.startegy.JudgeContext;
import com.cyr.ojbackendserviceclient.service.QuestionFeignClient;
import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.cyr.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.cyr.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.cyr.ojbackendmodel.model.dto.question.JudgeCase;
import com.cyr.ojbackendmodel.model.entity.Question;
import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;
import com.cyr.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 20:53
 */
@Service
public class JudgeServiceImpl implements JudgeService {

	@Resource
 	private QuestionFeignClient questionFeignClient;

 	
 	@Value("${codesandbox.type}")
 	private String type;

	@Override
	public QuestionSubmit doJudge(long questionSubmitId) {
		// 1、传入题目的提交id，获取到对应的题目、提交信息（包含代码、编程语言等）
		QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
		if (questionSubmit == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
		}
		Long questionId = questionSubmit.getQuestionId();
		Question question = questionFeignClient.getQuestionById(questionId);
		if (question == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
		}
		if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WATTING.getValue())) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
		}
		// 修改数据库中的判题状态为 RUNNING
		QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
		questionSubmitUpdate.setId(questionSubmitId);
		questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
		boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
		if (!update) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
		}
		
		// 2、调用沙箱，获取执行结果
		CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
		codeSandbox = new CodeSandboxProxy(codeSandbox);
		String judgeCaseStr = question.getJudgeCase();
		List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
		List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
		String language = questionSubmit.getLanguage();
		String code = questionSubmit.getCode();
		ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
				.code(code)
				.language(language)
				.inputList(inputList)
				.build();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		List<String> outputList = executeCodeResponse.getOutputList();
		
		// 3、根据沙箱的执行结果，设置题目的判题状态和信息
		JudgeContext judgeContext = new JudgeContext();
		judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
		judgeContext.setOutputList(outputList);
		judgeContext.setInputList(inputList);
		judgeContext.setJudgeCaseList(judgeCaseList);
		judgeContext.setQuestion(question);
		judgeContext.setQuestionSubmit(questionSubmit);
		
		JudgeManager judgeManager = new JudgeManager();
		JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
		
		// 4、修改数据库的判题结果
		questionSubmitUpdate = new QuestionSubmit();
		questionSubmitUpdate.setId(questionSubmitId);
		questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
		questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
		update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
		if (!update) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
		}
		QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
		return questionSubmitResult;
	}
}

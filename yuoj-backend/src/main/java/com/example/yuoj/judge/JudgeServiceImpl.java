package com.example.yuoj.judge;

import cn.hutool.json.JSONUtil;
import com.example.yuoj.common.ErrorCode;
import com.example.yuoj.exception.BusinessException;
import com.example.yuoj.judge.codesandbox.CodeSandbox;
import com.example.yuoj.judge.codesandbox.CodeSandboxFactory;
import com.example.yuoj.judge.codesandbox.CodeSandboxProxy;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.example.yuoj.judge.startegy.JudgeContext;
import com.example.yuoj.model.dto.question.JudgeCase;
import com.example.yuoj.judge.codesandbox.model.JudgeInfo;
import com.example.yuoj.model.entity.Question;
import com.example.yuoj.model.entity.QuestionSubmit;
import com.example.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.example.yuoj.service.QuestionService;
import com.example.yuoj.service.QuestionSubmitService;
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
 	private QuestionService questionService;
 	
	@Resource
 	private QuestionSubmitService questionSubmitService;
 	
 	@Value("${codesandbox.type}")
 	private String type;

	@Override
	public QuestionSubmit doJudge(long questionSubmitId) {
		// 1、传入题目的提交id，获取到对应的题目、提交信息（包含代码、编程语言等）
		QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
		if (questionSubmit == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
		}
		Long questionId = questionSubmit.getQuestionId();
		Question question = questionService.getById(questionId);
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
		boolean update = questionSubmitService.updateById(questionSubmitUpdate);
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
		update = questionSubmitService.updateById(questionSubmitUpdate);
		if (!update) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
		}
		QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
		return questionSubmitResult;
	}
}

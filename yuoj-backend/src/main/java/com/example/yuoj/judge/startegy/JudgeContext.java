package com.example.yuoj.judge.startegy;

import com.example.yuoj.model.dto.question.JudgeCase;
import com.example.yuoj.judge.codesandbox.model.JudgeInfo;
import com.example.yuoj.model.entity.Question;
import com.example.yuoj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 10:23
 */
@Data
public class JudgeContext {
	
	private JudgeInfo judgeInfo;
	
	private List<String> outputList;
	
	private List<String> inputList;
	
	private List<JudgeCase> judgeCaseList;
	
	private Question question;

	private QuestionSubmit questionSubmit;
	
}

package com.example.yuoj.judge;

import com.example.yuoj.judge.startegy.DefaultJudgeStrategy;
import com.example.yuoj.judge.startegy.JavaLanguageJudgeStrategy;
import com.example.yuoj.judge.startegy.JudgeContext;
import com.example.yuoj.judge.startegy.JudgeStrategy;
import com.example.yuoj.judge.codesandbox.model.JudgeInfo;
import com.example.yuoj.model.entity.QuestionSubmit;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 10:44
 */
public class JudgeManager {

	public JudgeInfo doJudge(JudgeContext judgeContext) {
		QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
		String language = questionSubmit.getLanguage();
		JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
		if ("java".equals(language)) {
			judgeStrategy = new JavaLanguageJudgeStrategy();
		}
		return judgeStrategy.doJudge(judgeContext);
	}

}

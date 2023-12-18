package com.cyr.ojbackendjudgeservice.judge;


import com.cyr.ojbackendjudgeservice.judge.startegy.DefaultJudgeStrategy;
import com.cyr.ojbackendjudgeservice.judge.startegy.JavaLanguageJudgeStrategy;
import com.cyr.ojbackendjudgeservice.judge.startegy.JudgeContext;
import com.cyr.ojbackendjudgeservice.judge.startegy.JudgeStrategy;
import com.cyr.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;

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

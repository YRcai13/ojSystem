package com.cyr.ojbackendjudgeservice.judge;


import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 20:50
 */
public interface JudgeService {
	public QuestionSubmit doJudge(long questionSubmitId);
}

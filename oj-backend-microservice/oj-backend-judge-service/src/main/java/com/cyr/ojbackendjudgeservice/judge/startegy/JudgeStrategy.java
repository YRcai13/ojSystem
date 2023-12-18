package com.cyr.ojbackendjudgeservice.judge.startegy;


import com.cyr.ojbackendmodel.model.codesandbox.JudgeInfo;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 10:22
 */
public interface JudgeStrategy {
	public JudgeInfo doJudge(JudgeContext judgeContext);
}

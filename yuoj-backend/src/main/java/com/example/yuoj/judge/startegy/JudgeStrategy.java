package com.example.yuoj.judge.startegy;

import com.example.yuoj.judge.codesandbox.model.JudgeInfo;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 10:22
 */
public interface JudgeStrategy {
	public JudgeInfo doJudge(JudgeContext judgeContext);
}

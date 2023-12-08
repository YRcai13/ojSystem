package com.example.yuoj.judge;

import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.example.yuoj.model.entity.QuestionSubmit;
import com.example.yuoj.model.vo.QuestionSubmitVO;
import org.springframework.stereotype.Service;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 20:50
 */
public interface JudgeService {
	public QuestionSubmit doJudge(long questionSubmitId);
}

package com.cyr.ojbackendjudgeservice.controller.inner;

import com.cyr.ojbackendjudgeservice.judge.JudgeService;
import com.cyr.ojbackendserviceclient.service.JudgeFeignClient;
import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 20:08
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

	@Resource
	private JudgeService judgeService;
	/**
	 * @description 判题
	 * @param questionSubmitId
	 * @return com.cyr.ojbackendmodel.model.entity.QuestionSubmit
	 * @author
	 * @date 2023/12/17 19:50
	 */
	@PostMapping("/do")
	public QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
		return judgeService.doJudge(questionSubmitId);
	}

}

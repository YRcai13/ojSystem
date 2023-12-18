package com.cyr.ojbackendquestionservice.controller.inner;

import com.cyr.ojbackendserviceclient.service.QuestionFeignClient;
import com.cyr.ojbackendmodel.model.entity.Question;
import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;
import com.cyr.ojbackendquestionservice.service.QuestionService;
import com.cyr.ojbackendquestionservice.service.QuestionSubmitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 20:05
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

	@Resource
	private QuestionService questionService;

	@Resource
	private QuestionSubmitService questionSubmitService;

	@GetMapping("/get/id")
	public Question getQuestionById(@RequestParam("questionId") long questionId) {
		return questionService.getById(questionId);
	}

	@GetMapping("/question_submit/get/id")
	public QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId) {
		return questionSubmitService.getById(questionSubmitId);
	}

	@PostMapping("/question_submit/update")
	public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
		return questionSubmitService.updateById(questionSubmit);
	}
}

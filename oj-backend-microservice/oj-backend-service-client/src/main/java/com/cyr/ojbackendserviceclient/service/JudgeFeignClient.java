package com.cyr.ojbackendserviceclient.service;


import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 20:50
 */
@FeignClient(name = "oj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
	/**
	 * @description 判题
	 * @param questionSubmitId
	 * @return com.cyr.ojbackendmodel.model.entity.QuestionSubmit
	 * @author
	 * @date 2023/12/17 19:50
	*/
	@PostMapping("/do")
	QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}

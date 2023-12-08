package com.example.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yuoj.model.dto.question.QuestionQueryRequest;
import com.example.yuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.example.yuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.example.yuoj.model.entity.Question;
import com.example.yuoj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yuoj.model.entity.User;
import com.example.yuoj.model.vo.QuestionSubmitVO;
import com.example.yuoj.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author cyr
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-12-05 22:09:04
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
	/**
	 * 题目提交
	 * @param questionSubmitAddRequest
	 * @param loginUser
	 * @return
	 */
	long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

	/**
	 * 获取查询条件
	 *
	 * @param questionSubmitQueryRequest
	 * @return
	 */
	QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

	/**
	 * 获取题目封装
	 *
	 * @param questionSubmit
	 * @param loginUser
	 * @return
	 */
	QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

	/**
	 * 分页获取题目封装
	 *
	 * @param questionSubmitPage
	 * @param loginUser
	 * @return
	 */
	Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);
}

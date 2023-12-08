package com.example.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yuoj.common.ErrorCode;
import com.example.yuoj.constant.CommonConstant;
import com.example.yuoj.exception.BusinessException;
import com.example.yuoj.judge.JudgeService;
import com.example.yuoj.model.dto.question.QuestionQueryRequest;
import com.example.yuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.example.yuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.example.yuoj.model.entity.Question;
import com.example.yuoj.model.entity.QuestionSubmit;
import com.example.yuoj.model.entity.QuestionSubmit;
import com.example.yuoj.model.entity.User;
import com.example.yuoj.model.enums.QuestionSubmitLanguageEnum;
import com.example.yuoj.model.enums.QuestionSubmitStatusEnum;
import com.example.yuoj.model.vo.QuestionSubmitVO;
import com.example.yuoj.model.vo.QuestionVO;
import com.example.yuoj.model.vo.UserVO;
import com.example.yuoj.service.QuestionService;
import com.example.yuoj.service.QuestionSubmitService;
import com.example.yuoj.service.QuestionSubmitService;
import com.example.yuoj.mapper.QuestionSubmitMapper;
import com.example.yuoj.service.UserService;
import com.example.yuoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author cyr
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-12-05 22:09:04
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
	@Resource
	private QuestionService questionService;

	@Resource
	private UserService userService;

	@Resource
	@Lazy
	private JudgeService judgeService;

	/**
	 * 提交题目
	 *
	 * @param questionSubmitAddRequest
	 * @param loginUser
	 * @return
	 */
	@Override
	public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
		// 判断编程语言是否合法
		String language = questionSubmitAddRequest.getLanguage();
		QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
		if (languageEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
		}
		Long questionId = questionSubmitAddRequest.getQuestionId();
		// 判断实体是否存在，根据类别获取实体
		Question question = questionService.getById(questionId);
		if (question == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		// 是否已提交题目
		long userId = loginUser.getId();
		// 每个用户串行提交题目
		QuestionSubmit questionSubmit = new QuestionSubmit();
		questionSubmit.setUserId(userId);
		questionSubmit.setQuestionId(questionId);
		questionSubmit.setCode(questionSubmitAddRequest.getCode());
		questionSubmit.setLanguage(language);
		// 设置初始状态
		questionSubmit.setStatus(QuestionSubmitStatusEnum.WATTING.getValue());
		questionSubmit.setJudgeInfo("{}");
		boolean save = this.save(questionSubmit);
		if (!save) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
		}
		Long questionSubmitId = questionSubmit.getId();
		// 执行判题服务
		CompletableFuture.runAsync(() -> {
			judgeService.doJudge(questionSubmitId);
		});
		return questionSubmitId;
	}

	/**
	 * 封装了事务的方法
	 *
	 * @param userId
	 * @param questionId
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public int doQuestionSubmitInner(long userId, long questionId) {
		QuestionSubmit questionSubmit = new QuestionSubmit();
		questionSubmit.setUserId(userId);
		questionSubmit.setQuestionId(questionId);
		QueryWrapper<QuestionSubmit> thumbQueryWrapper = new QueryWrapper<>(questionSubmit);
		QuestionSubmit oldQuestionSubmit = this.getOne(thumbQueryWrapper);
		boolean result;
		// 已提交
		if (oldQuestionSubmit != null) {
			result = this.remove(thumbQueryWrapper);
			if (result) {
				// 提交数 - 1
				result = questionService.update()
						.eq("id", questionId)
						.gt("thumbNum", 0)
						.setSql("thumbNum = thumbNum - 1")
						.update();
				return result ? -1 : 0;
			} else {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR);
			}
		} else {
			// 未提交
			result = this.save(questionSubmit);
			if (result) {
				// 提交数 + 1
				result = questionService.update()
						.eq("id", questionId)
						.setSql("thumbNum = thumbNum + 1")
						.update();
				return result ? 1 : 0;
			} else {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR);
			}
		}
	}

	/**
	 * 获取查询包装类
	 *
	 * @param questionSubmitQueryRequest
	 * @return
	 */
	@Override
	public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
		QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
		if (questionSubmitQueryRequest == null) {
			return queryWrapper;
		}
		String language = questionSubmitQueryRequest.getLanguage();
		Integer status = questionSubmitQueryRequest.getStatus();
		Long questionId = questionSubmitQueryRequest.getQuestionId();
		Long userId = questionSubmitQueryRequest.getUserId();
		String sortField = questionSubmitQueryRequest.getSortField();
		String sortOrder = questionSubmitQueryRequest.getSortOrder();

		// 拼接查询条件
		queryWrapper.like(StringUtils.isNotBlank(language), "language", language);
		queryWrapper.ne(ObjectUtils.isNotEmpty(QuestionSubmitStatusEnum.getEnumByValue(status)), "status", status);
		queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
//		queryWrapper.eq("idDelete", false);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
		return queryWrapper;

	}

	@Override
	public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
		QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
		Long userId = loginUser.getId();
		// 处理脱敏
		if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
			questionSubmitVO.setCode(null);
		}
		return questionSubmitVO;
	}

	@Override
	public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
		List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
		Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
		if (CollectionUtils.isEmpty(questionSubmitList)) {
			return questionSubmitVOPage;
		}
		List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser)).collect(Collectors.toList());
		questionSubmitVOPage.setRecords(questionSubmitVOList);
		return questionSubmitVOPage;
	}
}





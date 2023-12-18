package com.cyr.ojbackendmodel.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cyr.ojbackendmodel.model.codesandbox.JudgeInfo;
import com.cyr.ojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/6 12:54
 */
@Data
public class QuestionSubmitVO implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 编程语言
	 */
	private String language;

	/**
	 * 用户代码
	 */
	private String code;

	/**
	 * 判题信息（json 对象）
	 */
	private JudgeInfo judgeInfo;

	/**
	 * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
	 */
	private Integer status;

	/**
	 * 题目 id
	 */
	private Long questionId;

	/**
	 * 创建用户 id
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	/**
	 * 提交用户信息
	*/
	private UserVO userVO;
	
	/**
	 * 对应题目信息
	*/
	private QuestionVO questionVO;


	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
	/**
	 * 包装类转对象
	 *
	 * @param questionSubmitVO
	 * @return
	 */
	public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
		if (questionSubmitVO == null) {
			return null;
		}
		QuestionSubmit questionSubmit = new QuestionSubmit();
		BeanUtils.copyProperties(questionSubmitVO, questionSubmit);
		JudgeInfo judgeInfo = questionSubmitVO.getJudgeInfo();
		if (judgeInfo != null) {
			questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
		}
		return questionSubmit;
	}

	/**
	 * 对象转包装类
	 *
	 * @param questionSubmit
	 * @return
	 */
	public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
		if (questionSubmit == null) {
			return null;
		}
		QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
		BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
		String judgeInfo = questionSubmit.getJudgeInfo();
		questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfo,JudgeInfo.class));
		return questionSubmitVO;
	}

}

package com.cyr.ojbackendmodel.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cyr.ojbackendmodel.model.dto.question.JudgeConfig;
import com.cyr.ojbackendmodel.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/6 12:54
 */
@Data
public class QuestionVO {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 标签列表（json 数组）
	 */
	private List<String> tags;

	/**
	 * 题目提交数
	 */
	private Integer submitNum;

	/**
	 * 题目通过数
	 */
	private Integer acceptedNum;

	/**
	 * 判题配置（json 对象）
	 */
	private JudgeConfig judgeConfig;

	/**
	 * 点赞数
	 */
	private Integer thumbNum;

	/**
	 * 收藏数
	 */
	private Integer favourNum;

	/**
	 * 创建用户 id
	 */
	private Long userId;

	/**
	 * 创建用户信息
	*/
	private UserVO userVO;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 是否删除
	 */
	private Integer isDelete;


	/**
	 * 包装类转对象
	 *
	 * @param questionVO
	 * @return
	 */
	public static Question voToObj(QuestionVO questionVO) {
		if (questionVO == null) {
			return null;
		}
		Question question = new Question();
		BeanUtils.copyProperties(questionVO, question);
		List<String> tagList = questionVO.getTags();
		if (tagList != null) {
			question.setTags(JSONUtil.toJsonStr(tagList));
		}
		JudgeConfig judgeConfigVo = questionVO.getJudgeConfig();
		if (judgeConfigVo != null) {
			question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfigVo));
		}
		return question;
	}

	/**
	 * 对象转包装类
	 *
	 * @param question
	 * @return
	 */
	public static QuestionVO objToVo(Question question) {
		if (question == null) {
			return null;
		}
		QuestionVO questionVO = new QuestionVO();
		BeanUtils.copyProperties(question, questionVO);
		List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
		questionVO.setTags(tagList);
		String judgeConfig = question.getJudgeConfig();
		JudgeConfig bean = JSONUtil.toBean(judgeConfig, JudgeConfig.class);
		questionVO.setJudgeConfig(bean);
		return questionVO;
	}

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}

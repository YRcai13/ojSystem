package com.example.yuoj.model.dto.question;

import lombok.Data;

/**
 * @author cyr
 * @version 1.0
 * @description 题目配置信息
 * @date 2023/12/6 12:18
 */
@Data
public class JudgeConfig {

	/**
	 * 时间限制（ms）
	*/
	private Long timeLimit;
	
	/**
	 * 内存限制（KB）
	*/
	private Long memoryLimit;
	
	/**
	 * 堆栈限制（KB）
	*/
	private Long stackLimit;

}

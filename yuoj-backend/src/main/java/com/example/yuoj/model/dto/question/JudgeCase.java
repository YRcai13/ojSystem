package com.example.yuoj.model.dto.question;

import lombok.Data;

/**
 * @author cyr
 * @version 1.0
 * @description 题目用例信息
 * @date 2023/12/6 12:18
 */
@Data
public class JudgeCase {

	/**
	 * 输入用例
	*/
    private String input;
	/**
	 * 输出用例
	*/
    private String output;

}

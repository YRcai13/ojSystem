package com.example.yuoj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

	/**
	 * 输出列表
	*/
	private List<String> outputList;
	
	/**
	 * 执行信息
	*/
	private String message;

	/**
	 * 执行状态
	*/
	private Integer status;
	
	/**
	 * 判题信息
	*/
	private JudgeInfo judgeInfo;
}

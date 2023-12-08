package com.yupi.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:34
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

	/**
	 * 输入列表
	 */
	private List<String> inputList;

	/**
	 * 运行代码
	*/
	private String code;

	/**
	 * 运行语言
	*/
	private String language;

}

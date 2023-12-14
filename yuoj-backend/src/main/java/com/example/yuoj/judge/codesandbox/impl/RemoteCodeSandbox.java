package com.example.yuoj.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.example.yuoj.common.ErrorCode;
import com.example.yuoj.exception.BusinessException;
import com.example.yuoj.judge.codesandbox.CodeSandbox;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import org.apache.poi.util.StringUtil;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:35
 */
public class RemoteCodeSandbox implements CodeSandbox {

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		System.out.println("远程代码沙箱");
		String url = "http://192.168.101.133:8090/executeCode";
		String json = JSONUtil.toJsonStr(executeCodeRequest);
		String responseStr = HttpUtil.createPost(url)
				.body(json)
				.execute()
				.body();
		if (StringUtils.isBlank(responseStr)) {
			throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error");
		}
		return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
	}
 	
}

package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 16:13
 */
public class JavaNativeCodeSandbox implements CodeSandbox{

	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

		String userDir = System.getProperty("user.dir");
		String globalPath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
		if (!FileUtil.exist(globalPath)) {
			FileUtil.mkdir(globalPath);
		}

		String userCodePath = globalPath + File.separator + UUID.randomUUID() +File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
		return null;


	}

	public static void main(String[] args) {
		// 1、读取用户代码
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		String code = ResourceUtil.readStr("testCode.simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
		executeCodeRequest.setCode(code);
		executeCodeRequest.setLanguage("java");
		CodeSandbox codeSandbox = new JavaNativeCodeSandbox();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
	}
}

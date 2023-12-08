package com.example.yuoj.judge.codesandbox;

import com.example.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.example.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.example.yuoj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/7 19:48
 */
@SpringBootTest
public class CodeSandboxTest {

	@Value("${codesandbox.type}")
	private String type;

	@Test
	void executeCodeFactory() {
		CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
		String code = "int main() {}";
		String language = QuestionSubmitLanguageEnum.JAVA.getValue();
		List<String> inputList = Arrays.asList("1 2", "3 4");
		ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
				.code(code)
				.language(language)
				.inputList(inputList)
				.build();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		Assertions.assertNotNull(executeCodeResponse);
	}

	@Test
	void executeCodeProxy() {
		CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
		codeSandbox = new CodeSandboxProxy(codeSandbox);
		String code = "int main() {}";
		String language = QuestionSubmitLanguageEnum.JAVA.getValue();
		List<String> inputList = Arrays.asList("1 2", "3 4");
		ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
				.code(code)
				.language(language)
				.inputList(inputList)
				.build();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		Assertions.assertNotNull(executeCodeResponse);
	}
}

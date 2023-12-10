package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.security.MySecurityManager;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

	private static final long TIME_OUT = 10000L;

	private static final String SECURITY_MANAGER_PATH = "D:\\workplace\\OJ项目\\yuoj-code-sandbox\\src\\main\\resources\\security";

	private static final String SECURITY_MANAGER_NAME = "MySecurityManager";

	private static final List<String> blackList = Arrays.asList("Files", "exec");

	// 字典树结构
	private static final WordTree WORD_TREE;

	static {
		WORD_TREE = new WordTree();
		WORD_TREE.addWords(blackList);
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		// 1、读取用户代码
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

//		FoundWord foundWord = WORD_TREE.matchWord(code);
//		if (foundWord != null) {
//			System.out.println("包含禁止词" + foundWord.getFoundWord());
//			return null;
//		}

		String userDir = System.getProperty("user.dir");
		String globalPath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
		if (!FileUtil.exist(globalPath)) {
			FileUtil.mkdir(globalPath);
		}
		String userCodeParentPath = globalPath + File.separator + UUID.randomUUID();
		String userCodePath = userCodeParentPath +File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

		// 2、编译代码，得到 class 文件
		String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCmd);
			ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(executeMessage);
		} catch (Exception e) {
//			e.printStackTrace();
			return getErrorResponse(e);
		}

		// 3、执行代码，得到输出结果
//		System.setSecurityManager(new MySecurityManager());
		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_NAME,  inputArgs);
			try {
				Process runProcess = Runtime.getRuntime().exec(runCmd);
				new Thread(() -> {
					try {
						Thread.sleep(TIME_OUT);
						System.out.println("超时了，中断");
						runProcess.destroy();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
//				ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
				ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
				System.out.println(executeMessage);
				executeMessageList.add(executeMessage);
			} catch (IOException e) {
//				e.printStackTrace();
				return getErrorResponse(e);
			}
		}

		// 4、收集整理输出结果
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		long maxTime = 0;
		for (ExecuteMessage executeMessage : executeMessageList) {
			String errorMessage = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				executeCodeResponse.setStatus(3);
				break;
			}
			outputList.add(executeMessage.getMessage());
			Long time = executeMessage.getTime();
			if (time != null) {
				maxTime = Math.max(maxTime, time);
			}
		}
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		// todo:以后再做
//		judgeInfo.setMemory();
		executeCodeResponse.setJudgeInfo(judgeInfo);

		// 5、文件清理（是否可以定时清理？）
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
		}

		return executeCodeResponse;
	}


	// 6、错误处理，提升程序健壮性
	private ExecuteCodeResponse getErrorResponse(Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(new ArrayList<>());
		executeCodeResponse.setMessage(e.getMessage());
		executeCodeResponse.setStatus(2);
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}
	
	public static void main(String[] args) {
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3", "6 9"));
		executeCodeRequest.setCode(code);
		executeCodeRequest.setLanguage("java");
		CodeSandbox codeSandbox = new JavaNativeCodeSandbox();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
	}
}

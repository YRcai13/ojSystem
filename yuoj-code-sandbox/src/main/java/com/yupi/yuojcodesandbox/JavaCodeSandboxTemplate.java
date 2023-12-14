package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/13 20:09
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox{

	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	private static final long TIME_OUT = 5000L;

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();

		// 1、读取用户代码到 tmpCode 下，并创建一个随机文件
		File userCodeFile = saveCodeToFile(code);

		// 2、编译代码，得到 class 文件
		ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
		System.out.println(compileFileExecuteMessage);

		// 3、执行代码，得到输出结果
		List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

		// 4、收集整理输出结果
		ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

		// 5、文件清理（是否可以定时清理？）
		boolean b = deleteFile(userCodeFile);
		if (!b) {
			log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
		}

		return outputResponse;
	}

	/**
	 * @description 把用户的代码保存为文件
	 * @param code
	 * @return java.io.File
	 * @author
	 * @date 2023/12/13 20:16
	*/
	public File saveCodeToFile(String code) {
		String userDir = System.getProperty("user.dir");
		String globalPath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
		if (!FileUtil.exist(globalPath)) {
			FileUtil.mkdir(globalPath);
		}
		String userCodeParentPath = globalPath + File.separator + UUID.randomUUID();
		String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
		return userCodeFile;
	}

	/**
	 * @description 编译代码
	 * @param userCodeFile
	 * @return com.yupi.yuojcodesandbox.model.ExecuteMessage
	 * @author
	 * @date 2023/12/13 20:21
	*/
	public ExecuteMessage compileFile(File userCodeFile) {
		String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCmd);
			ExecuteMessage compileMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			if (compileMessage.getExitValue() != 0) {
				throw new RuntimeException("编译错误");
			}
			return compileMessage;
		} catch (Exception e) {
			System.out.println("编译错误");
			// 编译失败就没有必要执行后面的步骤了
			throw new RuntimeException(e);
		}
	}

	/**
	 * @description 运行代码文件，得到输出结果
	 * @param compiledFile 
	 * @param inputList 
	 * @return List<ExecuteMessage>
	 * @author 
	 * @date 2023/12/13 20:27
	*/
	public List<ExecuteMessage> runFile(File compiledFile, List<String> inputList) {
		String userCodeParentPath = compiledFile.getParentFile().getAbsolutePath();
		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
			String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath,  inputArgs);
			try {
				Process runProcess = Runtime.getRuntime().exec(runCmd);
				ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
				System.out.println(executeMessage);
				executeMessageList.add(executeMessage);
			} catch (Exception e) {
				throw new RuntimeException("执行错误" + e);
			}
		}
		return executeMessageList;
	}

	/**
	 * @description 获取输出结果
	 * @param executeMessageList
	 * @return com.yupi.yuojcodesandbox.model.ExecuteCodeResponse
	 * @author
	 * @date 2023/12/13 20:31
	*/
	public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
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
			long time = executeMessage.getTime();
			maxTime = Math.max(maxTime, time);
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
		return executeCodeResponse;
	}

	/**
	 * @description 文件清理
	 * @param userCodeFile
	 * @return boolean
	 * @author
	 * @date 2023/12/13 20:33
	*/
	public boolean deleteFile(File userCodeFile) {
		if (userCodeFile.getParentFile() != null) {
			String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
			return del;
		}
		return true;
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

}

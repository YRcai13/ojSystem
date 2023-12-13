package com.yupi.yuojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 16:13
 */
public class JavaDockerCodeSandbox implements CodeSandbox{

	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	private static final long TIME_OUT = 5000L;

	private static final String SECURITY_MANAGER_PATH = "D:\\workplace\\OJ项目\\yuoj-code-sandbox\\src\\main\\resources\\security";

	private static final String SECURITY_MANAGER_NAME = "MySecurityManager";

	private static Boolean FIRST_PULL = true;

	public static void main(String[] args) {
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//		String code = ResourceUtil.readStr("testCode/unsafeCode/SleepError.java", StandardCharsets.UTF_8);
		String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
		executeCodeRequest.setCode(code);
		executeCodeRequest.setLanguage("java");
		CodeSandbox codeSandbox = new JavaDockerCodeSandbox();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		// 1、读取用户代码
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();


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
			return getErrorResponse(e);
		}
		// 3、拉取镜像，创建容器，把文件复制到容器
		// 获取默认的docker client
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		String image = "openjdk:8-alpine";

		if (FIRST_PULL) {
			// 拉取镜像
			PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
			PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
				@Override
				public void onNext(PullResponseItem item) {
					System.out.println("下载镜像：" + item.getStatus());
					super.onNext(item);
				}
			};
			try {
				pullImageCmd
						.exec(pullImageResultCallback)
						.awaitCompletion();
			} catch (InterruptedException e) {
				System.out.println("拉取镜像失败");
				throw new RuntimeException(e);
			}
			System.out.println("下载成功");
			FIRST_PULL = false;
		}

		// 创建容器
		HostConfig hostConfig = new HostConfig();
		hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
		hostConfig.withMemory(100 * 1000 * 1000L);
		hostConfig.withCpuCount(1L);
		CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
		CreateContainerResponse createContainerResponse = containerCmd
				.withHostConfig(hostConfig)	// 挂载文件目录
				.withAttachStderr(true)		// 错误和输入输出流以及tty交互（代码沙箱不可能一次执行一个示例）
				.withAttachStdin(true)
				.withAttachStdout(true)
				.withTty(true)
				.exec();
		System.out.println(createContainerResponse);
		String containerId = createContainerResponse.getId();

		// 启动容器
		dockerClient.startContainerCmd(containerId).exec();

		List<ExecuteMessage> executeMessageList = new ArrayList<>();
		final String[] message = {null};
		final String[] errMessage = {null};
		// docker exec keen_shamir java -cp /app Main 1 4
		for (String inputArgs : inputList) {
			StopWatch stopWatch = new StopWatch();
			long time = 0L;
			String[] strings = inputArgs.split(" ");
			String[] cmdArray = ArrayUtil.append(new String[] {"java", "-cp", "/app", "Main"}, strings);
			ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
					.withCmd(cmdArray)
					.withAttachStderr(true)		// 错误和输入输出流以及tty交互（代码沙箱不可能一次执行一个示例）
					.withAttachStdin(true)
					.withAttachStdout(true)
					.exec();
			System.out.println("创建执行命令:" + execCreateCmdResponse);
			String execCreateCmdResponseId = execCreateCmdResponse.getId();
			ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
				@Override
				public void onNext(Frame frame) {
					StreamType streamType = frame.getStreamType();
					if (streamType.equals(StreamType.STDERR)) {
						errMessage[0] = new String(frame.getPayload());
						System.out.println("输出错误结果:" + new String(frame.getPayload()));
					} else {
						message[0] = new String(frame.getPayload());
						System.out.println("输出结果:" + new String(frame.getPayload()));
					}
					super.onNext(frame);
				}
			};

			// 获取内存
			final long[] maxMemory = {0L};
			StatsCmd statsCmd = dockerClient.statsCmd(containerId);
			ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
				@Override
				public void onStart(Closeable closeable) {

				}

				@Override
				public void onNext(Statistics object) {
					System.out.println("内存占用：" + object.getMemoryStats().getUsage());
					maxMemory[0] = Math.max(maxMemory[0], object.getMemoryStats().getUsage());
				}

				@Override
				public void onError(Throwable throwable) {

				}

				@Override
				public void onComplete() {

				}

				@Override
				public void close() throws IOException {

				}
			});
			statsCmd.exec(statisticsResultCallback);


			try {
				stopWatch.start();
				dockerClient.execStartCmd(execCreateCmdResponseId)
						.exec(execStartResultCallback)
						.awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
				stopWatch.stop();
				time = stopWatch.getLastTaskTimeMillis();
			} catch (InterruptedException e) {
				System.out.println("程序执行异常");
				throw new RuntimeException(e);
			}
			ExecuteMessage executeMessage = new ExecuteMessage();
			executeMessage.setTime(time);
			executeMessage.setMemory(maxMemory[0]);
			executeMessage.setMessage(message[0]);
			executeMessage.setErrorMessage(errMessage[0]);
			executeMessageList.add(executeMessage);
		}


		// 4、收集整理输出结果
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		long maxMemory = 0L;
		long maxTime = 0L;
		for (ExecuteMessage eMessage : executeMessageList) {
			String errorMessage = eMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				executeCodeResponse.setStatus(3);
				break;
			}
			outputList.add(eMessage.getMessage());
			long time = eMessage.getTime();
			maxTime = Math.max(maxTime, time);
			long memory = eMessage.getMemory();
			maxMemory = Math.max(maxMemory, memory);
		}
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		judgeInfo.setMemory(maxMemory);
		executeCodeResponse.setJudgeInfo(judgeInfo);


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

}

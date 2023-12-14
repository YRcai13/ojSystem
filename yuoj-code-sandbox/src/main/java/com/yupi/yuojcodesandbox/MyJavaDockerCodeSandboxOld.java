package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 16:13
 */
public class MyJavaDockerCodeSandboxOld implements CodeSandbox{

	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	private static final long LIMIT_MEMORY = 100 * 1000 * 1000L;

	private static final long LIMIT_TIME = 5000L;


	public static void main(String[] args) {
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
		executeCodeRequest.setCode(code);
		executeCodeRequest.setLanguage("java");
		CodeSandbox codeSandbox = new MyJavaDockerCodeSandboxOld();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();

		// 1、读取用户代码到 tmpCode 下，并创建一个随机文件
		String userDir = System.getProperty("user.dir");
		String globalPath = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
		if (!FileUtil.exist(globalPath)) {
			FileUtil.mkdir(globalPath);
		}
		String userCodeParentPath = globalPath + File.separator + UUID.randomUUID();
		String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

		// 2、编译代码，得到 class 文件
		String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCmd);
			ExecuteMessage compileMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(compileMessage);
			if (compileMessage.getExitValue() != 0) {
				throw new RuntimeException("编译错误");
			}
		} catch (Exception e) {
			System.out.println("编译错误");
			// 编译失败就没有必要执行后面的步骤了
			return getErrorResponse(e);
		}

		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		// 3、拉取镜像，创建容器 create，启动容器 start，进入容器执行命令 exec不会自动停止容器，attach会自动停止容器

		// 3.1、拉取镜像，判断镜像是否存在，存在则不操作
		String image = "openjdk:8-alpine";
		try {
			// 尝试检查镜像是否存在
			dockerClient.inspectImageCmd(image).exec();
			System.out.println("镜像存在");
		} catch (Exception e) {
			System.out.println("镜像不存在");
			PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
			PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
				@Override
				public void onNext(PullResponseItem item) {
					System.out.println("拉取镜像" + item.getStatus());
					super.onNext(item);
				}
			};
			try {
				pullImageCmd
						.exec(pullImageResultCallback)
						.awaitCompletion();
			} catch (InterruptedException ex) {
				System.out.println("拉取镜像失败");
                throw new RuntimeException(ex);
            }
            System.out.println("拉取镜像成功");
		}

		// 3.2、创建容器
		CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
		HostConfig hostConfig = new HostConfig();
		hostConfig.withMemory(LIMIT_MEMORY)
				.withCpuCount(1L)
				.withBinds(new Bind(userCodeParentPath, new Volume("/app")));
		CreateContainerResponse createContainerResponse = containerCmd
				.withHostConfig(hostConfig)
				.withTty(true)
				.withAttachStderr(true)
				.withAttachStdin(true)
				.withAttachStdout(true)
				.exec();
		System.out.println(createContainerResponse);
		String containerId = createContainerResponse.getId();

		// 3.3、启动容器
		dockerClient.startContainerCmd(containerId).exec();

		List<ExecuteMessage> executeMessageList = new ArrayList<>();

		// 3.4、进入容器执行命令
		for (String inputArgs : inputList) {
			final CountDownLatch latch = new CountDownLatch(1);
			String[] strings = inputArgs.split(" ");
			// 参考命令 docker exec keen_shamir java -cp /app Main 1 2
			String[] execCmd = ArrayUtil.append(new String[] {"java", "-cp", "/app", "Main"}, strings);	// 1 2 或1 3
			ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
					.withCmd(execCmd)
					.withAttachStderr(true)
					.withAttachStdin(true)
					.withAttachStdout(true)
					.withTty(true)
					.exec();

			ExecuteMessage executeMessage = new ExecuteMessage();
			final String[] message = {null};
			final String[] errMessage = {null};
			String execId = execCreateCmdResponse.getId();
			ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
				@Override
				public void onNext(Frame frame) {
					StreamType streamType = frame.getStreamType();
					if (streamType.equals(StreamType.STDERR)) {
						errMessage[0] = new String(frame.getPayload());
						System.out.println("==========输出错误结果为：" + errMessage[0]);
					} else {
						message[0] = new String(frame.getPayload());
						System.out.println("==========代码返回结果为：" + message[0]);
					}
					super.onNext(frame);
				}

				@Override
				public void onComplete() {
					System.out.println("==========回调结束");
					latch.countDown();
					super.onComplete();
				}
			};
			try {
				// todo:这里的计时器不靠谱，后面再改
				dockerClient.execStartCmd(execId)
						.exec(execStartResultCallback).
						awaitCompletion(LIMIT_TIME, TimeUnit.MICROSECONDS);
				System.out.println("=========程序运行完成");
			} catch (InterruptedException e) {
				System.out.println("程序执行异常" + e);
				return getErrorResponse(e);
			}

			try {
				System.out.println("await...");
				latch.await(); // 等待所有命令执行完成
				System.out.println("await===");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executeMessage.setTime(1000L);
			executeMessage.setMemory(1000L);
			executeMessage.setMessage(message[0]);
			System.out.println("===========赋值");
			executeMessage.setErrorMessage(errMessage[0]);
			executeMessageList.add(executeMessage);
		}
		// 关闭连接
		dockerClient.stopContainerCmd(containerId).exec();
		dockerClient.removeContainerCmd(containerId).exec();
		try {
			dockerClient.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// 4、整理输出结果
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		// 取用时最大值，便于判断是否超时
		long maxTime = 0;
		for (ExecuteMessage executeMessage : executeMessageList) {
			String errorMessage = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				// 用户提交的代码执行中存在错误
				executeCodeResponse.setStatus(3);
				break;
			}
			outputList.add(executeMessage.getMessage());
			Long time = executeMessage.getTime();
			if (time != null) {
				maxTime = Math.max(maxTime, time);
			}
		}
		// 正常运行完成
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		// 要借助第三方库来获取内存占用，非常麻烦，此处不做实现
//        judgeInfo.setMemory();
		executeCodeResponse.setJudgeInfo(judgeInfo);


		// 5、文件清理（是否可以定时清理？）
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
		}

		return executeCodeResponse;
	}

	// 错误处理，提升程序健壮性
	private ExecuteCodeResponse getErrorResponse(Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(new ArrayList<>());
		executeCodeResponse.setMessage(e.getMessage());
		executeCodeResponse.setStatus(2);
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}


}

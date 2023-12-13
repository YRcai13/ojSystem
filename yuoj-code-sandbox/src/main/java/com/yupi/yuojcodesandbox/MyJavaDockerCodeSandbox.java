package com.yupi.yuojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
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
public class MyJavaDockerCodeSandbox implements CodeSandbox{

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
		CodeSandbox codeSandbox = new MyJavaDockerCodeSandbox();
		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

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
		String compileCmd = "javac -encoding utf-8 " + userCodePath;
		try {
			Process compileProcess = Runtime.getRuntime().exec(compileCmd);
			ExecuteMessage compileMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(compileMessage);
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

		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		long maxTime = 0L;
		StopWatch stopWatch = new StopWatch();

		// 3.4、进入容器执行命令
		final CountDownLatch latch = new CountDownLatch(inputList.size());
		for (String inputArgs : inputList) {
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
			ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
				@Override
				public void onNext(Frame frame) {
					System.out.println("==========代码返回结果为：" + new String(frame.getPayload()));
					outputList.add(new String(frame.getPayload()));
					latch.countDown();
					super.onNext(frame);
				}
			};
			try {
				// todo:这里的计时器不靠谱，后面再改
				stopWatch.start();
				dockerClient.execStartCmd(execCreateCmdResponse.getId())
						.exec(execStartResultCallback).
						awaitCompletion(LIMIT_TIME, TimeUnit.MICROSECONDS);
				stopWatch.stop();
				long time = stopWatch.getLastTaskTimeMillis();
				maxTime = Math.max(maxTime, time);
				System.out.println("=========程序运行完成");
			} catch (InterruptedException e) {
				System.out.println("程序执行异常" + e);
				return getErrorResponse(e);
			}
		}
		try {
			System.out.println("await...");
			latch.await(); // 等待所有命令执行完成
			System.out.println("await===");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 4、整理输出结果
		// 最大运行时间、最大消耗内存
		for (String outputArgs : outputList) {
			System.out.println("=========整理输出结果");
			if (outputArgs.contains("Exception")) {
				executeCodeResponse.setStatus(3);
				executeCodeResponse.setMessage("运行异常" + outputArgs);
				break;
			}
			executeCodeResponse.setStatus(1);
			executeCodeResponse.setMessage("运行正常");
		}
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setTime(maxTime);
		// todo:这里的内存先设置默认值，之后再写
		judgeInfo.setMemory(1000L);
		executeCodeResponse.setOutputList(outputList);
		executeCodeResponse.setJudgeInfo(judgeInfo);


		dockerClient.stopContainerCmd(containerId).exec();
		dockerClient.removeContainerCmd(containerId).exec();
		try {
			dockerClient.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
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

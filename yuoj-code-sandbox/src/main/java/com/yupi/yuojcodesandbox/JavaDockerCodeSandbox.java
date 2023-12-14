package com.yupi.yuojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author caiyu
 * @version 1.0
 * @description 实现模板类的java docker代码沙箱
 * @date 2023/12/13 20:38
 */
@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

	private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	private static final long TIME_OUT = 5000L;
	private static Boolean FIRST_PULL = true;

	@Override
	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		System.out.println("这是实现模板类的java docker代码沙箱");
		return super.executeCode(executeCodeRequest);
	}

	@Override
	public List<ExecuteMessage> runFile(File compiledFile, List<String> inputList) {
		String userCodeParentPath = compiledFile.getParentFile().getAbsolutePath();
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

		final CountDownLatch latch = new CountDownLatch(inputList.size());
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
					latch.countDown();
					super.onNext(frame);
				}
			};

			// 获取内存
//			final long[] maxMemory = {0L};
//			StatsCmd statsCmd = dockerClient.statsCmd(containerId);
//			ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
//				@Override
//				public void onStart(Closeable closeable) {
//
//				}
//
//				@Override
//				public void onNext(Statistics object) {
//					System.out.println("内存占用：" + object.getMemoryStats().getUsage());
//					maxMemory[0] = Math.max(maxMemory[0], object.getMemoryStats().getUsage());
//				}
//
//				@Override
//				public void onError(Throwable throwable) {
//
//				}
//
//				@Override
//				public void onComplete() {
//
//				}
//
//				@Override
//				public void close() throws IOException {
//
//				}
//			});
//			statsCmd.exec(statisticsResultCallback);


			try {
				stopWatch.start();
				dockerClient.execStartCmd(execCreateCmdResponseId)
						.exec(execStartResultCallback)
						.awaitCompletion();
				stopWatch.stop();
				time = stopWatch.getLastTaskTimeMillis();
			} catch (InterruptedException e) {
				System.out.println("程序执行异常");
				throw new RuntimeException(e);
			}
			try {
				System.out.println("await...");
				latch.await(); // 等待所有命令执行完成
				System.out.println("await===");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ExecuteMessage executeMessage = new ExecuteMessage();
			executeMessage.setTime(time);
//			executeMessage.setMemory(maxMemory[0]);
			executeMessage.setMessage(message[0]);
			executeMessage.setErrorMessage(errMessage[0]);
			executeMessageList.add(executeMessage);
		}
		return executeMessageList;
	}
}

package com.yupi.yuojcodesandbox;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class MyJavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final long LIMIT_MEMORY = 100 * 1000 * 1000L;

    private static final long LIMIT_TIME = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    @Override
    public List<ExecuteMessage> runFile(File compiledFile, List<String> inputList) {
        String userCodeParentPath = compiledFile.getParentFile().getAbsolutePath();

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
                        awaitCompletion(LIMIT_TIME, TimeUnit.MILLISECONDS);
                System.out.println("=========程序运行完成");
                try {
                    System.out.println("await...");
                    latch.await(); // 等待所有命令执行完成
                    System.out.println("await===");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                System.out.println("程序执行异常" + e);
                throw new RuntimeException(e);
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
        return executeMessageList;
    }
}

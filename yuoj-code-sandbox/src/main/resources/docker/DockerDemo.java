
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/11 9:23
 */
public class DockerDemo {
	public static void main(String[] args) {
		// 获取默认的docker client
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		PingCmd pingCmd = dockerClient.pingCmd();
		pingCmd.exec();
	}
}

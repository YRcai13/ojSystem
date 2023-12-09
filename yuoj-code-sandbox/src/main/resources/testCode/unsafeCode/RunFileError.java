

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/9 11:07
 */
public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
		Process process = Runtime.getRuntime().exec(filePath);
		process.waitFor();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String compileOutputLine;
		while ((compileOutputLine = bufferedReader.readLine()) != null) {
			System.out.println(compileOutputLine);
		}
		System.out.println("执行木马程序成功");
	}
}

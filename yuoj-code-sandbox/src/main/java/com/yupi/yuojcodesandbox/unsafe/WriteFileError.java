package com.yupi.yuojcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/9 11:01
 */
public class WriteFileError {
	public static void main(String[] args) throws IOException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
		String errorProgram = "java -version 2>&1";
		Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
		System.out.println("gg");
	}
}

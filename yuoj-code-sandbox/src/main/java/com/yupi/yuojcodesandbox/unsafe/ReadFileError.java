package com.yupi.yuojcodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/9 10:53
 */
public class ReadFileError {

	public static void main(String[] args) throws IOException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/application.yml";
		List<String> allLines = Files.readAllLines(Paths.get(filePath));
		System.out.println(String.join("\n", allLines));

	}
}

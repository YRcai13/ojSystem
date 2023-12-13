package com.yupi.yuojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 19:49
 */
public class ProcessUtils {
	 public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
	 	 ExecuteMessage executeMessage = new ExecuteMessage();

		 try {
			 StopWatch stopWatch = new StopWatch();
			 stopWatch.start();
			 int exitVaule = runProcess.waitFor();
			 executeMessage.setExitValue(exitVaule);
			 if (exitVaule == 0) {	// 成功
				 System.out.println(opName + "成功");
				 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				 StringBuilder stringBuilder = new StringBuilder();
				 String line;
				 while ((line = bufferedReader.readLine()) != null) {
					 stringBuilder.append(line);
				 }
				 executeMessage.setMessage(stringBuilder.toString());
			 } else {	// 失败
				 System.out.println(opName + "失败");
				 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				 StringBuilder stringBuilder = new StringBuilder();
				 String line;
				 while ((line = bufferedReader.readLine()) != null) {
					 stringBuilder.append(line);
				 }
				 System.out.println(stringBuilder);
				 BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
				 StringBuilder errorStringBuilder = new StringBuilder();
				 String errorLine;
				 while ((errorLine = errorBufferedReader.readLine()) != null) {
					 errorStringBuilder.append(errorLine);
				 }
				 executeMessage.setErrorMessage(errorStringBuilder.toString());
			 }
			 stopWatch.stop();
			 executeMessage.setTime(stopWatch.getTotalTimeMillis());
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return executeMessage;
	 }


	public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName, String args) {
		ExecuteMessage executeMessage = new ExecuteMessage();

		try {
			InputStream inputStream = runProcess.getInputStream();
			OutputStream outputStream = runProcess.getOutputStream();

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			String[] s = args.split(" ");
			String join = StrUtil.join("\n", s) + "\n";
			outputStreamWriter.write(join);
			outputStreamWriter.flush();

			int exitVaule = runProcess.waitFor();
			executeMessage.setExitValue(exitVaule);
			if (exitVaule == 0) {	// 成功
				System.out.println(opName + "成功");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				executeMessage.setMessage(stringBuilder.toString());
			} else {	// 失败
				System.out.println(opName + "失败");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				System.out.println(stringBuilder);
				BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
				StringBuilder errorStringBuilder = new StringBuilder();
				String errorLine;
				while ((errorLine = errorBufferedReader.readLine()) != null) {
					errorStringBuilder.append(errorLine);
				}
				executeMessage.setErrorMessage(errorStringBuilder.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executeMessage;
	}
}

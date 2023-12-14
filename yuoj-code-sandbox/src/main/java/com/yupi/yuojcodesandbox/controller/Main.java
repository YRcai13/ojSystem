package com.yupi.yuojcodesandbox.controller;

import java.util.Date;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/8 16:17
 */
public class Main {
	public static void main(String[] args) {
//		int arg0 = Integer.parseInt(args[0]);
//		int arg1 = Integer.parseInt(args[1]);
		int arg0 = 1;
		int arg1 = 2;
		int num = arg0 + arg1;
		System.out.println("结果为:" + num);
		Date date = new Date();
		long time = date.getTime();
		System.out.println("time" + time);
	}
}
//public class Main {
//	public static void main(String[] args) {
//		Scanner scanner = new Scanner(System.in);
//		int i1 = scanner.nextInt();
//		int i2 = scanner.nextInt();
//		int num = i1 + i2;
//		System.out.println("结果为:" + num);
//	}
//}
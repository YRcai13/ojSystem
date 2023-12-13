import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		int arg0 = Integer.parseInt(args[0]);
		int arg1 = Integer.parseInt(args[1]);
//		int x = 1 / 0;
		int num = arg0 + arg1;
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.print(num);
	}
}
//public class Main {
//	public static void main(String[] args) {
//		Scanner scanner = new Scanner(System.in);
//		int i1 = scanner.nextInt();
//		int i2 = scanner.nextInt();
//		int num = i1 + i2;
//		System.out.println("交互结果为:" + num);
//	}
//}
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		int arg0 = Integer.parseInt(args[0]);
		int arg1 = Integer.parseInt(args[1]);
		int num = arg0 + arg1;
		System.out.println("结果为:" + num);
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
package com.yupi.yuojcodesandbox.unsafe;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/9 10:36
 */
public class SleepError {

    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡完了");
    }
}

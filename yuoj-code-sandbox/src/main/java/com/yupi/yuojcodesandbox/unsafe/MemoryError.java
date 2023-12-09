package com.yupi.yuojcodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/9 10:44
 */
public class MemoryError {
	public static void main(String[] args) {
		List<byte[]> bytes = new ArrayList<>();
		while (true) {
			bytes.add(new byte[1000]);
		}
	}
}

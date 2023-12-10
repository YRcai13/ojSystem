package com.yupi.yuojcodesandbox.security;

import java.security.Permission;

/**
 * @author cyr
 * @version 1.0
 * @description TODO
 * @date 2023/12/10 9:57
 */
public class MySecurityManager extends SecurityManager{
	@Override
	public void checkPermission(Permission perm) {
//		super.checkPermission(perm);
	}

	@Override
	public void checkExec(String cmd) {
		throw new SecurityException("权限异常：" + cmd);
	}
}

package com.obe.common.interceptor;

import java.util.HashMap;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
/**
 * 管理端权限拦截器并检查
 * @author Lizhenhao
 *
 */
public class AuthManagerInterceptor implements Interceptor{
 
	//@Override
	public void intercept(Invocation inv) {
		// TODO Auto-generated method stub
		System.out.println("before invoking");
		Controller c = inv.getController();
		HashMap<String, String> sessionInfo=c.getSessionAttr(c.getSession().getId());
		if (sessionInfo!=null&&(sessionInfo.get("type").equals("1")||sessionInfo.get("type").equals("2")||sessionInfo.get("type").equals("3")||sessionInfo.get("type").equals("4"))) {
			inv.invoke();
			System.out.println("after invoking");
		}
		else {
			c.renderText("{}");
		}
	}
}

package com.obe.index;

import java.util.HashMap;


import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.obe.auth.AuthController;
import com.obe.common.interceptor.AuthManagerInterceptor;


/**
 * IndexController
 */
public class IndexController extends Controller {
	public void index() {
		render("login.html");
	}
	
	public void findPwd() {
		render("findkeyword.html");
	}
	
	@Before(AuthManagerInterceptor.class)
	public void panel(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		setAttr("userInfo", AuthController.userInfo(sessionInfo));
		
		render("index.html");
	}
}






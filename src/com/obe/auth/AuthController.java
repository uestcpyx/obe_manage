package com.obe.auth;



import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.obe.common.interceptor.AuthManagerInterceptor;
import com.obe.common.tools.tools;

/**
 * IndexController
 */
public class AuthController extends Controller {
	public void index() {
		renderText("auth interface is running");
	}
	/**
	 * 用户登录
	 */
	public void userLogin()
	{
		
	}
	
	/**
	 * 监护站退出
	 */
	public void userLogout()
	{
		String sid=getSession().getId();
		getSession().removeAttribute(sid);//删除session
		
		renderText(tools.getResultJSON("1"));
		
	}
	
	/**
	 * 获得监护站基本信息（登录可用）
	 * @throws JSONException 
	 */
	@Before(AuthManagerInterceptor.class)
	public void stationInfo() throws JSONException
	{
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		String uid=sessionInfo.get("UID");//拿到监护站 ID
		String stationName=sessionInfo.get("stationName");//拿到监护站 ID
		JSONObject j=new JSONObject();
		j.put("stationId", uid);
		j.put("stationName", stationName);
		renderJson(j.toString());
		
	}
	 
}






package com.obe.auth;



import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Model;
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
		String sid="";
		String loginName=getPara("loginName");
		String password=getPara("password");
		String passwordDB="";
		if (loginName!=null&&!loginName.equals("")) {//查空
			 passwordDB=AuthDao.getPassword(loginName);//获取密码 
		}
		JSONObject jr=new JSONObject();
		if (password!=null&&!password.equals("")&&passwordDB!=null&&!passwordDB.equals("")&&password.equals(passwordDB)) {//成功
			//setSession
			sid=getSession().getId();
			Model<Model>user=AuthDao.getUserInfo(loginName);
			HashMap<String, String> sessionInfo=new HashMap<String, String>();
			sessionInfo.put("UID", user.getInt("id").toString());//保存id(通用命名，通过配合type来区分)
			sessionInfo.put("loginName", user.getStr("login_name"));//保存登录名
			sessionInfo.put("realName", user.getStr("real_name"));//保存真实姓名
			sessionInfo.put("type", user.getInt("user_type_id").toString());//保存session 类型
			getSession().setAttribute(sid, sessionInfo);//保存session
			//update login count and time
			AuthDao.updateLoginInfo(loginName, tools.getCurrentTime());
			try {
				jr.put("result", "1");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			System.out.println(sid);
		}else {//失败
			try {
				jr.put("result", "2");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		renderText(jr.toString());
	}
	
	/**
	 * 用户登出
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
//	@Before(AuthManagerInterceptor.class)
//	public void stationInfo() throws JSONException
//	{
//		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
//		String uid=sessionInfo.get("UID");//拿到监护站 ID
//		String stationName=sessionInfo.get("stationName");//拿到监护站 ID
//		JSONObject j=new JSONObject();
//		j.put("stationId", uid);
//		j.put("stationName", stationName);
//		renderJson(j.toString());
//		
//	}
	 
}






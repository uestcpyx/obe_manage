package com.obe.auth;




import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.obe.common.interceptor.AuthManagerInterceptor;
import com.obe.common.tools.Mail;
import com.obe.common.tools.tools;
import com.obe.jiaowuke.JiaowukeDao;

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
		redirect("/");
	}
	
	/**
	 * 找回密码
	 * @throws Exception 
	 */
	public void findPwd() throws Exception
	{
		String sid="";
		String eMail=getPara("eMail");
		String loginName="";
		if (eMail!=null&&!eMail.equals("")) {//查空
			 loginName=AuthDao.getUser(eMail);//获取密码 
		}
		JSONObject jr=new JSONObject();
		if (loginName!=null&&!loginName.equals("")) {//查找成功
			Model<Model>user=AuthDao.getUserInfo(loginName); //获取用户信息
			//重新设置密码
			String password = loginName + (int) (Math.random() * 10000);
			String msgContent = "亲爱的" + user.getStr("real_name") + "老师，您好，<br/><br/>"  
					+ "您在" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "提交找回密码的请求。<br/><br/>"  
					+ "以下是您的帐户及密码信息：<br/><br/>"  
					+ "用户名：" + loginName + "，密码：" + password + "<br/><br/>"  
					+ "该密码是临时密码，请您尽快修改密码，感谢使用本系统。" + "<br/><br/>"  
					+ "此为自动发送邮件，请勿直接回复！";  
			if(AuthDao.updatePasswordByNumber(loginName, tools.md5Encode(password))) {
				Mail.sendTextEmail(eMail, msgContent);
			}

			try {
				jr.put("result", "1");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			System.out.println(sid);
		}
		else {//失败
			try {
				jr.put("result", "2");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		renderText(jr.toString());
	}
	
	
	/**
	 * 获得用户基本信息（登录可用）
	 * @throws JSONException 
	 */
	@Before(AuthManagerInterceptor.class)
	public static Record userInfo(HashMap<String, String> sessionInfo)
	{
		String uId=sessionInfo.get("UID");//拿到用户 ID
		String userName=sessionInfo.get("realName");//拿到用户名
		String userType=sessionInfo.get("type");//拿到用户类型
		Record l = new Record().set("uId", uId).set("userName", userName).set("userType", userType);
		return l;
	}
	
	@Before(AuthManagerInterceptor.class)
	public void profile(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		setAttr("userInfo", AuthController.userInfo(sessionInfo));
		
		render("profile.html");
	}
	
	@Before(AuthManagerInterceptor.class)
	public void modifyPassword()
	{
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		String oldPass=getPara("oldPassword");
		String newPass=getPara("newPassword1");
		String oldPassInDB=AuthDao.getPassword(sessionInfo.get("loginName"));
		if (!oldPass.equals(oldPassInDB)) {
			renderHtml("<script>alert('原口令错误');history.go(-1);</script>");
		}else
		{
			AuthDao.updatePasswordByNumber(sessionInfo.get("loginName"), newPass);
			renderHtml("<script>alert('口令已修改，请重新登录');window.location.href='../index';</script>");
		}
	}
	 
}






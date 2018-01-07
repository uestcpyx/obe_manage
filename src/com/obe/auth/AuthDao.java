package com.obe.auth;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.obe.common.model.users;

public class AuthDao {
	/**
     * 获得用户密码
     */
    public static String getPassword(String loginName)
    {
    	Db db=new Db();
    	String sqlString="SELECT u.password from users u where u.login_name=?";
    	Record r = db.findFirst(sqlString, loginName);
    	if (r!=null) {
    		return r.getStr("password");
		}else
		{
			return "";
		}
    }
    
    /**
	 * 查找账户
	 */
    public static String getUser(String eMail) 
    {
    	Db db = new Db();
    	String sqlString = "SELECT u.login_name from users u where u.email=?";
    	Record r = db.findFirst(sqlString, eMail);
    	if (r!=null) {
    		return r.getStr("login_name");
		}else
		{
			return "";
		}
    }
    
    /**
	 * 获得用户基本信息
	 */
	public static Model<Model> getUserInfo(String loginName)
	{
		Model<Model> s=users.dao.findFirst("SELECT u.* from users u "
				+ "where u.login_name=?", loginName);
		return s;
	}
	
	/**
	 * 更新用户登录信息
	 */
	public static boolean updateLoginInfo(String loginName,String time)
	{
		@SuppressWarnings("unchecked")
		Model<Model> s= users.dao.findFirst("select * from users where login_name=?", loginName);
		s.set("login_count", s.getInt("login_count")+1).set("last_login_date", time);
		
		return s.update();
	}
	
	/**
	 * 更新密码
	 * @param loginName
	 * @return
	 */
	public static boolean updatePasswordByNumber(String loginName, String password)
	{
		boolean r= users.dao.findFirst("select * from users where login_name=?", loginName).set("password", password).update();
		return r;
	}
}

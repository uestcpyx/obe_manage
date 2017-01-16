package com.obe.common.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

//import com.jfinal.plugin.activerecord.Record;
//import com.sun.swing.internal.plaf.basic.resources.basic;
//import com.sun.tools.javac.resources.javac;
 

public class tools {
	/**
	 *  返回执行结果（用于仅返回一个 Json 字段的情况）
	 * @param code
	 * @return
	 */
	public static String getResultJSON(String code)
	{
		JSONObject j=new JSONObject();
		try {
			j.put("result", code);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return j.toString();
	}
	
	/**
	 * 计划分页数
	 * @param totalNumber
	 * @param numberInpage
	 * @return
	 */
	public static int calTotalPage(long totalNumber,int numberInpage)
	 {
		if (totalNumber==0) {
			return 1;
		}
		 int r=0;
		 double re=(double)totalNumber/(double)numberInpage;
		 r = (int) Math.ceil(re);
		 return r;
	 }
	
	 /**
	  * 将时间戳转换为人类可读的时间
	  * @param timeLong
	  * @return
	  */
	 public static String ConvertTimeLongToString(long timeLong)
	 {
		 String vv = "" + timeLong;
		long time = Long.valueOf(vv);
		 TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time);
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String s = dateformat.format(c.getTime());
		 return s;
	 }
	
	public static String getCurrentTime(){
		long timeLong=System.currentTimeMillis();
		String vv = "" + timeLong;
		long time = Long.valueOf(vv);
		 TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time);
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String s = dateformat.format(c.getTime());
		 return s.substring(0,19);
	}
	
	public static String getRandomString(int length) { // 
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }  
	public static void alertAndGoBack(HttpServletResponse response,String text){
		PrintWriter out;
		try {
			out = response.getWriter();
			out.print("<script>alert('"+text+"');</script>");
		     out.print("<script>history.go(-1)</script>"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void alertAndJump(HttpServletResponse response,String text,String url){
		PrintWriter out;
		try {
			out = response.getWriter();
			out.print("<script>alert('"+text+"');</script>");
		     out.print("<script>window.location.href='"+url+"';</script>"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void alert(HttpServletResponse response,String text){
		PrintWriter out;
		try {
			out = response.getWriter();
			out.print("<script>alert('"+text+"');</script>");
		     
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String string2MD5(String inStr){  
        MessageDigest md5 = null;  
        try{  
            md5 = MessageDigest.getInstance("MD5");  
        }catch (Exception e){  
            System.out.println(e.toString());  
            e.printStackTrace();  
            return "";  
        }  
        char[] charArray = inStr.toCharArray();  
        byte[] byteArray = new byte[charArray.length];  
  
        for (int i = 0; i < charArray.length; i++)  
            byteArray[i] = (byte) charArray[i];  
        byte[] md5Bytes = md5.digest(byteArray);  
        StringBuffer hexValue = new StringBuffer();  
        for (int i = 0; i < md5Bytes.length; i++){  
            int val = ((int) md5Bytes[i]) & 0xff;  
            if (val < 16)  
                hexValue.append("0");  
            hexValue.append(Integer.toHexString(val));  
        }  
        return hexValue.toString();  
  
    }  
	

	public static void main(String[] args) {
		
	}
}

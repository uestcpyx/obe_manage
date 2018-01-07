package com.obe.common.tools;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
    public static void sendTextEmail(String to,String content) throws Exception {   
   
    	
    	Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.163.com");
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");  
    	
    	// 创建Session实例对象   
        Session session = Session.getInstance(prop);   
//      session.setDebug(true);
           
        Message message = createSimpleMail(session,to,content);  
           
        // 获得Transport实例对象   
        Transport transport = session.getTransport();   
        // 打开连接   
        transport.connect("smtp.163.com","uestc_obeSystem","obe123");   
        // 发送邮件   
        transport.sendMessage(message, message.getAllRecipients());   
        // 关闭连接   
        transport.close();   
    } 
    
    public static MimeMessage createSimpleMail(Session session,String to,String content)throws Exception {
        //创建邮件对象
        MimeMessage message = new MimeMessage(session);
        //指明邮件的发件人
        message.setFrom(new InternetAddress("uestc_obeSystem@163.com"));
        //指明邮件的收件人
        message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
        //邮件的标题
        message.setSubject("OBE系统提示邮件");
        //邮件的文本内容
        message.setContent(content, "text/html;charset=UTF-8");
        //返回创建好的邮件对象
        return message;
    }
               
          
}

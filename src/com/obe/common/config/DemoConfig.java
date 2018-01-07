package com.obe.common.config;

import java.io.File;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.obe.auth.AuthController;
import com.obe.common.model._MappingKit;
import com.obe.common.model.rel_program_teacher;
import com.obe.common.model.teaching_program;
import com.obe.exam.ExamController;
import com.obe.index.IndexController;
import com.obe.jiaowuke.JiaowukeController;
import com.obe.program.ProgramController;
import com.obe.teacher.TeacherController;


/**
 * API引导式配置
 */
public class DemoConfig extends JFinalConfig {
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("dbConfig.txt");
		me.setDevMode(PropKit.getBoolean("devMode", false));
		
		String baseDownloadPath ="D:"+File.separator+"Program Files"+File.separator+"Apache Software Foundation"+
				File.separator+"webapps"+File.separator+"fileserver"+File.separator+"download";
		String baseUploadPath ="D:"+File.separator+"Program Files"+File.separator+"Apache Software Foundation"+
				File.separator+"webapps"+File.separator+"fileserver"+File.separator+"upload";
		me.setBaseDownloadPath(baseDownloadPath);
		me.setBaseUploadPath(baseUploadPath);
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", IndexController.class, "/AdminLTE-master");	// 第三个参数为该Controller的视图存放路径
		me.add("/auth", AuthController.class, "/AdminLTE-master");	
		me.add("/program", ProgramController.class, "/AdminLTE-master/pages/course");
		me.add("/jiaowuke", JiaowukeController.class, "/AdminLTE-master/pages/course");
		me.add("/teacher", TeacherController.class, "/AdminLTE-master/pages/course");
		me.add("/exam", ExamController.class, "/AdminLTE-master/pages/exam");
	}
	
	public static C3p0Plugin createC3p0Plugin() {
		return new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置C3p0数据库连接池插件
		C3p0Plugin C3p0Plugin = createC3p0Plugin();
		me.add(C3p0Plugin);
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(C3p0Plugin);
		me.add(arp);
		
		// 所有配置在 MappingKit 中搞定
		_MappingKit.mapping(arp);
		arp.addMapping("rel_program_teacher", rel_program_teacher.class);
		arp.addMapping("teaching_program", teaching_program.class);
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		
	}
	
	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("WebRoot", 80, "/", 5);
	}
}

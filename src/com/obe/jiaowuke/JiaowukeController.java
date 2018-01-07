package com.obe.jiaowuke;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.obe.auth.AuthController;
import com.obe.common.interceptor.AuthManagerInterceptor;
import com.obe.common.tools.Mail;
import com.obe.common.tools.tools;
import com.obe.exam.ExamService;
import com.obe.program.ProgramDao;
import com.obe.teacher.TeacherDao;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class JiaowukeController extends Controller {	
	//查看现有课程
	@Before(AuthManagerInterceptor.class)
	public void showAllCourse(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		String sql = "select t.id, t.code, t.cn_name, u.real_name, t.`status`, t.course_group_id from teaching_program t "
				+ "left join (users u LEFT JOIN rel_program_teacher r ON u.id = r.user_id) "
				+ "ON t.id = r.teaching_program_id where r.master_flag  =1";
		List<Record> courses = Db.find(sql);
		for(int i =0; i<courses.size(); i++){
			courses.get(i).set("course_group", JiaowukeDao.getCourseGroup(courses.get(i).getInt("course_group_id")).get("course_group_name"));
			courses.get(i).set("edition", JiaowukeDao.getEdition(courses.get(i).getInt("id")).get("edition"));
		}
		setAttr("course_list", courses);
		render("showAllCourse.html");
	}

	//教务科添加课程跳转
	@Before(AuthManagerInterceptor.class)
	public void addUser(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		setAttr("groupName",ProgramDao.getCourseGroup());
		render("addCourse.html");
	}

	//教务科添加课程
	@Before(AuthManagerInterceptor.class)
	public void addCourseController() throws Exception{
		Integer gId = getParaToInt("teacher_select");
		String code = getPara("teaching_program.code");
		String cn_name = getPara("teaching_program.cn_name");
		Integer status = getParaToInt("teaching_program.status");
		String edition = getPara("teaching_program.year");
		Long code_count = Db.queryLong("select count(*) from teaching_program where code=? && edition=?",code,edition);
		Integer oldversionId = getParaToInt("oldversionId");
		
		if(code_count>=1){
			renderHtml("<script>alert('该课程已被添加');history.go(-1);</script>");
			return;
		}
		
		Record master_id=Db.findFirst("select master_id from mb_course_group where id=?", gId);
		Record users = Db.findById("users", master_id.getInt("master_id")).set("user_type_id","3");
		String currentTime = tools.getCurrentTime();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date currentDate = sdf.parse(currentTime);
		
		Long course_count = Db.queryLong("select count(*) from courses where code =?", code);		
		Integer courseId;
		if(course_count==0){
			Record courses = new Record().set("code", code).set("course_name", cn_name);
			Db.save("courses", courses);
			Long Id = courses.get("id");
			courseId = Integer.parseInt(Id.toString());
		}
		else{
			courseId = Db.findFirst("select id from courses where code =?", code).get("id");
		}
			
		Record teaching_program = new Record().set("course_id", courseId).set("code",code).set("cn_name", cn_name).set("status", status).set("course_group_id", gId).set("create_date", currentDate).set("update_date", currentDate).set("edition", edition);
		if(oldversionId!=null){
			Record info = JiaowukeDao.getOldVersionInfo(oldversionId);
			teaching_program.set("credit", info.get("credit")).set("hours", info.get("hours")).set("course_type_id", info.get("course_type_id")).set("major_id", info.get("major_id")).set("intro", info.get("intro")).set("teach_plan", info.get("teach_plan")).set("books", info.get("books")).set("references", info.get("references"));	
		}
		Db.update("users",users);
		
		if(Db.save("teaching_program", teaching_program)){
			Mail.sendTextEmail(JiaowukeDao.getUserEmail(gId), "您已分配到新的课程大纲，请登陆http://www.is.uestc.edu.cn/obe/尽快填写!");
			renderHtml("<script>alert('添加成功');window.location.href='./showAllCourse';</script>");
		}
		else{
			renderHtml("<script>alert('添加失败');history.back(-1);</script>");
		}
		Record teaching_program_id=Db.findFirst("select id from teaching_program where code=? && edition=?", code,edition);
		Record rel_program_teacher = new Record().set("user_id",master_id.getInt("master_id")).set("master_flag", 1).set("teaching_program_id", teaching_program_id.getInt("id")).set("leader_flag", 0);
		Db.save("rel_program_teacher",rel_program_teacher);
	}

	
	@Before(AuthManagerInterceptor.class)
	public void checkCourse() throws Exception{
		Integer pId = getParaToInt("pId");
		String submit =getPara("submit");
		if(submit.equals("in")){
			Record teaching_program=Db.findById("teaching_program",pId).set("status", 3);
			Db.update("teaching_program",teaching_program);
			Mail.sendTextEmail(JiaowukeDao.getTechersEmail(pId), "您有新的课程大纲信息更新，请登陆OBE系统http://www.is.uestc.edu.cn/obe/查看!");
			renderHtml("<script>alert('审核成功');window.location.href='./showAllCourse';</script>");		
		}
		else if(submit.equals("out")){
			String suggestion =getPara("suggestion");
			Record teaching_program=Db.findById("teaching_program",pId).set("status", 0);
			Db.update("teaching_program",teaching_program);
			Mail.sendTextEmail(JiaowukeDao.getTechersEmail(pId), "很抱歉，您所提交的课程大纲未通过审核，以下为审核人的修改意见，请您参考: “"+ suggestion + "”");
			renderHtml("<script>alert('驳回成功');window.location.href='./showAllCourse';</script>");	
		}
	}
	
	@Before(AuthManagerInterceptor.class)
	public void delCourse(){		
		Integer tpId = getParaToInt("pId");
		
		if(JiaowukeDao.DeleteCourse(tpId)){
			renderHtml("<script>alert('删除成功');window.location.href='./showAllCourse';</script>");
		}
		else{
			renderHtml("<script>alert('删除失败');history.back(-1);</script>");
		}
	}

	//检查有无旧版本
	@Before(AuthManagerInterceptor.class)
	public void checkOldVersion(){
		String code = getPara("code");
		List<Record> records = JiaowukeDao.getOldVersion(code);
		setAttr("status", "success");
		setAttr("intro", records);
		renderJson();
	}
	
	/*
	 * 根据信息生成大纲
	 */
	public void exportWord() throws IOException, TemplateException{
		Integer pId = getParaToInt("pId");
		
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		
		List<Record> courseName = ProgramDao.getCourseName(pId);
		String cn_name = courseName.get(0).getStr("cn_name").toString();
		dataMap.put("cn_name", cn_name);
		String en_name = ProgramDao.getEnName(cn_name);
		if(en_name!=null&&en_name.length()>0){
			dataMap.put("en_name",en_name);
		}
		String courseNumber = ProgramDao.getCourseNumber(cn_name);
		if(courseNumber!=null&&courseNumber.length()>0){
			dataMap.put("courseNumber",courseNumber);
		}
		Integer hours = ProgramDao.getHours(cn_name);
		if(hours!=null){
			dataMap.put("hours",hours.toString());
		}
		Integer credit = ProgramDao.getCredit(cn_name);
		if(credit!=null){
			dataMap.put("credit",credit.toString());
		}
		if(ProgramDao.getCourseTypeSelected(pId)!=0){
			String course_type = TeacherDao.getCourseType(ProgramDao.getCourseTypeSelected(pId));
			if(course_type!=null&&course_type.length()>0){
				dataMap.put("course_type",course_type);
			}
		}
		String major = ProgramDao.getMajorNameAll().get(0).get("name");
		if(major != null && major.length()>0){
			dataMap.put("major",major);
		}
		
		//先修课程
		List<Record> pre_teaching_program = TeacherDao.getPreTeachingProgram(ProgramDao.getPreTeachingProgramSelected(pId));
		if(pre_teaching_program!=null&&pre_teaching_program.size()!=0){
			dataMap.put("pre_teaching_program",pre_teaching_program);
		}
		Integer status = ProgramDao.getTpStatus(pId);
		if(status!=null){
			dataMap.put("status",status);
		}
		String intro = ProgramDao.getIntro(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", 
				"</w:t></w:r></w:p>"
				+ "<w:p wsp:rsidR=\"00E353B4\" wsp:rsidRPr=\"00B26055\" wsp:rsidRDefault=\"006553A1\" wsp:rsidP=\"00E353B4\">\r\n"
				+ "<w:pPr>\r\n" 
				+ "<w:spacing w:line=\"360\" w:line-rule=\"auto\"/>\r\n" 
				+ "<w:ind w:first-line-chars=\"250\" w:first-line=\"525\"/>\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:rFonts w:ascii=\"宋体\" w:h-ansi=\"宋体\"/>\r\n" 
				+ "<wx:font wx:val=\"宋体\"/>\r\n"
				+ "</w:rPr>\r\n"
				+ "</w:pPr>\r\n"
				+ "<w:r>\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:rFonts w:ascii=\"宋体\" w:h-ansi=\"宋体\"/>\r\n"
				+ "<wx:font wx:val=\"宋体\"/>\r\n" 
				+ "</w:rPr>\r\n" 
				+ "<w:tab/>\r\n" 
				+ "<w:t>"
				);
		if(intro!=null&&intro.length()>0){
			dataMap.put("intro",intro);
		}
		String teach_plan = ProgramDao.getTeachPlan(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", 
				"</w:t></w:r></w:p>"
				+ "<w:p wsp:rsidR=\"00E353B4\" wsp:rsidRPr=\"00DD4C3F\" wsp:rsidRDefault=\"00E2188A\" wsp:rsidP=\"00D9117F\">\r\n" 
				+ "<w:pPr>\r\n" 
				+ "<w:pStyle w:val=\"a7\"/>\r\n" 
				+ "<w:spacing w:before=\"0\" w:before-autospacing=\"off\" w:after=\"0\" w:after-autospacing=\"off\" w:line=\"360\" w:line-rule=\"auto\"/>\r\n" 
				+ "<w:ind w:left=\"420\" w:first-line-chars=\"200\" w:first-line=\"420\"/>\r\n" 
				+ "<w:jc w:val=\"both\"/>\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:rFonts w:ascii=\"等线\" w:fareast=\"等线\" w:h-ansi=\"等线\" w:cs=\"仿宋_GB2312\"/>\r\n"
				+ "<wx:font wx:val=\"等线\"/>\r\n" 
				+ "<w:b/>\r\n" 
				+ "<w:sz w:val=\"21\"/>\r\n" 
				+ "<w:sz-cs w:val=\"21\"/>\r\n" 
				+ "</w:rPr>\r\n" 
				+ "</w:pPr>\r\n" 
				+ "<w:r wsp:rsidRPr=\"00DD4C3F\">\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:rFonts w:ascii=\"等线\" w:fareast=\"等线\" w:h-ansi=\"等线\" w:hint=\"fareast\"/>\r\n" 
				+ "<wx:font wx:val=\"等线\"/>\r\n" 
				+ "<w:sz w:val=\"21\"/>\r\n" 
				+ "<w:sz-cs w:val=\"21\"/>\r\n" 
				+ "</w:rPr>\r\n" 
				+ "<w:t>"
				);
		if(teach_plan!=null&&teach_plan.length()>0){
			dataMap.put("teach_plan",teach_plan);
		}
		String books = ProgramDao.getBooks(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", 
				"</w:t></w:r></w:p>"
				+ "<w:p wsp:rsidR=\"00E353B4\" wsp:rsidRPr=\"00EF3A32\" wsp:rsidRDefault=\"003B0FAD\" wsp:rsidP=\"003B0FAD\">\r\n" 
				+ "<w:pPr>\r\n"
				+ "<w:spacing w:line=\"300\" w:line-rule=\"auto\"/>\r\n"
				+ "<w:ind w:left=\"202\" w:first-line=\"424\"/>\r\n"
				+ "</w:pPr>\r\n"
				+ "<w:r>\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:sz-cs w:val=\"20\"/>\r\n" 
				+ "</w:rPr>\r\n" 
				+ "<w:tab/>\r\n"
				+ "<w:t>");
		if(books!=null&&books.length()>0){
			dataMap.put("books",books);
		}
		String references = ProgramDao.getReferences(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", 
				"</w:t></w:r></w:p>"
				+ "<w:p wsp:rsidR=\"00E353B4\" wsp:rsidRPr=\"00EF3A32\" wsp:rsidRDefault=\"003B0FAD\" wsp:rsidP=\"003B0FAD\">\r\n" 
				+ "<w:pPr>\r\n"
				+ "<w:spacing w:line=\"300\" w:line-rule=\"auto\"/>\r\n"
				+ "<w:ind w:left=\"202\" w:first-line=\"424\"/>\r\n"
				+ "</w:pPr>\r\n"
				+ "<w:r>\r\n" 
				+ "<w:rPr>\r\n" 
				+ "<w:sz-cs w:val=\"20\"/>\r\n" 
				+ "</w:rPr>\r\n" 
				+ "<w:tab/>\r\n"
				+ "<w:t>");
		if(references!=null&&references.length()>0){
			dataMap.put("references",references);
		}	
		
		List<Record> cRList = ProgramDao.getCReach(pId);
		if(cRList != null){
			for(int i =0; i<cRList.size(); i++){
				cRList.get(i).set("evaluate_type", TeacherDao.getEvaluateType(cRList.get(i).getInt("evaluate_type_id")));
				cRList.get(i).set("cm_List", TeacherDao.getCmList(cRList.get(i).getInt("id"), pId));
			}
			dataMap.put("cRList", cRList);
		}
		
		List<Record> gRList = ProgramDao.getGReach(pId);
		if(gRList != null){
			for(int i =0; i<gRList.size(); i++){
				gRList.get(i).set("evaluate_type", TeacherDao.getEvaluateType(gRList.get(i).getInt("evaluate_type_id")));
				gRList.get(i).set("grNumber", TeacherDao.getGrNumber(gRList.get(i).getInt("gr_id")));
				gRList.get(i).set("gr_cm_List", TeacherDao.getGrCmList(gRList.get(i).getInt("id"), pId));
			}
			dataMap.put("gList", gRList);
		}
		
		//考核方式
		List<Record> eList = ExamService.getCMandETypeByTpId(pId);
		dataMap.put("eList", eList);
		
		dataMap.put("Date", TeacherDao.getDate(pId));
		dataMap.put("cm_intro",TeacherDao.getCmIntro(pId));
		dataMap.put("co_intro",TeacherDao.getCoIntro(pId));
		dataMap.put("poGrCoCm",TeacherDao.getPoGrCoCm(pId));
		
		//Configuration用于读取ftl文件  
	    Configuration configuration = new Configuration();  
	    configuration.setDefaultEncoding("utf-8");
		
	    //设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，  
	    //这里我们的模板是放在com.havenliu.document.template包下面  
	    configuration.setClassForTemplateLoading(this.getClass(), "/com/obe/common/tools");  
	    Template t=null;  
	    try {  
	       //test.ftl为要装载的模板  
	    	t = configuration.getTemplate("test.ftl");  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	    //输出文档路径及名称  
	    File outFile = new File("../《"+cn_name+"》课程大纲.doc");  
	    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"), 10240);  
        t.process(dataMap, out); 
        out.close();  
	    renderFile(new File("../《"+cn_name+"》课程大纲.doc"));
	}
	
	
}

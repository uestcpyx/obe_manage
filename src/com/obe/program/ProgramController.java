package com.obe.program;



import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.obe.auth.AuthController;
import com.obe.common.interceptor.AuthManagerInterceptor;
import com.obe.common.tools.Mail;
import com.obe.common.tools.tools;

public class ProgramController extends Controller {
	
	//查看我的大纲
	@Before(AuthManagerInterceptor.class)
	public void myCourse(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		String sql = "select t.id, t.cn_name, t.`status`, t.create_date from teaching_program t "
				+ "left join (users u LEFT JOIN rel_program_teacher r ON u.id = r.user_id) "
				+ "ON t.id = r.teaching_program_id where r.master_flag  =1 && r.user_id =?";
		List<Record> courses = Db.find(sql,userInfo.get("uId")); 
		setAttr("course_list", courses);
		render("myCourseList.html");
	}
	
	//查看我的课程：教师
	@Before(AuthManagerInterceptor.class)
	public void myLesson(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
			
		String sql = "select t.id, t.cn_name, t.`status` from teaching_program t "
				+ "left join (users u LEFT JOIN rel_program_teacher r ON u.id = r.user_id) "
				+ "ON t.id = r.teaching_program_id where r.master_flag  =0 && r.user_id =?";
		List<Record> courses = Db.find(sql,userInfo.get("uId")); 
		setAttr("lesson_list", courses);
		render("myLessonList.html");
	}	

	/**
	 * 网页端添加课程基本信息
	 */
	public void setBasicInformation(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer pId = getParaToInt("pId");
		List<Record> courseName = ProgramDao.getCourseName(pId);
		setAttr("courseName", courseName);
		setAttr("pId",pId);
		String cn_name = courseName.get(0).getStr("cn_name").toString();
		
		setAttr("courseNumber", ProgramDao.getCourseNumber(cn_name)); //获取课程代码
		setAttr("course_type", ProgramDao.getCourseTypeAll());
		setAttr("major", ProgramDao.getMajorNameAll());
		List<Record> preteaching_program = ProgramDao.getPreTeachingProgramAll(ProgramDao.getCourseNumber(cn_name));
		setAttr("pre_teaching_program", preteaching_program);
		
		List<Record> preTechProSelected = ProgramDao.getPreTeachingProgramSelected(pId);;
		if(preTechProSelected!=null && preTechProSelected.size()>0){
			setAttr("pre_tech_selected",preTechProSelected);
		}
		String en_name = ProgramDao.getEnName(cn_name);
		if(en_name!=null&&en_name.length()>0){
			setAttr("en_name",en_name);
		}
		Integer hours = ProgramDao.getHours(cn_name);
		if(hours!=null){
			setAttr("hours",hours);
		}
		Integer credit = ProgramDao.getCredit(cn_name);
		if(credit!=null){
			setAttr("credit",credit);
		}
		
		render("setBasicInformation.html");
	}
	
	/**
	 * 加载预设数据
	 */
	public void dataRequest(){
		
		Integer pId = getParaToInt("pId");
		Integer courseTypeSelected = ProgramDao.getCourseTypeSelected(pId);
		
		if(courseTypeSelected!=null){
			setAttr("course_type_selected",courseTypeSelected);
		}
		
		
		String intro = ProgramDao.getIntro(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", "\r");
		if(intro!=null&&intro.length()>0){
			setAttr("intro",intro);
		}
		String teach_plan = ProgramDao.getTeachPlan(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", "\r");
		if(teach_plan!=null&&teach_plan.length()>0){
			setAttr("teach_plan",teach_plan);
		}
		String books = ProgramDao.getBooks(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", "\r");
		if(books!=null&&books.length()>0){
			setAttr("books",books);
		}
		String references = ProgramDao.getReferences(pId).replaceAll("&nbsp", " ").replaceAll("<br/>", "\r");
		if(references!=null&&references.length()>0){
			setAttr("references",references);
		}
		
		renderJson();
	}
	
	
	/**
	 * 添加课程基本信息到数据库
	 * @throws ParseException 
	 */
	public void addBasicInformation() throws ParseException{
		Integer pId = getParaToInt("pId");
		Integer add_status = 1; //添加数据库记录状态，保存成功为1，失败为0
		String courseName = getPara("courseName");
		String en_name = getPara("en_name");
		Integer hours = getParaToInt("hours");
		Integer credit = getParaToInt("credit");
		Integer course_type_id = getParaToInt("course_type");
		Integer major_id = getParaToInt("major");
		String[] pre_teaching_program = getParaValues("pre_teaching_program");
		String intro = getPara("intro").replaceAll(" ", "&nbsp").replaceAll("\r", "<br/>");
		String teach_plan = getPara("teach_plan").replaceAll(" ", "&nbsp").replaceAll("\r", "<br/>");
		String books = getPara("books").replaceAll(" ", "&nbsp").replaceAll("\r", "<br/>");
		String references = getPara("references").replaceAll(" ", "&nbsp").replaceAll("\r", "<br/>");
		
		String currentTime = tools.getCurrentTime();
		//SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//java.util.Date currentDate = sdf.parse(currentTime);
		//插入数据库
		ProgramDao.saveProgram(courseName, en_name, hours, credit, course_type_id, major_id, pre_teaching_program, intro, teach_plan, books, references, currentTime);
		
		Integer submit = getParaToInt("submit");
		//如果是教务科提交的，就跳转会myCourse页面
		if(submit==1){
			if(add_status==1){
				renderHtml("<script>alert('保存成功');window.location.href='../jiaowuke/showAllCourse';</script>");
			}
			else{
				renderHtml("<script>alert('保存失败');history.back(-1);</script>");
			}
		}
		else{
			if(add_status==1){
				renderHtml("<script>alert('保存成功，请继续完成CO和CM关联设置');window.location.href='./getCOandCM?pId="+pId+"';</script>");
			}
			else{
				renderHtml("<script>alert('保存失败');history.back(-1);</script>");
			}
		}
	}
	
	/*
	 * 打开添加CO与CMs设置页面
	 */
	@Before(AuthManagerInterceptor.class)
	public void getCOandCM(){
		//获取用户信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("pId");
		List<Record> coList = ProgramDao.getCO(tpId);
		List<Record> cmList = ProgramDao.getCM(tpId);
		if(coList!=null)
		{
			setAttr("coList", coList);
			setAttr("coListNum", coList.size());
		}
		if(cmList!=null)
		{
			setAttr("cmList", cmList);
			setAttr("cmListNum", cmList.size());
		}
		List<Record> poList = ProgramDao.getPO();
		List<Record> grList = ProgramDao.getGR();

	 	setAttr("poList", poList);
		setAttr("grList", grList);

		List<Record> relList = ProgramDao.getRel(tpId);
		if(relList!=null){
			setAttr("relList", relList);
			setAttr("relListNum", relList.size());
		}
		setAttr("pId", tpId);
		render("COandCM_set.html");
	}
	/*
	 * 添加CO与CM设置
	 */
	@Before(AuthManagerInterceptor.class)
	public void submitCOandCM_set(){
		Integer tpId = getParaToInt("pId");
		
		Integer status = 1; //添加指标关联状态，添加成功为1，失败为0
		
		if(!ProgramDao.delOldRel(tpId)||!ProgramDao.delOldCM(tpId)||!ProgramDao.delOldCO(tpId)){
			status = 0;
		}
		
		//添加CO
		Integer CO_num = getParaToInt("CO_num");
		Integer[] COorder = getParaValuesToInt("COorder");
		String[] COname = getParaValues("COname");
		String[] COintro = getParaValues("COintro");
		for(int i=0;i<CO_num;i++)
		{
			ProgramDao.saveCO(tpId, COorder[i], COname[i], COintro[i]);
		}

		//添加CM
		Integer CM_num = getParaToInt("CM_num");
		Integer[] CMorder = getParaValuesToInt("CMorder");
		String[] CMname = getParaValues("CMname");
		String[] CMintro = getParaValues("CMintro");
		for(int i=0;i<CM_num;i++)
		{
			ProgramDao.saveCM(tpId, CMorder[i], CMname[i], CMintro[i]);
		}
		
		//添加指标关联关系
		Integer rel_num = getParaToInt("rel_num");
		String[] POList = getParaValues("PO");
		String[] GRList = getParaValues("GR");
		String[] COList = getParaValues("CO");
		String[] CMList = getParaValues("CM");
				
		//添加指标关联
		for(int i=0; i<rel_num; i++)
		{
			System.out.println(i);
			if(ProgramDao.saveRel(tpId, POList[i], GRList[i], COList[i], CMList[i]))
			{
				System.out.println("添加成功");
			}
			else{
				System.out.println("添加失败");
				status = 0;
			}
		}
		
		String currentTime = tools.getCurrentTime();
		Record teaching_program=Db.findById("teaching_program",tpId).set("update_date",currentTime);
		Db.update("teaching_program",teaching_program);
		
		if(status==1){
			renderHtml("<script>alert('保存成功，请继续完成达成度评价设置');window.location.href='./getEvaluate?pId="+tpId+"';</script>");
		}
		else{
			renderHtml("<script>alert('保存失败');history.back(-1);</script>");
		}	
		
			
	}
	
	/*
	 * 打开添加课程组成员页面
	 */
	@Before(AuthManagerInterceptor.class)
	public void getTeachers(){
		//获得用户信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("pId");
		List<Record> tList = ProgramDao.getTeacher();
		setAttr("tList", tList);
		List<Record> tsList = ProgramDao.getTeacherSelected(tpId);
		if(tsList!=null){
			setAttr("tsList", tsList);
		}
		if(ProgramDao.getTLeader(tpId)!=null){
			Integer tLeader = ProgramDao.getTLeader(tpId).getInt("user_id");
			setAttr("tLeader", tLeader);
		}
		setAttr("pId", tpId);
		render("teaching_group.html");
	}
	/*
	 * 添加课程组成员
	 */
	@Before(AuthManagerInterceptor.class)
	public void submitTeaching_Group() throws Exception{
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		String uId=sessionInfo.get("UID");//拿到用户 ID
		Integer tpId = getParaToInt("pId");
		
		Integer teacher_status =  1; //添加课程组老师状态，成功为1，失败为0

		if(!ProgramDao.delOldTeacher(tpId)){
			System.out.println("删除失败");
			teacher_status = 0;
		}
		
		String[] tList = getParaValues("addTeacher");
		String tLeader = getPara("addTeacherLeader");
		
		String master_flag ="0";
		for(int i=0; i<tList.length;i++)
		{
			if(!tList[i].equals(ProgramDao.getTMaster(tpId).get("user_id").toString())){
				if(!ProgramDao.saveTeachers(tList[i], master_flag, tpId))
				{
					System.out.println("添加失败");
					teacher_status = 0;
				}
			}
		}
		
		//如果没有接收到课程负责人的信息，代表修改信息的是课程负责人自己
		if(tLeader==null){
			tLeader = uId;
		}
		if(!ProgramDao.saveTLeader(tLeader, tpId)){
			teacher_status = 0;
		}
		
		String currentTime = tools.getCurrentTime();
		Record teaching_program=Db.findById("teaching_program",tpId).set("update_date",currentTime);
		Db.update("teaching_program",teaching_program);
		
	
		if(!tLeader.equals(uId)){
			if(teacher_status==1){
				Mail.sendTextEmail(ProgramDao.getEmail(ProgramDao.getTLeader(tpId).getInt("user_id")), "您已分配到新的课程大纲，请登陆http://www.is.uestc.edu.cn/obe/尽快填写!");
				renderHtml("<script>alert('提交成功！请通知课程负责人协助您继续完成课程大纲的填写！您可不用继续完成后续内容！');window.location.href='./setBasicInformation?pId="+tpId+"';</script>");
			}
			else{
				renderHtml("<script>alert('保存失败');history.back(-1);</script>");
			}
		}
		else {
			if(teacher_status==1){
				renderHtml("<script>alert('提交成功！请继续完成课程基本信息的填写');window.location.href='./setBasicInformation?pId="+tpId+"';</script>");
			}
			else{
				renderHtml("<script>alert('保存失败');history.back(-1);</script>");
			}
		}
	}
	
	public void getEvaluate(){
		//获得用户信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("pId");
		setAttr("pId", tpId);
		
		List<Record> eList = ProgramDao.getEvaluateType();
		List<Record> gr_idList = ProgramDao.getGRID(tpId);
		
		List<Record> grList = new ArrayList<Record>();
		for(int i=0; i<gr_idList.size();i++){
			grList.addAll(ProgramDao.getGRId(gr_idList.get(i).getInt("gr_id")));
		}
		setAttr("eList", eList);
		setAttr("grList", grList);
		
		setAttr("coList",ProgramDao.getCO(tpId)); //获得CO列表
		setAttr("co_cmList",ProgramDao.getCM(tpId)); //获得CM模块
		
		List<Record> cRList = ProgramDao.getCReach(tpId);
		if(cRList != null){
			setAttr("cRList", cRList);
			setAttr("cRList_num", cRList.size());
		}
		
		List<Record> co_cm_List = ProgramDao.getCOEvaCMId(tpId);
		if(co_cm_List!=null){
			setAttr("co_cm_List",co_cm_List);
		}
		
		List<Record> gRList = ProgramDao.getGReach(tpId);
		if(gRList != null){
			setAttr("gList", gRList);
			setAttr("gList_num", gRList.size());
		}
		
		List<Record> gr_cm_List = ProgramDao.getGREvaCMId(tpId);
		if(gr_cm_List!=null){
			setAttr("gr_cm_List",gr_cm_List);
		}
		
		render("evaluate_set.html");
	}
	
	public void postEvaluate(){
		Integer tpId = getParaToInt("pId");
		String submit = getPara("submit");
		
		Integer co_num = getParaToInt("Cr_num");
		String[] CReach = getParaValues("CReach");
		String[] CeType = getParaValues("CeType");
		String[] CsType = getParaValues("CsType");
		
		String[] CWeight = getParaValues("CWeight");
		String[] CrWeight = getParaValues("CrWeight");
		
		//删除旧的数据
		ProgramDao.delRelCoEva(tpId);
		ProgramDao.delRelCoEvaCM(tpId);
		ProgramDao.delRelGrEva(tpId);
		ProgramDao.delRelGREvaCM(tpId);
		
		for(int i=0; i<co_num; i++)
		{
			String[] CeModel = getParaValues("CeModel"+(i+1));
			if(ProgramDao.saveRelCoEva(tpId, CReach[i], CeType[i], CsType[i], CWeight[i], CrWeight[i], CeModel)){
				System.out.println("添加成功");
			}
			else{
				System.out.println("添加失败");
			}
		}
		
		
		Integer Gr_num = getParaToInt("Gr_num");
		String[] eId = getParaValues("GeType");
		String[] GRList = getParaValues("GReach");
		String[] Weight = getParaValues("GWeight"); 
		String[] GrWeight = getParaValues("GrWeight");
		for(int i=0; i<Gr_num; i++)
		{
			ProgramDao.saveGR(tpId,GRList[i],eId[i],Weight[i],GrWeight[i]);
		    String[] CMList = getParaValues("GeModel"+(i+1));
		    for(int m=0; m<CMList.length; m++){
		    	ProgramDao.saveGR_CM(tpId,CMList[m],GRList[i],eId[i]);
		    }
		}
		
		String currentTime = tools.getCurrentTime();
		Record teaching_program=Db.findById("teaching_program",tpId).set("update_date",currentTime);
		Db.update("teaching_program",teaching_program);
		
		if(submit.equals("submit")){
			if(Db.findFirst("select course_type_id from teaching_program where id =?", tpId).getInt("course_type_id")==0){
				renderHtml("<script>alert('您在前面页面有关键项未填写，请填写完后再提交'); history.go(-1);</script>");
			}
			else{
				if(ProgramDao.shenghe(tpId)){
					renderHtml("<script>alert('提交成功');window.location.href='./myCourse';</script>");
				}
			}
		}
		else{
			if(submit.equals("submit_2")){
				if(ProgramDao.submit2(tpId)){
					renderHtml("<script>alert('提交成功');window.location.href='../teacher/lessonList';</script>");
				}
			}
			else{
				renderHtml("<script>alert('保存成功');window.location.href='./getEvaluate?pId="+tpId+"';</script>");
			}			
		}
	}

	public void reference(){
		Integer id = getParaToInt("id");
		if(id==1){
			render("reference1.html");
		}
		else{
			render("reference2.html");
		}
	}
}

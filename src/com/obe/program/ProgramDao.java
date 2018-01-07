package com.obe.program;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ProgramDao {

	public static List<Record> getTeacher(){
		List<Record> t = Db.find("select id, real_name from users where id<148");
		return t;
	}
	
	public static List<Record> getCourseGroup(){
		List<Record> g = Db.find("select id, course_group_name from mb_course_group");
		return g;
	}
	
	public static List<Record> getTeacherSelected(Integer tpId){
		List<Record> t = Db.find("select user_id from rel_program_teacher where teaching_program_id =?", tpId);
		return t;
	}
	
	public static Record getTLeader(Integer tpId){
		Record test = Db.findFirst("select user_id from rel_program_teacher where teaching_program_id =? && leader_flag = 1", tpId);		
		return test;
	}
	
	//获取课程组长id
	public static Record getTMaster(Integer tpId){
		Record test = Db.findFirst("select user_id from rel_program_teacher where teaching_program_id =? && master_flag = 1", tpId);
		return test;
	}
	//删除旧的课程组数据
	public static boolean delOldTeacher(Integer tpId){
		boolean status = true;
		List<Record> t = Db.find("select id from rel_program_teacher where teaching_program_id =? && master_flag = 0", tpId);
		for(int i=0; i<t.size();i++){
			if(!Db.deleteById("rel_program_teacher", t.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//填充新的课程组数据
	public static boolean saveTeachers(String uId, String master_flag, Integer pId)
	{
		Record pr_teacher = new Record().set("user_id", uId).set("master_flag", master_flag).set("leader_flag", 0).set("teaching_program_id", pId);
		return Db.save("rel_program_teacher", pr_teacher);
	}
	//存入课程组组长信息
	public static boolean saveTLeader(String uId, Integer pId)
	{
		Record tleader = Db.findById("rel_program_teacher", "user_id, teaching_program_id", uId, pId).set("leader_flag", 1);
		return Db.update("rel_program_teacher", tleader);
	}
	
	public static List<Record> getPO(){
		List<Record> p = Db.find("select level1, content from mb_po");
		return p;
	}
	
	public static List<Record> getGR(){
		List<Record> g = Db.find("select level1, content from mb_gr where level2 = 0");
		return g;
	}
	
	public static List<Record> getCO(Integer tpId){
		List<Record> co = Db.find("select co_order, co_name, co_intro from teaching_program_co where teaching_program_id = ?", tpId);
		return co;
	}
	
	public static List<Record> getCM(Integer tpId){
		List<Record> cm = Db.find("select cm_order, cm_name, cm_intro from teaching_program_cm where teaching_program_id = ?", tpId);
		return cm;
	}
	
	public static List<Record> getRel(Integer tpId){
		List<Record> rel = Db.find("select cm_id, co_id, gr_id, po_id from rel_cm_co where teaching_program_id = ?", tpId);
		return rel;
	}
	
	public static int getTpId(String uId){
		Record temp = Db.findFirst("select * from rel_program_teacher where user_id = ?",uId);
		Integer tpId = temp.getInt("teaching_program_id");
		return tpId;
	}


	//删除旧的CO数据
	public static boolean delOldCO(Integer tpId){
		boolean status = true;
		List<Record> co = Db.find("select id from teaching_program_co where teaching_program_id = ?", tpId);
		for(int i = 0;i<co.size();i++){
			if(!Db.deleteById("teaching_program_co", co.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//存储新的CO数据
	public static boolean saveCO(Integer pId, Integer order, String name, String intro)
	{
			Record co =new Record().set("teaching_program_id", pId).set("co_order", order).set("co_name", name).set("co_intro", intro);
			return Db.save("teaching_program_co", co);
	}
	//删除旧的CM数据
	public static boolean delOldCM(Integer tpId){
		boolean status = true;
		List<Record> cm = Db.find("select id from teaching_program_cm where teaching_program_id = ?", tpId);
		for(int i = 0;i<cm.size();i++){
			if(!Db.deleteById("teaching_program_cm", cm.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//存储新的CM数据
	public static boolean saveCM(Integer pId, Integer order, String name, String intro)
	{
			Record cm =new Record().set("teaching_program_id", pId).set("cm_order", order).set("cm_name", name).set("cm_intro", intro);
			return Db.save("teaching_program_cm", cm);	
	}
	//删除旧的指标关联关系
	public static boolean delOldRel(Integer tpId){
		boolean status = true;
		List<Record> t = Db.find("select id from rel_cm_co where teaching_program_id =?", tpId);
		for(int i=0; i<t.size();i++){
			if(!Db.deleteById("rel_cm_co", t.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//存储新的指标关联关系
	public static boolean saveRel(Integer pId, String PO, String GR, String CO, String CM)
	{
		Record cm_co = new Record().set("cm_id", CM).set("co_id", CO).set("gr_id", GR).set("po_id", PO).set("teaching_program_id", pId);		
		return Db.save("rel_cm_co", cm_co);
	}
	
	//提交课程组长
	public static boolean submit2(Integer tpId){
		Record s = Db.findById("teaching_program", tpId).set("status", 1);
		return Db.update("teaching_program", s);
	}
		
	//提交审核
	public static boolean shenghe(Integer tpId){
		Record s = Db.findById("teaching_program", tpId).set("status", 2);
		return Db.update("teaching_program", s);
	}
		
	//获得课程中文名
	public static List<Record> getCourseName(Integer pId){
		return Db.find("select cn_name from teaching_program where id = ?", pId);
	}
		
	//获得已选课程代码
	public static String getCourseNumber(String courseName){
		Record courseNumber = Db.findFirst("select * from teaching_program where cn_name = ?",courseName);
		return courseNumber.getStr("code");
	}
		
	//获得已选课程英文名
	public static String getEnName(String courseName){
		Record en_name = Db.findFirst("select * from teaching_program where cn_name = ?",courseName);
		return en_name.getStr("en_name");
	}
		
	//获得已选课程学时
	public static Integer getHours(String courseName){
		Record hours = Db.findFirst("select * from teaching_program where cn_name = ?",courseName);
		return hours.getInt("hours");
	}
		
	//获得已选课程学分
	public static Integer getCredit(String courseName){
		Record credit = Db.findFirst("select * from teaching_program where cn_name = ?",courseName);
		return credit.getInt("credit");
	}
		
	//获得已选课程简介
	public static String getIntro(Integer pId){
		Record intro = Db.findFirst("select * from teaching_program where id = ?",pId);
		return intro.getStr("intro");
	}
		
	//获得已选课程教学计划
	public static String getTeachPlan(Integer pId){
		Record teach_plan = Db.findFirst("select * from teaching_program where id = ?",pId);
		return teach_plan.getStr("teach_plan");
	}
		
	//获得已选课程教材资料
	public static String getBooks(Integer pId){
		Record books = Db.findFirst("select * from teaching_program where id = ?",pId);
		return books.getStr("books");		
	}		
		
	//获得已选课程参考资料
	public static String getReferences(Integer pId){
		Record references = Db.findFirst("select * from teaching_program where id = ?",pId);
		return references.getStr("references");
	}
		
	//获得课程类型
	public static List<Record> getCourseTypeAll(){
		return Db.find("select * from mb_course_type");
	}
		
	//获得已选课程类型ID
	public static Integer getCourseTypeSelected(Integer pId){
		Integer courseTypeId = Db.findFirst("select * from teaching_program where id = ?",pId).getInt("course_type_id");
		return courseTypeId;
	}
	
	//获得适用专业类型
	public static List<Record> getMajorNameAll(){
		return Db.find("select * from mb_major");
	}
	
	//获得先修课程清单
	public static List<Record> getPreTeachingProgramAll(String code){
		return Db.find("select * from courses where code NOT IN(SELECT code from courses where code = ?)", code);
	}
	
	//获得已选先修课程ID
	public static List<Record> getPreTeachingProgramSelected(Integer pId){
		List<Record> preRequirement = Db.find("select * from rel_pre_requirement where teaching_program_id = ?",pId);
		return preRequirement;
	}

	//获得课程状态
	public static Integer getTpStatus(Integer tpId){
		return Db.findFirst("select `status` from teaching_program WHERE id = ?", tpId).getInt("status");
	}
	//数据插入数据库
	public static void saveProgram(String courseName,String en_name,Integer hours,Integer credit,Integer course_type_id,Integer major_id,String[] pre_teaching_program,String intro,String teach_plan,String books,String references, String time){
		Integer tpId = (Db.findFirst("select * from teaching_program where cn_name = ?",courseName)).getInt("id");
		Record tech_pro = Db.findById("teaching_program", tpId).set("en_name", en_name).set("hours", hours).set("credit", credit).set("course_type_id", course_type_id).set("major_id", major_id).set("intro", intro).set("teach_plan", teach_plan).set("books", books).set("references", references).set("update_date", time);
		Db.update("teaching_program", tech_pro);
		List<Record> temp = Db.find("select id from rel_pre_requirement where teaching_program_id = ?",tpId);
		if(temp != null){
			for(int j=0;j<temp.size();j++){
				Db.deleteById("rel_pre_requirement", temp.get(j).get("id"));
			}
		}
		if(!pre_teaching_program[0].equals(0)){
			for(int i=0;i<pre_teaching_program.length;i++){
				Record rel_pre_req = new Record().set("teaching_program_id",tpId).set("pre_teaching_program_id", pre_teaching_program[i]);
				Db.save("rel_pre_requirement", rel_pre_req);
			}
		}
	}
	
	public static List<Record> getEvaluateType(){
		List<Record> e = Db.find("select * from mb_evaluate_type");
		return e;
	}
	
	public static List<Record> getGRId(Integer tpId){
		List<Record> gr = Db.find("select * from mb_gr where level1= ? && level2 != 0",tpId);
		return gr;
	}
	
	//
	public static boolean delRelGrEva(Integer tpId){
		boolean status = true;
		List<Record> temp = Db.find("select * from rel_gr_evaluate where teaching_program_id = ?", tpId);
		for(int i = 0;i<temp.size();i++){
			if(!Db.deleteById("rel_gr_evaluate", temp.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//指标点达成度设置
	public static boolean saveGR(Integer pId, String gr_id, String e_id, String Weight, String GrWeight){
		Record rel_gr_evaluate = new Record().set("teaching_program_id",pId).set("gr_id", gr_id).set("evaluate_type_id", e_id).set("weight", Weight).set("reach_weight", GrWeight);
		return Db.save("rel_gr_evaluate", rel_gr_evaluate);
	}
	
	//
	public static boolean delRelGREvaCM(Integer tpId){
		boolean status = true;
		List<Record> temp = Db.find("select * from rel_gr_evaluate_cm where teaching_program_id = ?", tpId);
		for(int i = 0;i<temp.size();i++){
			if(!Db.deleteById("rel_gr_evaluate_cm", temp.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	//指标点与cm关系表设置
	public static boolean saveGR_CM(Integer pId, String CMList, String GRList, String GeModel){
		Record teaching_program_cm_id=Db.findFirst("select cm_order from teaching_program_cm where teaching_program_id=? and cm_order=?", pId,CMList);
    	Record rel_gr_evaluate_id=Db.findFirst("select id from rel_gr_evaluate "
    			+ "where gr_id =? && teaching_program_id = ? && evaluate_type_id=?", GRList, pId, GeModel);
    	Record rel_gr_evaluate_cm = new Record().set("rel_gr_evaluate_id",rel_gr_evaluate_id.get("id")).set("teaching_program_cm_order", teaching_program_cm_id.get("cm_order")).set("teaching_program_id", pId);
    	return Db.save("rel_gr_evaluate_cm", rel_gr_evaluate_cm);
	}
	
	public static boolean delRelCoEva(Integer tpId){
		boolean status = true;
		List<Record> temp = Db.find("select * from rel_co_evaluate where teaching_program_id = ?", tpId);
		for(int i = 0;i<temp.size();i++){
			if(!Db.deleteById("rel_co_evaluate", temp.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	
	public static boolean saveRelCoEva(Integer tpId,String CReach,String CeType,String CsType,String CWeight,String CrWeight, String[] CeModel)
	{
		boolean status = true;
		Record rel_co_evaluate_data = new Record().set("teaching_program_id", tpId).set("teaching_program_co_order", CReach).set("evaluate_type_id", CeType).set("score_type_id", CsType).set("weight",CWeight).set("reach_weight", CrWeight);		
		if(!Db.save("rel_co_evaluate", rel_co_evaluate_data)){
			status =false;
		}
		String co_id = Db.findFirst("select id from rel_co_evaluate "
				+ "where teaching_program_id = ? && teaching_program_co_order = ? && evaluate_type_id = ?", tpId,CReach,CeType).getInt("id").toString();
		
		for(int m=0;m<CeModel.length;m++){
			Record rel_co_evaluate_cm = new Record().set("rel_co_evaluate_id", co_id).set("teaching_program_cm_order", CeModel[m]).set("teaching_program_id", tpId);
			if(!Db.save("rel_co_evaluate_cm", rel_co_evaluate_cm)){
				status = false;
			}
		}
		
		return status;	
	}
	
	public static boolean delRelCoEvaCM(Integer tpId){
		boolean status = true;
		List<Record> temp = Db.find("select * from rel_co_evaluate_cm where teaching_program_id = ?", tpId);
		for(int i = 0;i<temp.size();i++){
			if(!Db.deleteById("rel_co_evaluate_cm", temp.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}

	public static void saveRelCoEvaCM(Integer tpId,Integer co_num,String CReach,String[] CeModel){
		String co_id = Db.findFirst("select id from rel_co_evaluate where teaching_program_id = ? && teaching_program_co_order = ?", tpId,CReach).getInt("id").toString();
		for(int m=0;m<CeModel.length;m++){
			Record rel_co_evaluate_cm = new Record().set("rel_co_evaluate_id", co_id).set("teaching_program_cm_order", CeModel[m]).set("teaching_program_id", tpId);
			Db.save("rel_co_evaluate_cm", rel_co_evaluate_cm);	
		}
	}
	
	public static List<Record> getGRID(Integer tpId){
		List<Record> gr_id=Db.find("select gr_id from rel_cm_co where teaching_program_id =?",tpId);
		return gr_id;
	}
	
	public static List<Record> getCOEvaCMId(Integer tpId){
		return Db.find("select * from rel_co_evaluate_cm where teaching_program_id = ?", tpId);
	}

	public static List<Record> getCReach(Integer tpId){
		return Db.find("select * from rel_co_evaluate where teaching_program_id = ?", tpId);
	}
	
	public static List<Record> getGREvaCMId(Integer tpId){
		return Db.find("select * from rel_gr_evaluate_cm where teaching_program_id = ?", tpId);
	}

	public static List<Record> getGReach(Integer tpId){
		return Db.find("select * from rel_gr_evaluate where teaching_program_id = ?", tpId);
	}
	
	//获取email地址
	public static String getEmail(Integer id){
		return Db.findFirst("select email from users where id = ?",id).getStr("email");
	}

}

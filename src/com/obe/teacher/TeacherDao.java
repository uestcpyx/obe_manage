package com.obe.teacher;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.obe.jiaowuke.JiaowukeDao;


public class TeacherDao {

	public static List<Record> getLessonList(String uId){
		String sql = "select t.id, t.create_date, t.code, t.status, t.cn_name, r.leader_flag, t.course_group_id from teaching_program t "
				+ "left join (users u LEFT JOIN rel_program_teacher r ON u.id = r.user_id) "
				+ "ON t.id = r.teaching_program_id where r.master_flag  =0 && r.user_id =?";
		List<Record> courses = Db.find(sql,uId); 
		for(int i = 0;i<courses.size();i++){
			Record master = Db.findFirst("select u.real_name from users u left join rel_program_teacher r on u.id = r.user_id "
						+ "where teaching_program_id = ? && leader_flag =1", courses.get(i).get("id"));
			courses.get(i).set("master", master.get("real_name")).set("course_group", JiaowukeDao.getCourseGroup(courses.get(i).getInt("course_group_id")).get("course_group_name"));
		}
		return courses;
	}


	public static List<Record> getPoGrCoCm(Integer pid){
		String sql = "select cm_id, co_id, gr_id, po_id from rel_cm_co where teaching_program_id = ?";
		return Db.find(sql,pid);
	}
	
	public static List<Record> getCmIntro(Integer pId){
		List<Record> cm_intro = Db.find("select * from teaching_program_cm where teaching_program_id = ?",pId);
		return cm_intro;
	}
	
	public static List<Record> getCoIntro(Integer pId){
		List<Record> co_intro = Db.find("select * from teaching_program_co where teaching_program_id = ?",pId);
		return co_intro;
	}
	
	public static String getCourseType(Integer courseTypeId){
		Record course_type = Db.findFirst("select course_type from mb_course_type where id = ?",courseTypeId);
		return course_type.getStr("course_type");
	}
	
	public static List<Record> getPreTeachingProgram(List<Record> preRequirement){
		List<Record> PreTeachingProgram = new ArrayList<Record>();
		for(int i=0;i<preRequirement.size();i++){
			Record test = Db.findFirst("select cn_name from teaching_program where id = ?",preRequirement.get(i).get("pre_teaching_program_id"));
			if(test!=null){
				PreTeachingProgram.add(test);
			}
		}
		return PreTeachingProgram;
	}

	public static String getEvaluateType(Integer evaluate_type_id){
		return Db.findFirst("select type from mb_evaluate_type where id =?",evaluate_type_id).getStr("type");
	}
	
	public static String getGrNumber(Integer gr_id){
		String level1 = Db.findFirst("select level1 from mb_gr where id =?",gr_id).getInt("level1").toString();
		String level2 = Db.findFirst("select level2 from mb_gr where id =?",gr_id).getInt("level2").toString();
		return level1 + '.' +level2;
	}
	
	public static List<String> getCmList(Integer co_id,Integer pId){
		List<Record> test = Db.find("select teaching_program_cm_order from rel_co_evaluate_cm where rel_co_evaluate_id =? && teaching_program_id =?",co_id,pId);
		
		List<String> test2 = new ArrayList<String>();
		for(int i=0;i<test.size(); i++){
			test2.add(test.get(i).getInt("teaching_program_cm_order").toString());
		}
		return test2;
	}
	
	public static List<String> getGrCmList(Integer gr_id,Integer pId){
		List<Record> test = Db.find("select teaching_program_cm_order from rel_gr_evaluate_cm where rel_gr_evaluate_id =? && teaching_program_id =?",gr_id,pId);
		List<String> test2 = new ArrayList<String>();
		for(int i=0;i<test.size(); i++){
			test2.add(test.get(i).getInt("teaching_program_cm_order").toString());
		}
		return test2;
	}

	public static String getDate(Integer tpId){
		Record test = Db.findFirst("select date(update_date) from teaching_program where id =?", tpId);
		String Date = test.getDate("date(update_date)").toString();
		return Date;
	}

}

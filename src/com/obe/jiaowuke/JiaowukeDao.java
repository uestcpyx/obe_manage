package com.obe.jiaowuke;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class JiaowukeDao {

	public static boolean DeleteCourse(Integer tpId){
		boolean a = Db.deleteById("teaching_program", tpId);
		boolean b = Db.deleteById("rel_program_teacher", "teaching_program_id", tpId);
		boolean c = Db.deleteById("rel_cm_co", "teaching_program_id", tpId);
		boolean d = Db.deleteById("rel_pre_requirement", "teaching_program_id", tpId);
		boolean e = Db.deleteById("rel_co_evaluate", "teaching_program_id", tpId);
        boolean f = Db.deleteById("rel_co_evaluate_cm", "teaching_program_id", tpId);
        boolean g = Db.deleteById("rel_gr_evaluate", "teaching_program_id", tpId);
        boolean h = Db.deleteById("rel_gr_evaluate_cm", "teaching_program_id", tpId);
        boolean i = Db.deleteById("teaching_program_co", "teaching_program_id", tpId);
        boolean k = Db.deleteById("teaching_program_cm", "teaching_program_id", tpId);
		
        return a&&b;
	}
	
	public static Record getCourseGroup(Integer CId){
		return Db.findFirst("select course_group_name from mb_course_group where id = ?", CId);
	}
	
	public static Record getEdition(Integer pId){
		return Db.findFirst("select edition from teaching_program where id = ?", pId);
	}
	
	public static List<Record> getOldVersion(String code){
		return Db.find("select id, cn_name, code, edition from teaching_program where code =? && status=3", code);
	}
	
	public static Record getOldVersionInfo(Integer tpId){
		return Db.findFirst("select * from teaching_program where id =?", tpId);
	}
	
	//获取用户邮箱
	public static String getUserEmail(Integer gId){
		return Db.findFirst("select email FROM users,mb_course_group WHERE mb_course_group.id = ? AND mb_course_group.master_id = users.id",gId).getStr("email");	
	}
	
	//获得课程组下所有教师的邮箱
	public static String getTechersEmail(Integer pId){
		String techersEmail = "";
		List<Record> techerIds = Db.find("SELECT user_id FROM `rel_program_teacher` WHERE teaching_program_id = ?",pId);	
		for(int i=0;i<techerIds.size();i++){
			if(i == techerIds.size()-1){
				techersEmail += Db.findFirst("select email FROM users where id = ?",techerIds.get(i).get("user_id")).getStr("email");
			}
			else{
				techersEmail = techersEmail + Db.findFirst("select email FROM users where id = ?",techerIds.get(i).get("user_id")).getStr("email") + ",";
			}
			
		}
		
		return techersEmail;
	}

}


package com.obe.exam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ExamDao {

	public static List<Record> getEvaluateType(Integer tpId){
		List<Record> tList = Db.find("select id, evaluate_type_id, score_type_id, weight, reach_weight from rel_co_evaluate where teaching_program_id = ?", tpId);
		return tList;
	}
	
	public static List<Record> getCO(Integer EtypeId, Integer tpId){
		List<Record> tList = Db.find("select r.id, t.co_order, t.co_name from rel_co_evaluate r "
				+ "left join teaching_program_co t on r.teaching_program_id = t.teaching_program_id && r.teaching_program_co_order = t.co_order"
				+ "where r.teaching_program_id = ? && r.evaluate_type_id =?", EtypeId, tpId);
		return tList;
	}
	
	public static List<Record> getCM(Integer rel_co_evaluate_id, Integer tpId){
		List<Record> tList = Db.find("select t.cm_order, t.cm_name from rel_co_evaluate_cm r "
				+ "left join teaching_program_cm t on t.cm_order = r.teaching_program_cm_order "
				+ "WHERE r.rel_co_evaluate_id = ? && t.teaching_program_id = ?", rel_co_evaluate_id, tpId);
		return tList;
	}
	
	public static boolean saveQuestion(String level1, String level2, String eType, Integer tpId, String weight, String expect, String intro, Integer order, String[] CM){
		boolean status = true;
		Record t = new Record().set("level1", level1).set("level2", level2).set("weight", weight).set("evaluate_type_id", eType).set("teaching_program_id", tpId).set("expect", expect).set("intro", intro).set("status", 1).set("q_order", order);
		if(!Db.save("exam_question", t)){
			status =false;
		}
		
		for(int m=0;m<CM.length;m++){
			Record rel_question_cm = new Record().set("exam_question_order", order).set("cm_order", CM[m]).set("teaching_program_id", tpId);
			if(!Db.save("rel_question_cm", rel_question_cm)){
				status = false;
			}
		}
		return status;
		
	}
	
	
	public static List<Record> getQuestionBytpId(Integer tpId){
		List<Record> tList = Db.find("select * from exam_question where teaching_program_id = ?", tpId);
		Long ksnum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 1);
		Long kcnum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 2);
		Long zynum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 3);
		Long synum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 4);
		
		for (int i = 0; i < tList.size(); i++) {
			Integer q_order = tList.get(i).getInt("q_order");
			List<Record> cm = Db.find("select t.cm_order, t.cm_name from rel_question_cm r left join teaching_program_cm t on r.cm_order = t.cm_order && r.teaching_program_id = t.teaching_program_id where r.exam_question_order = ? && r.teaching_program_id = ?", q_order, tpId);
			tList.get(i).set("cmList", cm);
			if(tList.get(i).getInt("evaluate_type_id")==1){
				tList.get(i).set("row", q_order);
			}
			else if(tList.get(i).getInt("evaluate_type_id")==2){
				tList.get(i).set("row", q_order-ksnum);
			}
			else if(tList.get(i).getInt("evaluate_type_id")==3){
				tList.get(i).set("row", q_order-ksnum-kcnum);
			}
			else if(tList.get(i).getInt("evaluate_type_id")==4){
				tList.get(i).set("row", q_order-ksnum-kcnum-zynum);
			}
			else{
				tList.get(i).set("row", q_order-ksnum-kcnum-zynum-synum);
			}
		}
		
		return tList;
	}
	
	public static Long getQuestionNum(Integer tpId){
		Long num = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && status =1", tpId);
		return num;
	}
	
	public static boolean renewQuestion(Integer tpId){
		boolean status = true;
		List<Record> qList = Db.find("select * from exam_question where teaching_program_id = ?", tpId);
		for(int i =0; i<qList.size(); i++){
			if(!Db.deleteById("exam_question", qList.get(i).get("id"))){
				status = false;
			}
		}
		return status;
	}
	
	public static Record getBasicInformation(Integer tpId){
		return Db.findFirst("select t.cn_name, t.create_date, c.course_type,t.hours,t.credit from teaching_program t,mb_course_type c where t.id =? and c.id = t.course_type_id",tpId);
	}
	
	public static List<Record> getRate(Integer tpId,ArrayList<Integer> achievementNum){
		List<Record> exam_question = Db.find("select * from exam_question where teaching_program_id = ? && status =1",tpId);
		DecimalFormat df=new DecimalFormat("0.00");
		for(int i =0;i<exam_question.size(); i++){
			List<Record> cm = Db.find("select cm_order from rel_question_cm where exam_question_order = ? and teaching_program_id =?",exam_question.get(i).get("q_order"),tpId);
			exam_question.get(i).set("cm", cm);
			Record t1 = Db.findFirst("select o.teaching_program_co_order "
					+ "from rel_co_evaluate o ,rel_co_evaluate_cm m "
					+ "where m.teaching_program_id=? and m.teaching_program_cm_order=? "
					+ "and m.rel_co_evaluate_id=o.id and o.evaluate_type_id =?", tpId,cm.get(0).get("cm_order"),exam_question.get(i).get("evaluate_type_id"));
			if(t1!=null){
				exam_question.get(i).set("co", t1.get("teaching_program_co_order"));
			}
			else{
				exam_question.get(i).set("co", 0);
			}
			
			Record t2 = Db.findFirst("select o.gr_id "
					+ "from rel_gr_evaluate o, rel_gr_evaluate_cm m "
					+ "where m.teaching_program_id=? and m.teaching_program_cm_order=? "
					+ "and m.rel_gr_evaluate_id=o.id and o.evaluate_type_id =?", tpId,cm.get(0).get("cm_order"),exam_question.get(i).get("evaluate_type_id"));
			if(t2!=null){
				exam_question.get(i).set("gr", t2.get("gr_id"));
			}
			else{
				exam_question.get(i).set("gr", 0);
			}
			
			exam_question.get(i).set("achievementNum", achievementNum.get(i));
			Db.update("update exam_question set achieve = ? where id = ?", achievementNum.get(i), exam_question.get(i).get("id"));
			exam_question.get(i).set("achievementDegree", df.format((double)achievementNum.get(i)/(double)exam_question.get(i).getInt("expect")));
		}
		return exam_question;
	}

	public static List<Record> getRateByTpId(Integer tpId){
		List<Record> exam_question = Db.find("select * from exam_question where teaching_program_id = ? && status =1",tpId);
		DecimalFormat df=new DecimalFormat("0.00");
		for(int i =0;i<exam_question.size(); i++){
			List<Record> cm = Db.find("select cm_order from rel_question_cm where exam_question_order = ? and teaching_program_id =?",exam_question.get(i).get("q_order"),tpId);
			exam_question.get(i).set("cm", cm);
			Record t1 = Db.findFirst("select o.teaching_program_co_order "
					+ "from rel_co_evaluate o ,rel_co_evaluate_cm m "
					+ "where m.teaching_program_id=? and m.teaching_program_cm_order=? "
					+ "and m.rel_co_evaluate_id=o.id and o.evaluate_type_id =?", tpId,cm.get(0).get("cm_order"),exam_question.get(i).get("evaluate_type_id"));
			if(t1!=null){
				exam_question.get(i).set("co", t1.get("teaching_program_co_order"));
			}
			else{
				exam_question.get(i).set("co", 0);
			}
			
			Record t2 = Db.findFirst("select o.gr_id "
					+ "from rel_gr_evaluate o, rel_gr_evaluate_cm m "
					+ "where m.teaching_program_id=? and m.teaching_program_cm_order=? "
					+ "and m.rel_gr_evaluate_id=o.id and o.evaluate_type_id =?", tpId,cm.get(0).get("cm_order"),exam_question.get(i).get("evaluate_type_id"));
			if(t2!=null){
				exam_question.get(i).set("gr", t2.get("gr_id"));
			}
			else{
				exam_question.get(i).set("gr", 0);
			}
			
			exam_question.get(i).set("achievementNum", exam_question.get(i).get("achieve"));
			exam_question.get(i).set("achievementDegree", df.format((double)exam_question.get(i).getInt("achieve")/(double)exam_question.get(i).getInt("expect")));
		}
		return exam_question;
	}
	public static List<Record> getCOByTpId(Integer tpId){
		List<Record> coList = Db.find("select * from rel_co_evaluate where teaching_program_id =?", tpId);
		return coList;
	}

	public static List<Record> getQuestionByCM(Integer cm_order, Integer tpId){
		List<Record> qList = Db.find("select exam_question_order from rel_question_cm r where r.cm_order =? && teaching_program_id = ?", cm_order, tpId);
		return qList;
	}
	
	public static List<Record> getEtypeByTpId(Integer co_order, Integer tpId){
		List<Record> Etype = Db.find("select evaluate_type_id from rel_co_evaluate where teaching_program_co_order = ? && teaching_program_id = ?", co_order, tpId);
		return Etype;
	}
	
	public static List<Record> getEtypeByTpIdGR(Integer gr_id, Integer tpId){
		List<Record> e = Db.find("select evaluate_type_id from rel_gr_evaluate where gr_id = ? && teaching_program_id = ?", gr_id, tpId);
		return e;
	}
	
	public static List<Record> getGRByTpId(Integer tpId){
		List<Record> grList = Db.find("select r.id, r.gr_id,m.level1,m.level2,r.evaluate_type_id,r.weight,r.reach_weight from rel_gr_evaluate r,mb_gr m where r.teaching_program_id =? and r.gr_id = m.id", tpId);
		return grList;
	}
	
	public static List<Record> getGRCM(Integer rel_gr_evaluate_id, Integer tpId){
		List<Record> tList = Db.find("select t.cm_order, t.cm_name from rel_gr_evaluate_cm r "
				+ "left join teaching_program_cm t on t.cm_order = r.teaching_program_cm_order "
				+ "WHERE r.rel_gr_evaluate_id = ? && t.teaching_program_id = ?", rel_gr_evaluate_id, tpId);
		return tList;
	}
	
	public static boolean saveCOAchieve(Integer co_order, Integer tpId, Double achieve){
		Record tRecord = Db.findById("rel_co_evaluate", "co_order","teaching_program_id", co_order,tpId).set("achievement", achieve);
		return Db.update("rel_co_evaluate", tRecord);
	}
	
	public static boolean saveGRAchieve(Integer gr_id, Integer tpId, Double achieve){
		Record tRecord = Db.findById("rel_co_evaluate", "gr_id","teaching_program_id", gr_id, tpId).set("achievement", achieve);
		return Db.update("rel_co_evaluate", tRecord);
	}
	
	public static Long getFinalExamQuestionNum(Integer tpId){
		Long num = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && status =1 && evaluate_type_id = 1", tpId);
		return num;
	}
	
	public static Long getAllExamQuestionNum(Integer tpId){
		Long num = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && status =1", tpId);
		return num;
	}
	
	public static Integer gettotalExpect(Integer tpId){
		List<Record> tList = Db.find("select expect from exam_question where teaching_program_id = ? && status =1", tpId);
		Integer total =0;
		for(int i =0; i<tList.size(); i++){
			total = total + tList.get(i).getInt("expect");
		}
		return total;
	}
	
	public static boolean saveDistribution(Integer tpId, Record t){
		Long num = Db.queryLong("select count(*) from exam_report where teaching_program_id = ?", tpId);
		if(num!=0){
			Db.deleteById("exam_report", "teaching_program_id", tpId); //删除旧数据
		}
		Record tRecord = new Record().set("final90", t.get("final90")).set("final80", t.get("final80")).set("final70", t.get("final70"));
		tRecord.set("final60", t.get("final60")).set("final00", t.get("final00")).set("all90", t.get("all90")).set("all80", t.get("all80"));
		tRecord.set("all70", t.get("all70")).set("all60", t.get("all60")).set("all00", t.get("all00")).set("teaching_program_id", tpId);
		return Db.save("exam_report", tRecord);
	}
	
	public static Record getDistribution(Integer tpId){
		Record t = Db.findFirst("select * from exam_report where teaching_program_id = ?", tpId);
		return t;
	}
}

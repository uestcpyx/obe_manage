package com.obe.exam;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

public class ExamService {
	//根据大纲id查询CM和EType
	public static List<Record> getCMandETypeByTpId(Integer tpId){
		List<Record> EType = ExamDao.getEvaluateType(tpId);
		List<Record> eList = new ArrayList<Record>();
		for(int i =0; i<EType.size(); i++){
			if(eList.size()==0){
				Record t = new Record().set("eType", EType.get(i).get("evaluate_type_id"));
				List<Record> cm = ExamDao.getCM(EType.get(i).getInt("id"), tpId);
				for (int j = 0; j < EType.size(); j++) {
					if(i!=j && EType.get(i).get("evaluate_type_id")==EType.get(j).get("evaluate_type_id")){
						List<Record> cm1 = ExamDao.getCM(EType.get(j).getInt("id"), tpId);
						cm.addAll(cm1);
					}
				}
				t.set("CMList", cm);
				eList.add(t);
			}
			else{
				boolean repeat = false;
				for(int j =0; j<eList.size(); j++){
					if(eList.get(j).get("eType")==EType.get(i).get("evaluate_type_id")){
						repeat = true;
					}
				}
				if(repeat!=true){
					Record t = new Record().set("eType", EType.get(i).get("evaluate_type_id"));
					List<Record> cm = ExamDao.getCM(EType.get(i).getInt("id"), tpId);
					for (int j = 0; j < EType.size(); j++) {
						if(i!=j && EType.get(i).get("evaluate_type_id")==EType.get(j).get("evaluate_type_id")){
							List<Record> cm1 = ExamDao.getCM(EType.get(j).getInt("id"), tpId);
							cm.addAll(cm1);
						}
					}
					t.set("CMList", cm);
					eList.add(t);
				}
				
			}
		}
		return eList;
 	}
	
	public static List<Record> getCOIntroByTpId(Integer tpId){
		List<Record> coList = ExamDao.getCOByTpId(tpId);
		for(int i=0; i<coList.size(); i++){
			List<Record> cm = ExamDao.getCM(coList.get(i).getInt("id"), tpId);
			for (int j = 0; j < cm.size(); j++) {
				List<Record> q = ExamDao.getQuestionByCM(cm.get(j).getInt("cm_order"), tpId);
				cm.get(j).set("qList", q);
			}
			coList.get(i).set("CMList", cm);
		}
		return coList;
	}
}
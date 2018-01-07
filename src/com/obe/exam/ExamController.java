package com.obe.exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.obe.auth.AuthController;
import com.obe.common.tools.tools;
import com.obe.program.ProgramDao;

public class ExamController extends Controller{

	public void setExamInformation(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("pId");
		
		
		//获得打包好的数据
		List<Record> qList = ExamDao.getQuestionBytpId(tpId);
		if(qList.size()!=0){
			setAttr("qList", qList);
		}
		List<Record> eList = ExamService.getCMandETypeByTpId(tpId);
		List<Record> cmList = ProgramDao.getCM(tpId);
		
		if(cmList!=null)
		{
			setAttr("cmList", cmList);
			setAttr("cmListNum", cmList.size());
		}
		
		Long ksnum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 1);
		Long kcnum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 2);
		Long zynum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 3);
		Long synum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 4);
		Long qtnum = Db.queryLong("select count(*) from exam_question where teaching_program_id = ? && evaluate_type_id = ?", tpId, 5);
		//渲染
		setAttr("eList", eList);
		setAttr("ksnum",ksnum);
		setAttr("kcnum",kcnum);
		setAttr("zynum",zynum);
		setAttr("synum",synum);
		setAttr("qtnum",qtnum);
		setAttr("pId",tpId);
		
		render("setExamInformation.html");
	}

	public void postExamInformation(){
		Integer tpId = getParaToInt("pId");
		String kaoshi = getPara("KAOSHI_num");
		String kaocha = getPara("KAOCHA_num");
		String zuoye = getPara("ZUOYE_num");
		String shiyan = getPara("SHIYAN_num");
		String others = getPara("QITA_num");

		List<Record> excelList = new ArrayList<Record>();
		
		ExamDao.renewQuestion(tpId);
		
		Integer order = 0;
		//期末考试
		if(kaoshi!=null && !kaoshi.equals(0)){
			Integer Examnum = getParaToInt("KAOSHI_num");
			
			String level1List[] = getParaValues("kaoshi_level1"); //获得大题编号数组
			String level2List[] = getParaValues("kaoshi_level2"); //获得小题编号数组
			String Weight[] = getParaValues("kaoshi_CWeight"); //获得权重
			String Expect[] = getParaValues("kaoshi_expect"); //获得期望值
			for(int i=0; i<Examnum; i++){
				String CMorder[] = getParaValues("kaoshi_cm_select"+(i+1)); //获得每题对应的CM模块
				String qustion = "期末考试第"+level1List[i]+"-"+level2List[i]+"题";
				Record cell = new Record().set("question", qustion).set("eType", "1");
				excelList.add(cell);
				
				order = order +1;
				ExamDao.saveQuestion(level1List[i], level2List[i], "1", tpId, Weight[i], Expect[i], "", order, CMorder);
			}		
		}
		
		//考察
		if(kaocha!=null && !kaocha.equals(0)){
			Integer Examnum = getParaToInt("KAOCHA_num");
			
			String level1List[] = getParaValues("kaocha_level1"); //获得大题编号数组
			String Weight[] = getParaValues("kaocha_CWeight"); //获得权重
			String Expect[] = getParaValues("kaocha_expect"); //获得期望值
			String intro[] = getParaValues("kaocha_intro"); //获得intro
			for(int i=0; i<Examnum; i++){
				String CMorder[] = getParaValues("kaocha_cm_select"+(i+1)); //获得每题对应的CM模块
				String qustion = intro[i];
				Record cell = new Record().set("question", qustion).set("eType", "2");
				excelList.add(cell);
				order = order +1;
				ExamDao.saveQuestion(level1List[i], "0", "2", tpId, Weight[i], Expect[i], intro[i], order, CMorder);
			}		
		}
		
		//作业
		if(zuoye!=null && !zuoye.equals(0)){
			Integer Examnum = getParaToInt("ZUOYE_num");
			String level1List[] = getParaValues("zuoye_level1"); //获得大题编号数组
			String Weight[] = getParaValues("zuoye_CWeight"); //获得权重
			String Expect[] = getParaValues("zuoye_expect"); //获得期望值
			String intro[] = getParaValues("zuoye_intro"); //获得intro
			for(int i=0; i<Examnum; i++){
				String CMorder[] = getParaValues("zuoye_cm_select"+(i+1)); //获得每题对应的CM模块
				String qustion = intro[i];
				Record cell = new Record().set("question", qustion).set("eType", "3");
				excelList.add(cell);
				order = order+1;
				ExamDao.saveQuestion(level1List[i], "0", "3", tpId, Weight[i], Expect[i], intro[i], order, CMorder);
			}	
		}
		
		//实验
		if(shiyan!=null && !shiyan.equals(0)){
			Integer Examnum = getParaToInt("SHIYAN_num");
			
			String level1List[] = getParaValues("shiyan_level1"); //获得大题编号数组
			String Weight[] = getParaValues("shiyan_CWeight"); //获得权重
			String Expect[] = getParaValues("shiyan_expect"); //获得期望值
			String intro[] = getParaValues("shiyan_intro"); //获得intro
			for(int i=0; i<Examnum; i++){
				String CMorder[] = getParaValues("shiyan_cm_select"+(i+1)); //获得每题对应的CM模块
				String qustion = intro[i];
				Record cell = new Record().set("question", qustion).set("eType", "4");
				excelList.add(cell);
				order = order +1;
				ExamDao.saveQuestion(level1List[i], "0", "4", tpId, Weight[i], Expect[i], intro[i], order, CMorder);
			}	
		}
		
		//其他
		if(others!=null && !others.equals(0)){
			Integer Examnum = getParaToInt("QITA_num");
			
			String level1List[] = getParaValues("qita_level1"); //获得大题编号数组
			String Weight[] = getParaValues("qita_CWeight"); //获得权重
			String Expect[] = getParaValues("qita_expect"); //获得期望值
			String intro[] = getParaValues("qita_intro"); //获得intro
			for(int i=0; i<Examnum; i++){
				String CMorder[] = getParaValues("qita_cm_select"+(i+1)); //获得每题对应的CM模块
				String qustion = intro[i];
				Record cell = new Record().set("question", qustion).set("eType", "5");
				excelList.add(cell);
				order = order+1;
				ExamDao.saveQuestion(level1List[i], "0", "5", tpId, Weight[i], Expect[i], intro[i], order, CMorder);
			}		
		}
		
		
		System.out.println(excelList.size());
		
		String title = "学生成绩录入表";
		//声明一个工作簿
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(title);
		sheet.setDefaultColumnWidth((short)15);
		
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell0 = row.createCell(0);
		cell0.setCellValue("序号");
		XSSFCell cell1 = row.createCell(1);
		cell1.setCellValue("学号");
		XSSFCell cell2 = row.createCell(2);
		cell2.setCellValue("姓名");
		
		for(int i =0; i<excelList.size(); i++){
			XSSFCell cell = row.createCell(3+i);
			cell.setCellValue(excelList.get(i).get("question").toString());
		}
		
//		XSSFCell cell3 = row.createCell(2+excelList.size()+1);
//		cell3.setCellValue("总评成绩");
//		XSSFCell cell4 = row.createCell(2+excelList.size()+2);
//		cell4.setCellValue("通过");
		
		OutputStream out;
		try {
			out = new FileOutputStream("../学生成绩录入表.xlsx");
			workbook.write(out);
			out.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		renderFile(new File("../学生成绩录入表.xlsx"));
		
		
	}
	
	public void setExcelShow(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("pId");
		
		setAttr("pId", tpId);
		render("uploadExamExcel.html");
	}
	
	public void readExcel(){
		UploadFile test = getFile();
		String basePath=File.separator+".."+File.separator+"fileserver"+File.separator+"obe_upload";
		String dateString=tools.ConvertTimeLongToString(System.currentTimeMillis()).substring(0,7);
		String savePath=basePath+File.separator+dateString;
		String uploadPath = PathKit.getWebRootPath()+savePath;
		File file = new File(uploadPath);
		if(file.isDirectory()&&file.exists())
		{
			//目录存在
		}else {
			file.mkdir();//不存在就创建
		}
		
		String Fname = test.getFileName();
		String prefix=Fname.substring(Fname.length()-3);
		prefix=prefix.toLowerCase();
		if (prefix.equals("doc")||prefix.equals("pdf")||prefix.equals("jpg")||prefix.equals("png")||prefix.equals("ppt")
				||prefix.equals("xls")||prefix.equals("zip")||prefix.equals("rar"))
		{
			
		}else{
			prefix=Fname.substring(Fname.length()-4);
			prefix=prefix.toLowerCase();
			if (prefix.equals("docx")||prefix.equals("xlsx")||prefix.equals("pptx"))
			{
				
			}else {
				//out.print("err");
				return;
			}
		}
		
		String saveFileName =System.currentTimeMillis()+tools.getRandomString(16)+"."+prefix;//组成存储名
		File savedFile = new File(uploadPath, saveFileName);
		
		test.getFile().renameTo(savedFile);
		System.out.println(savedFile.getAbsolutePath());
		System.out.println(savedFile.getPath());
		
		List<Record> records = new ArrayList<Record>();
		List<String> titles = new ArrayList<String>();
		
		//解析excel表格
		try{
			FileInputStream excelFileInputStream = new FileInputStream(savedFile.getAbsolutePath());
			XSSFWorkbook workbook = new XSSFWorkbook(excelFileInputStream);
			
			
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			//获得标题数组
			XSSFRow title = sheet.getRow(0);
	        int colNum = title.getLastCellNum();
	        System.out.println("colNum:" + colNum);
	       
	        for (int i =0; i <colNum; i++) {
	        	XSSFCell titleCell =title.getCell(i);
	        	titles.add(titleCell.getStringCellValue());
	        }
	        
	        //获得数据
			for(int rowIndex =1; rowIndex <= sheet.getLastRowNum(); rowIndex++){
				XSSFRow row =sheet.getRow(rowIndex);
				if(row == null){
					continue;
				}
				Record tRecord = new Record();
				
				for (int i =0; i <colNum; i++) {
		        	XSSFCell Cell =row.getCell(i);
		        	if(Cell == null){
		        		tRecord.set("Cell"+i, "");
					}
		        	
		        	if(Cell.getCellType()==0){
		        		tRecord.set("Cell"+i, Cell.getNumericCellValue());
		        	}
		        	else{
		        		tRecord.set("Cell"+i, Cell.getStringCellValue());
		        	}
		        	
		        	tRecord.set("colNum", colNum);
		        }
				
				records.add(tRecord);
			}
			excelFileInputStream.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		//删除临时文件
		if(savedFile.delete()){
			System.out.println("success");
		}
		else{
			System.out.println("fail");
		}

		setAttr("titles", titles);
		setAttr("intro", records);
		setAttr("status", "success");
		renderJson();
	}
	
	public void postExcelInformation(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		Integer tpId = getParaToInt("tpId");
		DecimalFormat df = new DecimalFormat("0.00");
		
		Integer q_Num = Integer.parseInt(ExamDao.getQuestionNum(tpId).toString());
		ArrayList<Integer []> question = new ArrayList<Integer []>();
		
		//qFinalExamNum是所有期末考试考题的数量
		Integer qFinalExamNum = Integer.parseInt(ExamDao.getFinalExamQuestionNum(tpId).toString());
		Integer qAllExamNum = Integer.parseInt(ExamDao.getAllExamQuestionNum(tpId).toString());
		//basicInformation封装逻辑
		Record basicInformation = ExamDao.getBasicInformation(tpId);
		
		//计算卷面成绩分布
		//计数ID，studentCount[0]是90以上人数，依次类推
		Integer[] studentCount = new Integer[5];
		for(int i =0; i<5; i++){
			studentCount[i] = 0; //预设数据
		}
				
		//temp.length是学生人数，studentScoreList是一个二维表，行是学生，列是各小题
		Integer[] temp = getParaValuesToInt("question"+1);
		Integer[][] studentScoreList = new Integer[temp.length][qFinalExamNum];
		Integer[][] studentallScoreList = new Integer[temp.length][qAllExamNum];
		
		for(int j=0; j<qFinalExamNum; j++){
			Integer q[] = getParaValuesToInt("question"+(j+1)); 
			for(int i=0; i<temp.length; i++){
				studentScoreList[i][j] = q[i]; 
			}
		}
		
				
		for(int i=0; i<temp.length; i++){
			Integer totalScore = 0;
			for(int j =0; j<qFinalExamNum; j++){
				totalScore += studentScoreList[i][j];
			}
			if(totalScore>=90){
				studentCount[0]+=1;
			}
			else if(totalScore>=80&&totalScore<90){
				studentCount[1]+=1;
			}
			else if(totalScore>=70&&totalScore<80){
				studentCount[2]+=1;
			}
			else if(totalScore>=60&&totalScore<70){
				studentCount[3]+=1;
			}
			else{
				studentCount[4]+=1;
			}	
		}
		
		basicInformation.set("final90",Double.parseDouble(df.format((double)studentCount[0]/temp.length))*100);
		basicInformation.set("final80",Double.parseDouble(df.format((double)studentCount[1]/temp.length))*100);
		basicInformation.set("final70",Double.parseDouble(df.format((double)studentCount[2]/temp.length))*100);
		basicInformation.set("final60",Double.parseDouble(df.format((double)studentCount[3]/temp.length))*100);
		basicInformation.set("final00",Double.parseDouble(df.format((double)studentCount[4]/temp.length))*100);
		
		//计算总体成绩分布
		for(int i =0; i<5; i++){
			studentCount[i] = 0; //归0重新计算
		}
		
		for(int j=0; j<qAllExamNum; j++){
			Integer q[] = getParaValuesToInt("question"+(j+1)); 
			for(int i=0; i<temp.length; i++){
				 studentallScoreList[i][j] = q[i]; 
			}
		}
		//计算总期望值
		Integer totalExpect = ExamDao.gettotalExpect(tpId); 
		
		for(int i=0; i<temp.length; i++){
			Integer totalScore = 0;
			for(int j =0; j<qAllExamNum; j++){
				totalScore += studentallScoreList[i][j];
			}
			if(totalScore>=totalExpect*0.9){
				studentCount[0]+=1;
			}
			else if(totalScore>=totalExpect*0.8&&totalScore<totalExpect*0.9){
				studentCount[1]+=1;
			}
			else if(totalScore>=totalExpect*0.7&&totalScore<totalExpect*0.8){
				studentCount[2]+=1;
			}
			else if(totalScore>=totalExpect*0.6&&totalScore<totalExpect*0.7){
				studentCount[3]+=1;
			}
			else{
				studentCount[4]+=1;
			}	
		}
		
		basicInformation.set("all90",Double.parseDouble(df.format((double)studentCount[0]/temp.length))*100);
		basicInformation.set("all80",Double.parseDouble(df.format((double)studentCount[1]/temp.length))*100);
		basicInformation.set("all70",Double.parseDouble(df.format((double)studentCount[2]/temp.length))*100);
		basicInformation.set("all60",Double.parseDouble(df.format((double)studentCount[3]/temp.length))*100);
		basicInformation.set("all00",Double.parseDouble(df.format((double)studentCount[4]/temp.length))*100);
		
		ExamDao.saveDistribution(tpId, basicInformation);
		
		//achievementDegree为每一列（即每一题）的达到值
		ArrayList<Integer> achievementNum = new ArrayList<Integer>();

		for(int i =0; i<q_Num; i++){
			 Integer[] q = getParaValuesToInt("question"+(i+1));
			 Integer t = 0;
			 for(int j=0; j<q.length; j++){
				 t+=q[j];
			 }
			 achievementNum.add((t/q.length));
			 question.add(q);
		}
		
		
		//返回前端的数据
		List<Record> eList = ExamService.getCMandETypeByTpId(tpId);
		List<Record> exam_question = ExamDao.getRate(tpId, achievementNum);
		List<Record> coList = ExamDao.getCOByTpId(tpId);
		List<Record> grList = ExamDao.getGRByTpId(tpId);

		List<Record> co = new ArrayList<Record>();
		List<Record> gr = new ArrayList<Record>();
		
		//coList封装逻辑
		for(int i=0; i<coList.size(); i++){
			if(co.size()==0){
				Record a = new Record().set("co_order", coList.get(i).get("teaching_program_co_order")).set("weight", coList.get(i).get("weight"));
				Integer eType = coList.get(i).getInt("evaluate_type_id");
				List<Record> cm = ExamDao.getCM(coList.get(i).getInt("id"), tpId);
				Double score = 0.00;
				Double coAchieve = 0.00; //co达成度
				Integer achieve =0, expect=0;
				for (int j = 0; j < exam_question.size(); j++) {
					if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
						achieve = achieve +exam_question.get(j).getInt("achievementNum");
						expect = expect +exam_question.get(j).getInt("expect");
					}
				}
				score = Double.parseDouble(df.format((double)achieve/(double)expect));
				coAchieve = coAchieve + score*coList.get(i).getDouble("reach_weight");
				Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(i).getDouble("reach_weight"), coList.get(i).get("id"));
				
				for(int k = 0; k<coList.size(); k++){
					if(k!=i && coList.get(k).get("teaching_program_co_order").equals(a.get("co_order"))){
						score = 0.00;
						achieve =0;
						expect =0;
						eType = coList.get(k).getInt("evaluate_type_id");
						for (int j = 0; j < exam_question.size(); j++) {
							if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
								achieve = achieve +exam_question.get(j).getInt("achievementNum");
								expect = expect +exam_question.get(j).getInt("expect");
							}
						}
						score = Double.parseDouble(df.format((double)achieve/(double)expect));
						coAchieve = coAchieve + score*coList.get(k).getDouble("reach_weight");
						Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(k).getDouble("reach_weight"), coList.get(k).get("id"));
						
						List<Record> cm1 = ExamDao.getCM(coList.get(k).getInt("id"), tpId);
						cm.addAll(cm1);
					}
				}
				a.set("cmList", cm);
				a.set("coAchieve", coAchieve);
				a.set("eType", ExamDao.getEtypeByTpId(a.getInt("co_order"), tpId));
				co.add(a);
			}
			else{
				//检查co中是否有重复的元素
				boolean repeat = false;
				for (int j = 0; j < co.size(); j++) {
					if(coList.get(i).get("teaching_program_co_order").equals(co.get(j).get("co_order"))){
						repeat = true;
					}
				}
				//若无，则添加
				if(repeat!=true){
					Record a = new Record().set("co_order", coList.get(i).get("teaching_program_co_order")).set("weight", coList.get(i).get("weight"));
					Integer eType = coList.get(i).getInt("evaluate_type_id");
					List<Record> cm = ExamDao.getCM(coList.get(i).getInt("id"), tpId);
					Double score = 0.00;
					Double coAchieve = 0.00; //co达成度
					Integer achieve =0, expect=0;
					for (int j = 0; j < exam_question.size(); j++) {
						if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
							achieve = achieve +exam_question.get(j).getInt("achievementNum");
							expect = expect +exam_question.get(j).getInt("expect");
						}
					}
					score = Double.parseDouble(df.format((double)achieve/(double)expect));
					coAchieve = coAchieve + score*coList.get(i).getDouble("reach_weight");
					Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(i).getDouble("reach_weight"), coList.get(i).get("id"));
					
					for(int k = 0; k<coList.size(); k++){
						if(k!=i && coList.get(k).get("teaching_program_co_order").equals(a.get("co_order"))){
							score = 0.00;
							achieve =0;
							expect =0;
							eType = coList.get(k).getInt("evaluate_type_id");
							for (int j = 0; j < exam_question.size(); j++) {
								if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
									achieve = achieve +exam_question.get(j).getInt("achievementNum");
									expect = expect +exam_question.get(j).getInt("expect");
								}
							}
							score = Double.parseDouble(df.format((double)achieve/(double)expect));
							coAchieve = coAchieve + score*coList.get(k).getDouble("reach_weight");
							Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(k).getDouble("reach_weight"), coList.get(k).get("id"));
							
							List<Record> cm1 = ExamDao.getCM(coList.get(k).getInt("id"), tpId);
							cm.addAll(cm1);
						}
					}
					a.set("cmList", cm);
					a.set("coAchieve", coAchieve);
					a.set("eType", ExamDao.getEtypeByTpId(a.getInt("co_order"), tpId));
					co.add(a);
				}
			}
			
		}
		Double pAchieve = 0.00; //课程达成度
		for(int i =0; i<co.size(); i++){
			pAchieve = pAchieve + co.get(i).getDouble("coAchieve") * co.get(i).getDouble("weight");
		}
		pAchieve = Double.parseDouble(df.format(pAchieve));
		Db.update("update exam_report set program_achievement = ? where teaching_program_id = ?", pAchieve, tpId);
		
		//gr封装逻辑
		for(int i=0; i<grList.size(); i++){
			if(gr.size()==0){
				Record a = new Record().set("gr_id", grList.get(i).get("gr_id")).set("weight", grList.get(i).get("weight")).set("level1", grList.get(i).get("level1")).set("level2", grList.get(i).get("level2"));
				Integer eType = grList.get(i).getInt("evaluate_type_id");
				Double score = 0.00;
				Double grAchieve = 0.00; //gr达成度
				Integer achieve =0, expect=0;
				
				List<Record> grscore = new ArrayList<Record>();
				Record t = new Record().set("eType", eType);
				for (int j = 0; j < exam_question.size(); j++) {
					if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
						achieve = achieve +exam_question.get(j).getInt("achievementNum");
						expect = expect +exam_question.get(j).getInt("expect");
					}
				}
				t.set("achieve", achieve).set("expect", expect);
				grscore.add(t);
				
				score = Double.parseDouble(df.format((double)achieve/(double)expect));
				grAchieve = grAchieve + score*grList.get(i).getDouble("reach_weight");
				Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(i).getDouble("reach_weight"), grList.get(i).get("id"));
				
				for(int k = 0; k<grList.size(); k++){
					if(k!=i && grList.get(k).get("gr_id").equals(a.get("gr_id"))){
						score = 0.00;
						achieve =0;
						expect =0;
						eType = grList.get(k).getInt("evaluate_type_id");
						Record t1 = new Record().set("eType", eType);
						
						for (int j = 0; j < exam_question.size(); j++) {
							if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
								achieve = achieve +exam_question.get(j).getInt("achievementNum");
								expect = expect +exam_question.get(j).getInt("expect");
							}
						}
						t1.set("achieve", achieve).set("expect", expect);
						grscore.add(t1);
						
						score = Double.parseDouble(df.format((double)achieve/(double)expect));
						grAchieve = grAchieve + score*grList.get(k).getDouble("reach_weight");
						Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(k).getDouble("reach_weight"), grList.get(k).get("id"));
					}
				}
				a.set("grAchieve", grAchieve);
				a.set("grscore", grscore);
				gr.add(a);
			}
			else{
				//检查gr中是否有重复的元素
				boolean repeat = false;
				for (int j = 0; j < gr.size(); j++) {
					if(grList.get(i).get("gr_id").equals(gr.get(j).get("gr_id"))){
						repeat = true;
					}
				}
				//若无，则添加
				if(repeat!=true){
					Record a = new Record().set("gr_id", grList.get(i).get("gr_id")).set("weight", grList.get(i).get("weight")).set("level1", grList.get(i).get("level1")).set("level2", grList.get(i).get("level2"));
					Integer eType = grList.get(i).getInt("evaluate_type_id");
					Double score = 0.00;
					Double grAchieve = 0.00; //gr达成度
					Integer achieve =0, expect=0;
					
					List<Record> grscore = new ArrayList<Record>();
					Record t = new Record().set("eType", eType);
					
					for (int j = 0; j < exam_question.size(); j++) {
						if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
							achieve = achieve +exam_question.get(j).getInt("achievementNum");
							expect = expect +exam_question.get(j).getInt("expect");
						}
					}
					t.set("achieve", achieve).set("expect", expect);
					grscore.add(t);
					
					score = Double.parseDouble(df.format((double)achieve/(double)expect));
					grAchieve = grAchieve + score*grList.get(i).getDouble("reach_weight");
					Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(i).getDouble("reach_weight"), grList.get(i).get("id"));
					
					for(int k = 0; k<grList.size(); k++){
						if(k!=i && grList.get(k).get("gr_id").equals(a.get("gr_id"))){
							score = 0.00;
							achieve =0;
							expect =0;
							eType = grList.get(k).getInt("evaluate_type_id");
							Record t1 = new Record().set("eType", eType);
							
							for (int j = 0; j < exam_question.size(); j++) {
								if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
									achieve = achieve +exam_question.get(j).getInt("achievementNum");
									expect = expect +exam_question.get(j).getInt("expect");
								}
							}
							t1.set("achieve", achieve).set("expect", expect);
							grscore.add(t1);
							
							score = Double.parseDouble(df.format((double)achieve/(double)expect));
							grAchieve = grAchieve + score*grList.get(k).getDouble("reach_weight");
							Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(k).getDouble("reach_weight"), grList.get(k).get("id"));
						}
					}
					a.set("grAchieve",grAchieve);
					a.set("grscore", grscore);
					gr.add(a);
				}
			}
			
		}
		Db.update("update teaching_program set status = 4 where id = ?", tpId);
		setAttr("eList", eList);
		setAttr("basicInformation", basicInformation);
		setAttr("exam_question",exam_question);
		setAttr("co_intro", co);
		setAttr("proAchieve", pAchieve);
		setAttr("gr_intro", gr);
		
		setAttr("pId", tpId);
		render("showExamReport.html");
	}
	
	public void showExamReport(){
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		DecimalFormat df = new DecimalFormat("0.00");
		Integer tpId = getParaToInt("pId");
		//basicInformation封装逻辑
		Record basicInformation = ExamDao.getBasicInformation(tpId);
		Record x = ExamDao.getDistribution(tpId);
		if(x==null){
			renderHtml("<script>alert('请先填写完试卷分析报告信息');history.go(-1);</script>");
			return;
		}
		basicInformation.set("final90", x.get("final90")).set("final80", x.get("final80")).set("final70", x.get("final70")).set("final60", x.get("final60")).set("final00", x.get("final00"));
		basicInformation.set("all90", x.get("all90")).set("all80", x.get("all80")).set("all70", x.get("all70")).set("all60", x.get("all60")).set("all00", x.get("all00"));
		//返回前端的数据
		List<Record> eList = ExamService.getCMandETypeByTpId(tpId);
		List<Record> exam_question = ExamDao.getRateByTpId(tpId);
		List<Record> coList = ExamDao.getCOByTpId(tpId);
		List<Record> grList = ExamDao.getGRByTpId(tpId);

		List<Record> co = new ArrayList<Record>();
		List<Record> gr = new ArrayList<Record>();
		
		//coList封装逻辑
		for(int i=0; i<coList.size(); i++){
			if(co.size()==0){
				Record a = new Record().set("co_order", coList.get(i).get("teaching_program_co_order")).set("weight", coList.get(i).get("weight"));
				Integer eType = coList.get(i).getInt("evaluate_type_id");
				List<Record> cm = ExamDao.getCM(coList.get(i).getInt("id"), tpId);
				Double score = 0.00;
				Double coAchieve = 0.00; //co达成度
				Integer achieve =0, expect=0;
				for (int j = 0; j < exam_question.size(); j++) {
					if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
						achieve = achieve +exam_question.get(j).getInt("achievementNum");
						expect = expect +exam_question.get(j).getInt("expect");
					}
				}
				score = Double.parseDouble(df.format((double)achieve/(double)expect));
				coAchieve = coAchieve + score*coList.get(i).getDouble("reach_weight");
				Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(i).getDouble("reach_weight"), coList.get(i).get("id"));
				
				for(int k = 0; k<coList.size(); k++){
					if(k!=i && coList.get(k).get("teaching_program_co_order").equals(a.get("co_order"))){
						score = 0.00;
						achieve =0;
						expect =0;
						eType = coList.get(k).getInt("evaluate_type_id");
						for (int j = 0; j < exam_question.size(); j++) {
							if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
								achieve = achieve +exam_question.get(j).getInt("achievementNum");
								expect = expect +exam_question.get(j).getInt("expect");
							}
						}
						score = Double.parseDouble(df.format((double)achieve/(double)expect));
						coAchieve = coAchieve + score*coList.get(k).getDouble("reach_weight");
						Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(k).getDouble("reach_weight"), coList.get(k).get("id"));
						
						List<Record> cm1 = ExamDao.getCM(coList.get(k).getInt("id"), tpId);
						cm.addAll(cm1);
					}
				}
				a.set("cmList", cm);
				a.set("coAchieve", coAchieve);
				a.set("eType", ExamDao.getEtypeByTpId(a.getInt("co_order"), tpId));
				co.add(a);
			}
			else{
				//检查co中是否有重复的元素
				boolean repeat = false;
				for (int j = 0; j < co.size(); j++) {
					if(coList.get(i).get("teaching_program_co_order").equals(co.get(j).get("co_order"))){
						repeat = true;
					}
				}
				//若无，则添加
				if(repeat!=true){
					Record a = new Record().set("co_order", coList.get(i).get("teaching_program_co_order")).set("weight", coList.get(i).get("weight"));
					Integer eType = coList.get(i).getInt("evaluate_type_id");
					List<Record> cm = ExamDao.getCM(coList.get(i).getInt("id"), tpId);
					Double score = 0.00;
					Double coAchieve = 0.00; //co达成度
					Integer achieve =0, expect=0;
					for (int j = 0; j < exam_question.size(); j++) {
						if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
							achieve = achieve +exam_question.get(j).getInt("achievementNum");
							expect = expect +exam_question.get(j).getInt("expect");
						}
					}
					score = Double.parseDouble(df.format((double)achieve/(double)expect));
					coAchieve = coAchieve + score*coList.get(i).getDouble("reach_weight");
					Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(i).getDouble("reach_weight"), coList.get(i).get("id"));
					
					for(int k = 0; k<coList.size(); k++){
						if(k!=i && coList.get(k).get("teaching_program_co_order").equals(a.get("co_order"))){
							score = 0.00;
							achieve =0;
							expect =0;
							eType = coList.get(k).getInt("evaluate_type_id");
							for (int j = 0; j < exam_question.size(); j++) {
								if(exam_question.get(j).get("co")==a.get("co_order") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
									achieve = achieve +exam_question.get(j).getInt("achievementNum");
									expect = expect +exam_question.get(j).getInt("expect");
								}
							}
							score = Double.parseDouble(df.format((double)achieve/(double)expect));
							coAchieve = coAchieve + score*coList.get(k).getDouble("reach_weight");
							Db.update("update rel_co_evaluate set achievement = ? where id = ?", score*coList.get(k).getDouble("reach_weight"), coList.get(k).get("id"));
							
							List<Record> cm1 = ExamDao.getCM(coList.get(k).getInt("id"), tpId);
							cm.addAll(cm1);
						}
					}
					a.set("cmList", cm);
					a.set("coAchieve", coAchieve);
					a.set("eType", ExamDao.getEtypeByTpId(a.getInt("co_order"), tpId));
					co.add(a);
				}
			}
			
		}
		//gr封装逻辑
		for(int i=0; i<grList.size(); i++){
			if(gr.size()==0){
				Record a = new Record().set("gr_id", grList.get(i).get("gr_id")).set("weight", grList.get(i).get("weight")).set("level1", grList.get(i).get("level1")).set("level2", grList.get(i).get("level2"));
				Integer eType = grList.get(i).getInt("evaluate_type_id");
				Double score = 0.00;
				Double grAchieve = 0.00; //gr达成度
				Integer achieve =0, expect=0;
				
				List<Record> grscore = new ArrayList<Record>();
				Record t = new Record().set("eType", eType);
				for (int j = 0; j < exam_question.size(); j++) {
					if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
						achieve = achieve +exam_question.get(j).getInt("achievementNum");
						expect = expect +exam_question.get(j).getInt("expect");
					}
				}
				t.set("achieve", achieve).set("expect", expect);
				grscore.add(t);
				
				score = Double.parseDouble(df.format((double)achieve/(double)expect));
				grAchieve = grAchieve + score*grList.get(i).getDouble("reach_weight");
				Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(i).getDouble("reach_weight"), grList.get(i).get("id"));
				
				for(int k = 0; k<grList.size(); k++){
					if(k!=i && grList.get(k).get("gr_id").equals(a.get("gr_id"))){
						score = 0.00;
						achieve =0;
						expect =0;
						eType = grList.get(k).getInt("evaluate_type_id");
						Record t1 = new Record().set("eType", eType);
						
						for (int j = 0; j < exam_question.size(); j++) {
							if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
								achieve = achieve +exam_question.get(j).getInt("achievementNum");
								expect = expect +exam_question.get(j).getInt("expect");
							}
						}
						t1.set("achieve", achieve).set("expect", expect);
						grscore.add(t1);
						
						score = Double.parseDouble(df.format((double)achieve/(double)expect));
						grAchieve = grAchieve + score*grList.get(k).getDouble("reach_weight");
						Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(k).getDouble("reach_weight"), grList.get(k).get("id"));
					}
				}
				a.set("grAchieve", grAchieve);
				a.set("grscore", grscore);
				gr.add(a);
			}
			else{
				//检查gr中是否有重复的元素
				boolean repeat = false;
				for (int j = 0; j < gr.size(); j++) {
					if(grList.get(i).get("gr_id").equals(gr.get(j).get("gr_id"))){
						repeat = true;
					}
				}
				//若无，则添加
				if(repeat!=true){
					Record a = new Record().set("gr_id", grList.get(i).get("gr_id")).set("weight", grList.get(i).get("weight")).set("level1", grList.get(i).get("level1")).set("level2", grList.get(i).get("level2"));
					Integer eType = grList.get(i).getInt("evaluate_type_id");
					Double score = 0.00;
					Double grAchieve = 0.00; //gr达成度
					Integer achieve =0, expect=0;
					
					List<Record> grscore = new ArrayList<Record>();
					Record t = new Record().set("eType", eType);
					
					for (int j = 0; j < exam_question.size(); j++) {
						if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
							achieve = achieve +exam_question.get(j).getInt("achievementNum");
							expect = expect +exam_question.get(j).getInt("expect");
						}
					}
					t.set("achieve", achieve).set("expect", expect);
					grscore.add(t);
					
					score = Double.parseDouble(df.format((double)achieve/(double)expect));
					grAchieve = grAchieve + score*grList.get(i).getDouble("reach_weight");
					Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(i).getDouble("reach_weight"), grList.get(i).get("id"));
					
					for(int k = 0; k<grList.size(); k++){
						if(k!=i && grList.get(k).get("gr_id").equals(a.get("gr_id"))){
							score = 0.00;
							achieve =0;
							expect =0;
							eType = grList.get(k).getInt("evaluate_type_id");
							Record t1 = new Record().set("eType", eType);
							
							for (int j = 0; j < exam_question.size(); j++) {
								if(exam_question.get(j).get("gr")==a.get("gr_id") && exam_question.get(j).get("evaluate_type_id").equals(eType)){
									achieve = achieve +exam_question.get(j).getInt("achievementNum");
									expect = expect +exam_question.get(j).getInt("expect");
								}
							}
							t1.set("achieve", achieve).set("expect", expect);
							grscore.add(t1);
							
							score = Double.parseDouble(df.format((double)achieve/(double)expect));
							grAchieve = grAchieve + score*grList.get(k).getDouble("reach_weight");
							Db.update("update rel_gr_evaluate set achievement = ? where id = ?", score*grList.get(k).getDouble("reach_weight"), grList.get(k).get("id"));
						}
					}
					a.set("grAchieve",grAchieve);
					a.set("grscore", grscore);
					gr.add(a);
				}
			}
			
		}
		
		setAttr("eList", eList);
		setAttr("basicInformation", basicInformation);
		
		setAttr("exam_question",exam_question);
		setAttr("co_intro", co);
		setAttr("proAchieve", x.get("program_achievement"));
		setAttr("gr_intro", gr);
		
		setAttr("pId", tpId);
		render("showExamReport.html");
	}
}

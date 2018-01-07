package com.obe.teacher;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.obe.auth.AuthController;
import com.obe.common.tools.tools;
import com.obe.program.ProgramDao;

public class TeacherController extends Controller {
	//查看我的课程：教师
	public void lessonList(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
				
		List<Record> lessons = TeacherDao.getLessonList(userInfo.get("uId").toString());
		setAttr("lesson_list", lessons);
		render("myLessonList.html");
	}	

	public void viewCourse(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		//获得登陆信息
		Integer pId = getParaToInt("pId");
		setAttr("pId", pId);
		
		List<Record> courseName = ProgramDao.getCourseName(pId);
		
		String cn_name = courseName.get(0).getStr("cn_name").toString();
		setAttr("cn_name", cn_name);
		String en_name = ProgramDao.getEnName(cn_name);
		if(en_name!=null&&en_name.length()>0){
			setAttr("en_name",en_name);
		}
		String courseNumber = ProgramDao.getCourseNumber(cn_name);
		if(courseNumber!=null&&courseNumber.length()>0){
			setAttr("courseNumber",courseNumber);
		}
		Integer hours = ProgramDao.getHours(cn_name);
		if(hours!=null){
			setAttr("hours",hours);
		}
		Integer credit = ProgramDao.getCredit(cn_name);
		if(credit!=null){
			setAttr("credit",credit);
		}
		if(ProgramDao.getCourseTypeSelected(pId)!=0){
			String course_type = TeacherDao.getCourseType(ProgramDao.getCourseTypeSelected(pId));
			if(course_type!=null&&course_type.length()>0){
				setAttr("course_type",course_type);
			}
		}
		String major = ProgramDao.getMajorNameAll().get(0).get("name");
		if(major != null && major.length()>0){
			setAttr("major",major);
		}
		List<Record> pre_teaching_program = TeacherDao.getPreTeachingProgram(ProgramDao.getPreTeachingProgramSelected(pId));
		if(pre_teaching_program!=null&&pre_teaching_program.size()!=0){
			setAttr("pre_teaching_program",pre_teaching_program);
		}
		Integer status = ProgramDao.getTpStatus(pId);
		if(status!=null){
			setAttr("status",status);
		}
		String intro = ProgramDao.getIntro(pId);
		if(intro!=null&&intro.length()>0){
			setAttr("intro",intro);
		}
		String teach_plan = ProgramDao.getTeachPlan(pId);
		if(teach_plan!=null&&teach_plan.length()>0){
			setAttr("teach_plan",teach_plan);
		}
		String books = ProgramDao.getBooks(pId);
		if(books!=null&&books.length()>0){
			setAttr("books",books);
		}
		String references = ProgramDao.getReferences(pId);
		if(references!=null&&references.length()>0){
			setAttr("references",references);
		}	
		
		List<Record> cRList = ProgramDao.getCReach(pId);
		if(cRList != null){
			for(int i =0; i<cRList.size(); i++){
				cRList.get(i).set("evaluate_type", TeacherDao.getEvaluateType(cRList.get(i).getInt("evaluate_type_id")));
				cRList.get(i).set("cm_List", TeacherDao.getCmList(cRList.get(i).getInt("id"), pId));
			}
			setAttr("cRList", cRList);
		}
		
		List<Record> gRList = ProgramDao.getGReach(pId);
		if(gRList != null){
			for(int i =0; i<gRList.size(); i++){
				gRList.get(i).set("evaluate_type", TeacherDao.getEvaluateType(gRList.get(i).getInt("evaluate_type_id")));
				gRList.get(i).set("grNumber", TeacherDao.getGrNumber(gRList.get(i).getInt("gr_id")));
				gRList.get(i).set("gr_cm_List", TeacherDao.getGrCmList(gRList.get(i).getInt("id"), pId));
			}
			setAttr("gList", gRList);
		}
		
		setAttr("Date", TeacherDao.getDate(pId));
		setAttr("cm_intro",TeacherDao.getCmIntro(pId));
		setAttr("co_intro",TeacherDao.getCoIntro(pId));
		setAttr("poGrCoCm",TeacherDao.getPoGrCoCm(pId));
		
		
		render("viewCourse.html");
	}
	
	public void test(){
		//获得登陆信息
		HashMap<String, String> sessionInfo=getSessionAttr(getSession().getId());
		Record userInfo = AuthController.userInfo(sessionInfo);
		setAttr("userInfo", userInfo);
		
		render("test.html");
	}
	
	public void upload(){
		UploadFile test = getFile();
		
		String basePath=File.separator+".."+File.separator+"fileserver"+File.separator+"upload";
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
		//解析excel表格
		try{
			FileInputStream excelFileInputStream = new FileInputStream(savedFile.getAbsolutePath());
			XSSFWorkbook workbook = new XSSFWorkbook(excelFileInputStream);
			
			XSSFSheet sheet = workbook.getSheetAt(0);
			for(int rowIndex =1; rowIndex <= sheet.getLastRowNum(); rowIndex++){
				XSSFRow row =sheet.getRow(rowIndex);
				if(row == null){
					continue;
				}
				Record tRecord = new Record();
				XSSFCell xhCell =row.getCell(0);
				tRecord.set("xh", xhCell.getNumericCellValue());
				XSSFCell numberCell = row.getCell(1);
				if(numberCell.getCellType()==0){
					Double num = numberCell.getNumericCellValue();
					Integer n = num.intValue();
					tRecord.set("number", n.toString());						
				}
				else{
					tRecord.set("number", numberCell.getStringCellValue());
				}
				XSSFCell nameCell = row.getCell(2);
				tRecord.set("name", nameCell.getStringCellValue());
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
		setAttr("intro", records);
		setAttr("status", "success");
		renderJson();
	}
	
	public void download(){
		String Filename ="测试.xlsx";
		renderFile(Filename);
	}
}

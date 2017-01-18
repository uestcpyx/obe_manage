package com.obe.index;

import com.jfinal.core.Controller;


/**
 * IndexController
 */
public class IndexController extends Controller {
	public void index() {
		render("test.html");
		//render("login.html");
		//渲染页面
	}
	public void panel(){
		render("index.html");
	}
}






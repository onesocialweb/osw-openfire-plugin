package org.onesocialweb.openfire.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PuSHServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)  {
		doProcess(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		doProcess(request, response);
	}
	
	private void doProcess(HttpServletRequest request, HttpServletResponse response){
			
	}
	
	private void subscribe(){
		
	}
}

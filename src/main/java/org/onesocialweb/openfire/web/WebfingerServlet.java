package org.onesocialweb.openfire.web;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.onesocialweb.xml.dom.XrdWriter;

@SuppressWarnings("serial")
public class WebfingerServlet extends HttpServlet{
	
	private static int BUFFSIZE = 64000;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)  {
		doProcess(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doProcess(request, response);
	}
	
	private void doProcess(HttpServletRequest request, HttpServletResponse response){
		try{
			String jid = request.getParameter("q");
			if (jid==null){
				PrintWriter out = response.getWriter();
				out.println("Please specify a valid account on this domain");
				return;
			}
			else{
				if (jid.startsWith("acct:"))
					jid=jid.substring(5);
			}

			String accountsPath = JiveGlobals.getProperty("onesocialweb.webfinger.accounts");
			String domainName= XMPPServer.getInstance().getServerInfo().getXMPPDomain();
			

			//validate the jid here ...

			if (xrdExists(accountsPath, jid)){
				serveFile(response, accountsPath, jid);
			}
			else {
				XrdWriter writer = new XrdWriter();				
				writer.writeAccount(jid, accountsPath, domainName);
				serveFile(response, accountsPath, jid);
				//write the xml to disc
				//serve the location
			}
		}catch (IOException e){

		}
	}
	
	private boolean xrdExists(String path, String jid){
		//find the xrd file
		String fileLoc=path+ "/"+jid+ ".xml";
		File f = new File(fileLoc);
		if (f.exists()) return true;
		else return false;
	}
	
	private void serveFile(HttpServletResponse response, String path, String jid){
		
		try{
		
			DataInputStream is = new DataInputStream(new FileInputStream(path+ "/"+jid+ ".xml"));

			// Send the headers
			response.setContentType("application/xrd+xml");

			// Stream the file
			final byte[] bbuf = new byte[BUFFSIZE];
			final OutputStream os = response.getOutputStream();

			int length = 0;
			while ((is != null) && ((length = is.read(bbuf)) != -1)) {
				os.write(bbuf, 0, length);
			}

			is.close();
			os.flush();
			os.close();
		}
		catch (FileNotFoundException fe){
			//we can swallow this one...
		}
		catch (IOException ioe){
			Log.error("error serving the account xrd file");
			
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// Exclude this servlet from requering the user to login
		AuthCheckFilter.addExclude("osw-openfire-plugin");		
		AuthCheckFilter.addExclude("osw-openfire-plugin/webfinger/");
		AuthCheckFilter.addExclude("osw-openfire-plugin/webfinger");
		
		
		}
	
}

package org.onesocialweb.openfire.web;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.onesocialweb.openfire.manager.FeedManager;

import PubSubHubbub.Discovery;

@SuppressWarnings("serial")
public class FeedServlet extends HttpServlet {
	
	
	private static int BUFFSIZE = 64000;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doProcess(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doProcess(request, response);
	}
	
	private void doProcess(HttpServletRequest request, HttpServletResponse response){
		
		String method=(String)request.getParameter("m");
		
		if (method.equalsIgnoreCase("subscribe")){
			try {
				doPushSubscribe(request, response);
			} catch (Exception e){
				
			}
		}
		
		if (method.equalsIgnoreCase("download")){
			try {	
				doDownloadFeed(request, response);
			}
			catch (FileNotFoundException e) {
				//manage the exception gracefully, please...
			}catch (UnsupportedEncodingException e){
				//manage the exception gracefully, please...
			}catch (IOException e){
				//manage the exception gracefully, please...
			}catch (UserNotFoundException e){
				//manage the exception gracefully, please...
			}
		}
	
		
	

	}

	
	private void doPushSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Discovery disc= new Discovery();
		String topic=request.getParameter("feed");
				
		
		FeedManager.getInstance().subscribeToFeed(topic);
		
	}
	
	private void doDownloadFeed(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, UnsupportedEncodingException, IOException, UserNotFoundException {
		
		String username=(String)request.getParameter("u");
		String feedsPath = JiveGlobals.getProperty("onesocialweb.push.feeds");
		FeedManager feedManager = FeedManager.getInstance();
		
		if (!feedManager.feedExists(feedsPath, username)){		
				feedManager.writeFeed(feedsPath, username);			
		}
		serveFile(response, feedsPath, username);
	}
	
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// Exclude this servlet from requering the user to login
		AuthCheckFilter.addExclude("osw-openfire-plugin");		
		AuthCheckFilter.addExclude("osw-openfire-plugin/updates/");
		AuthCheckFilter.addExclude("osw-openfire-plugin/updates");
		
		
	}
	
	private void serveFile(HttpServletResponse response, String path, String username){
		
		try{
		
			DataInputStream is = new DataInputStream(new FileInputStream(path+ "/"+username+ ".atom"));

			// Send the headers
			response.setContentType("application/atom+xml");

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
	
	
}

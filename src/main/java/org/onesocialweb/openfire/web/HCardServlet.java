package org.onesocialweb.openfire.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclFactory;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.acl.AclSubject;
import org.onesocialweb.model.vcard4.Field;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.model.vcard4.exception.CardinalityException;
import org.onesocialweb.model.vcard4.exception.UnsupportedFieldException;
import org.onesocialweb.model.xml.hcard.HCardWriter;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.model.acl.PersistentAclFactory;
import org.onesocialweb.openfire.model.vcard4.PersistentProfile;

@SuppressWarnings("serial")
public class HCardServlet extends HttpServlet {
	
	private static int BUFFSIZE = 64000;
	private AclFactory aclFactory;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)  {
		try{
			doProcess(request, response);
		}catch (UserNotFoundException e){
			//manage the exception nicely, please ...
		}
		catch (IOException ioe){
			//manage the exception nicely, please ...
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try{
			doProcess(request, response);
		}
		catch (UserNotFoundException e){
				//manage the exception nicely, please ...
		}
		catch (IOException ioe){
			//manage the exception nicely, please ...
		}
	}
		
	
	private void doProcess(HttpServletRequest request, HttpServletResponse response) throws UserNotFoundException, IOException{
		String username = request.getParameter("u");		
		String profilesPath= JiveGlobals.getProperty("onesocialweb.webfinger.profiles.path");
		
	/*	if (profileExists(profilesPath, username)){
			serveFile(response, profilesPath, username);
		}
		else { */
			Profile publicProfile= getPublicProfile(username + "@" + XMPPServer.getInstance().getServerInfo().getXMPPDomain());
			HCardWriter writer = new HCardWriter();	
			
			String hCardHTML=writer.buildProfilePage(publicProfile, username, profilesPath);
			serveFile(response, hCardHTML);
			//write the html file to disc
			//serve the location
	//	}
	}
	
	private void serveFile(HttpServletResponse response, String hCardHTML){
		
		try{				

			// Send the headers
			response.setContentType("text/html");
			// Stream the file
			//final byte[] bbuf = new byte[BUFFSIZE];
			final OutputStream os = response.getOutputStream();

			os.write(hCardHTML.getBytes());
			os.flush();
			os.close();
		}
	
		catch (IOException ioe){
			Log.error("error serving the hcard");
			
		}
	}
	
	public boolean profileExists(String profilePath, String username){
		//find the profile file
		String fileLoc=profilePath+ "/"+username+ ".html";
		File f = new File(fileLoc);
		if (f.exists()) return true;
		else return false;
	}
	
	public Profile getPublicProfile(String targetJID) throws UserNotFoundException {
		
		
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		PersistentProfile profile = em.find(PersistentProfile.class, targetJID);
		em.close();
		if (profile != null) {

			// We should filter all fields that are not public


			final AclAction viewAction = aclFactory.aclAction(AclAction.ACTION_VIEW, AclAction.PERMISSION_GRANT);
			List<Field> fields =profile.getFields();
			List<Field> canSeefields= new ArrayList<Field>();
			for (Field field: fields)
			{
				boolean canSee=false;
				List<AclRule> rules= field.getAclRules();
				//this is a patch, so that the profile and its fields can be retrieved even when the acl rules where not set...
				// currently the vodafonernd.com DB has many profiles without any ACL rules, which retrieves empty profiles...
				if (rules.isEmpty())
					canSee=true;
				for (AclRule rule: rules)
				{
					if ((rule.hasAction(viewAction)) && (isPublic(rule)))						
						canSee=true;										
				}
				if (canSee)
					canSeefields.add(field);						
			}

			profile.removeAll();				
			try{
				for (Field f: canSeefields){						
					f.setAclRules(new ArrayList<AclRule>());					
					profile.addField(f);
				}				
			}catch (CardinalityException ce){
			}catch (UnsupportedFieldException ufe){					
			}					
			return profile;
		}
		else {
			return null;
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		aclFactory = new PersistentAclFactory();
		
		AuthCheckFilter.addExclude("osw-openfire-plugin");		
		AuthCheckFilter.addExclude("osw-openfire-plugin/profiles/");
		AuthCheckFilter.addExclude("osw-openfire-plugin/profiles");
	}
	
	public boolean isPublic(AclRule rule){
		
		final List<AclSubject> subjects = rule.getSubjects();
		for (AclSubject aclSubject : subjects) {
			if (aclSubject.getType().equals(AclSubject.EVERYONE)) {
				return true;
			}
		}
				
		return false;
	}

}
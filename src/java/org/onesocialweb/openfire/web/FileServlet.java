/*
 *  Copyright 2010 Vodafone Group Services Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *    
 */
package org.onesocialweb.openfire.web;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.util.JiveGlobals;
import org.onesocialweb.openfire.exception.AuthenticationException;
import org.onesocialweb.openfire.exception.InvalidParameterValueException;
import org.onesocialweb.openfire.exception.MissingParameterException;
import org.onesocialweb.openfire.model.FileEntry;
import org.xmpp.packet.JID;

@SuppressWarnings("serial")
public class FileServlet extends HttpServlet {

	private static final long NOTIFICATION_THRESHOLD = 256000;

	private static int BUFFSIZE = 64000;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final PrintWriter out = response.getWriter();
		
		try {
			processGet(request, response);
		} catch (MissingParameterException e) {
			out.println("Missing paramter: " + e.getParameter());
		} catch (FileNotFoundException e) {
			out.println("File not found: " + e.getMessage());
		}
		catch (Exception e) {
			out.println("Exception occured: " + e.getMessage());
			e.printStackTrace(out);		
		}

		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		
		final PrintWriter out = response.getWriter();

		try {
			processPost(request, response);
		} catch (Exception e) {
			out.println("Exception occured: " + e.getMessage());
			e.printStackTrace(out);
		}

		out.flush();
	}

	private void processPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final PrintWriter out = response.getWriter();
		
		// 1- Bind this request to a XMPP JID
		final JID user = getAuthenticatedUser(request);
		if (user == null) {
			throw new AuthenticationException();
		}

		// 2- Process the file
		final UploadManager uploadManager = UploadManager.getInstance();

		// Files larger than a threshold should be stored on disk in temporary
		// storage place
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(256000);
		factory.setRepository(getTempFolder());

		// Prepare the upload parser and set a max size to enforce
		final ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(512000000);

		// Get the request ID
		final String requestId = request.getParameter("requestId");

		// Create a progress listener
		ProgressListener progressListener = new ProgressListener() {
			private long lastNotification = 0;

			public void update(long pBytesRead, long pContentLength, int pItems) {
				if (lastNotification == 0 || (pBytesRead - lastNotification) > NOTIFICATION_THRESHOLD) {
					lastNotification = pBytesRead;
					UploadManager.getInstance().updateProgress(user, pBytesRead, pContentLength, requestId);
				}
			}
		};
		upload.setProgressListener(progressListener);

		// Process the upload
		List items = upload.parseRequest(request);
		for (Object objItem : items) {
			FileItem item = (FileItem) objItem;
			if (!item.isFormField()) {
				String fileID = UUID.randomUUID().toString();
				File target = new File(getUploadFolder(), fileID);
				item.write(target);
				UploadManager.getInstance().commitFile(user, target, item.getName(), requestId);
				break; // Store only one file
			}
		}
	}

	private void processGet(HttpServletRequest request, HttpServletResponse response) throws MissingParameterException, IOException, InvalidParameterValueException {

		// Validate the request token
		// TODO

		// Process the parameters
		String fileId = request.getParameter("fileId");
		if (fileId == null || fileId.isEmpty()) {
			throw new MissingParameterException("fileId");
		}

		// Get the file entry
		FileEntry fileEntry = UploadManager.getInstance().getFile(fileId);
		if (fileEntry == null) {
			throw new FileNotFoundException(fileId);
		}

		// Open the file
		File file = new File(getUploadFolder(), fileEntry.getId());
		if (!file.exists()) {
			throw new FileNotFoundException(fileEntry.getName());
		}

		// Process the file
		String pSize = request.getParameter("size");
		if (pSize != null) {
		}
			
		DataInputStream is = new DataInputStream(new FileInputStream(file));

		// Send the headers
		response.setContentType(fileEntry.getType());
		response.setContentLength((int) fileEntry.getSize());
		//response.setHeader("Content-Disposition", "attachment; filename=\"" + fileEntry.getName() + "\"");

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

	private JID getAuthenticatedUser(HttpServletRequest request) throws MissingParameterException {
		// Fetch the parameters
		String jid = request.getParameter("jid");
		String signature = request.getParameter("signature");

		// Validate
		if (jid == null) {
			throw new MissingParameterException("jid");
		}
		if (signature == null) {
			throw new MissingParameterException("signature");
		}

		// Validate the session
		try {
			if (SessionValidator.getInstance().validateSession(jid, signature)) {
				return new JID(jid);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// Exclude this servlet from requering the user to login
		AuthCheckFilter.addExclude("osw-openfire-plugin");		
		AuthCheckFilter.addExclude("osw-openfire-plugin/file/");
		AuthCheckFilter.addExclude("osw-openfire-plugin/form.html");
		
		
		}

	private File getTempFolder() {	
		final String tempPath = JiveGlobals.getProperty("onesocialweb.path.temp");
		if (tempPath != null) {
			File tempFolder = new File(tempPath);
			if (tempFolder.exists() && tempFolder.canWrite()) {
				return tempFolder;
			}
		}
		
		return null;
	}
	
	private File getUploadFolder() {	
		final String uploadPath = JiveGlobals.getProperty("onesocialweb.path.upload");
		if (uploadPath != null) {
			File uploadFolder = new File(uploadPath);
			if (uploadFolder.exists() && uploadFolder.canWrite()) {
				return uploadFolder;
			}
		}
		
		return null;
	}
}

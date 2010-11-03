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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.persistence.EntityManager;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.model.FileEntry;
import org.onesocialweb.openfire.model.PersistentFileEntry;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

public class UploadManager {

	private static UploadManager instance;

	public static UploadManager getInstance() {
		if (instance == null) {
			instance = new UploadManager();
		}
		return instance;
	}

	public void updateProgress(JID user, long pBytesRead, long pContentLength, String requestId) {
		Message message = new Message();
		message.setTo(user);
		Element payload = message.addChildElement("upload", "http://onesocialweb.org/spec/1.0/upload");
		payload.addElement("request-id").setText(requestId);
		payload.addElement("status").setText("progress");
		payload.addElement("bytes-read").setText(Long.toString(pBytesRead));
		payload.addElement("size").setText(Long.toString(pContentLength));
		XMPPServer.getInstance().getMessageRouter().route(message);
	}

	public void commitFile(JID user, File file, String name, String requestId) {
		FileEntry entry = new PersistentFileEntry();
		entry.setId(file.getName());
		entry.setOwner(user.toBareJID());
		entry.setName(name);
		entry.setSize(file.length());
		entry.setType("unknown/unknown");
		
		// Attemtp to detect the mime type
		MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		try {
			Collection<?> mimeTypes = MimeUtil.getMimeTypes(file);
			if (!mimeTypes.isEmpty()) {
				Iterator<?> i = mimeTypes.iterator();
				if (i.hasNext()) {
					MimeType mimeType = (MimeType) i.next();
					entry.setType(mimeType.toString());
				}
			}
		} catch (Exception e) {};
		
		// Store the file in the database
		
		// Initialize then entity manager
		EntityManager em  = OswPlugin.getEmFactory().createEntityManager();
		em.getTransaction().begin();
		em.persist(entry);
		em.getTransaction().commit();
		em.close();
		
		Message message = new Message();
		message.setTo(user);
		Element payload = message.addChildElement("upload", "http://onesocialweb.org/spec/1.0/upload");
		payload.addElement("request-id").setText(requestId);
		payload.addElement("status").setText("completed");
		payload.addElement("file-id").setText(file.getName());
		payload.addElement("size").setText(Long.toString(entry.getSize()));
		payload.addElement("mime-type").setText(entry.getType());
		XMPPServer.getInstance().getMessageRouter().route(message);
	}
	
	public FileEntry getFile(String id) {
		EntityManager em  = OswPlugin.getEmFactory().createEntityManager();
		FileEntry entry = em.find(PersistentFileEntry.class, id);
		em.close();
		return entry;
	}

	/*
	 * Private constructor to enforce the singleton
	 */
	private UploadManager() {
		//
	}

}

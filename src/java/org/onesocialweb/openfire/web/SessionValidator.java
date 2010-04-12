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

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;

public class SessionValidator {

	private static SessionValidator instance;
	
	private XMPPServer xmppServer;
	
	private SessionManager sessionManager;
	
	public static SessionValidator getInstance() {
		if (instance == null) {
			instance = new SessionValidator();
		}
		return instance;
	}
	
	public boolean validateSession(String jid, String signature) throws Exception {
		if (xmppServer.isLocal(new JID(jid))) {
			return validateLocalSession(jid, signature);
		} else {
			throw new Exception("Not yet implemented for remote sessions");
		}
	} 
	
	private boolean validateLocalSession(String jid, String signature) {
		Session session = sessionManager.getSession(new JID(jid));
		
		// Does a valid session exist ?
		if (session == null || !session.validate()) {
			return false;
		}
		
		// Is the session authenticated ?
		if (session.getStatus() != Session.STATUS_AUTHENTICATED) {
			return false;
		}

		// Is the provided signature correct ?
		// TODO the signature should be a hash of stuff... 
		String streamID = session.getStreamID().getID();
		System.out.println("Validating signature " + signature + " against ID " + streamID);
		if (!streamID.equals(signature)) {
			return false;
		}
		
		// All checks OK ! Session is valid then
		return true;
	}
	
	/**
	 * Private constructor to enforce singleton
	 */
	private SessionValidator() {
		xmppServer = XMPPServer.getInstance();
		sessionManager = SessionManager.getInstance();
	}
}

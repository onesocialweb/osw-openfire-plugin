package org.onesocialweb.openfire.handler.activity;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class ActivityDeleteHandler  extends PEPCommandHandler {

	public static final String COMMAND = "retract";
	
	private ActivityManager activityManager;
	
	private UserManager userManager;
	
	public ActivityDeleteHandler() {
		super("OneSocialWeb - Delete activity handler");
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}
	
	@SuppressWarnings( { "deprecation", "unchecked" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		
		JID sender = packet.getFrom();
		JID target = packet.getTo();
		String id=packet.getID();
		
		
		// Process the request inside a try/catch so that unhandled exceptions
		// (oufofbounds etc...) can trigger a server error and we can send a
		// error result packet
		
		try {
			
			// A valid request is an IQ of type set, 
			if (!packet.getType().equals(IQ.Type.set)) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}
			
			// If a recipient is specified, it must be equal to the sender
			// bareJID
			if (target != null && !target.toString().equals(sender.toBareJID())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}
			
			// Only a local user can delete an activity to his stream
			if (!userManager.isRegisteredUser(sender.getNode())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}
			
			ActivityDomReader reader = new PersistentActivityDomReader();
			Element pubsubElement = packet.getChildElement();
			Element retractElement = pubsubElement.element("retract");
			Element item = (Element)retractElement.elements("item").get(0);
			String activityId=reader.readActivityId(new ElementAdapter(item));

			activityManager.deleteActivity(sender.toBareJID(), activityId);

			// Send a success result as specified in XEP-0060...
			IQ result = IQ.createResultIQ(packet);
			result.setFrom(target);
			result.setTo(sender);
			result.setID(id);

			return result;
			
		} catch (Exception e) {
			Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			IQ result = IQ.createResultIQ(packet);
			result.setChildElement(packet.getChildElement().createCopy());
			result.setError(PacketError.Condition.internal_server_error);
			return result;
		}
		
	}
	
	@Override
	public void initialize(XMPPServer server) {
		super.initialize(server);		
		userManager = server.getUserManager();
		activityManager = ActivityManager.getInstance();
	}
}

package org.onesocialweb.openfire.handler.commenting;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
import org.onesocialweb.openfire.handler.pep.PEPCommandHandler;
import org.onesocialweb.openfire.manager.ActivityManager;
import org.onesocialweb.openfire.model.activity.PersistentActivityDomReader;
import org.onesocialweb.xml.dom.ActivityDomReader;
import org.onesocialweb.xml.dom4j.ElementAdapter;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class CommentPublishHandler extends PEPCommandHandler  {

	public static final String COMMAND = "publish";
	
	private UserManager userManager;
	private ActivityManager activityManager;
	
		
	public CommentPublishHandler(){
		super("OneSocialWeb - Handler for publishing comments");
	}
	
	@Override
	public String getCommand() {
		return COMMAND;
	}
	
	@SuppressWarnings( { "deprecation", "unchecked" })
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		final JID sender = packet.getFrom();
		final JID recipient = packet.getTo();

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
			if (recipient != null && !recipient.toString().equals(sender.toBareJID())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}
			
			// Only a local user can publish an activity to his stream
			if (!userManager.isRegisteredUser(sender.getNode())) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.not_authorized);
				return result;
			}

			// A valid submit request must contain at least one entry
			Element pubsubElement = packet.getChildElement();
			Element publishElement = pubsubElement.element("publish");
			List<Element> items = publishElement.elements("item");
			if (items == null || items.size() == 0) {
				IQ result = IQ.createResultIQ(packet);
				result.setChildElement(packet.getChildElement().createCopy());
				result.setError(PacketError.Condition.bad_request);
				return result;
			}

			// Parse the activities
			ActivityDomReader reader = new PersistentActivityDomReader();
			List<String> itemIds = new ArrayList<String>(items.size());
			for (Element item : items) {
				Element entry = item.element("entry");
				if (entry != null) {
					ActivityEntry activity = reader.readEntry(new ElementAdapter(entry));
					Log.debug("CommentPublishHandler received comment: " + activity);
					try {
						if (activity.getParentId()!=null){						
							activityManager.commentActivity(sender.toBareJID(), activity);									
						}
										
					} catch (UserNotFoundException e) {}
				}
			}

			// Send a success result
			IQ result = IQ.createResultIQ(packet);
			Element resultPubsubElement = result.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
			Element resultPublishElement = resultPubsubElement.addElement("publish", "http://jabber.org/protocol/pubsub");
			resultPublishElement.addAttribute("node", PEPActivityHandler.NODE);
			for (String itemId : itemIds) {
				Element itemElement = resultPublishElement.addElement("item");
				itemElement.addAttribute("id", itemId);
			}
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

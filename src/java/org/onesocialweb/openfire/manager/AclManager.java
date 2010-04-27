package org.onesocialweb.openfire.manager;

import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.acl.AclSubject;
import org.xmpp.packet.JID;

public class AclManager 
{

	public static boolean canSee(String owner, AclRule rule, String viewer) throws UserNotFoundException {
		
		// Get the subjects, if none then access denied
		final List<AclSubject> subjects = rule.getSubjects();
		if (subjects == null)
			return false;

		Roster roster =XMPPServer.getInstance().getRosterManager().getRoster(new JID(owner).getNode());
		// Get the roster entry that match the viewer, this is only
		// used for the groups based matches
		RosterItem rosterItem = null;
		try {
			rosterItem = roster.getRosterItem(new JID(viewer));
		} catch (UserNotFoundException e) {
		}

		// Iterate through the subjects and hope for the best
		for (AclSubject aclSubject : subjects) {
			if (aclSubject.getType().equals(AclSubject.EVERYONE)) {
				return true;
			} else if (aclSubject.getType().equals(AclSubject.GROUP)) {
				if (rosterItem != null && rosterItem.getGroups().contains(aclSubject.getName())) {
					return true;
				}
			} else if (aclSubject.getType().equals(AclSubject.PERSON)) {
				if (viewer.equals(aclSubject.getName())) {
					return true;
				}
			}
		}

		// Still here ? Then we did not find a match and it is a deny
		return false;
		
		
	}
	
	
}

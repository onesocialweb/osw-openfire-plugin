package org.onesocialweb.openfire.manager;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jivesoftware.openfire.user.UserNotFoundException;
import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclFactory;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.OswPlugin;
import org.onesocialweb.openfire.model.acl.PersistentAclFactory;
import org.onesocialweb.openfire.model.activity.PersistentActivityEntry;

public class RepliesManager {

	
	private static RepliesManager instance;
	
	private final AclFactory aclFactory;

	public static RepliesManager getInstance() {
		if (instance == null) {
			// Carefull, we are in a threaded environment !
			synchronized (RepliesManager.class) {
				instance = new RepliesManager();
			}
		}
		return instance;
	}
	
	public List<ActivityEntry> getReplies( String parentId){
						
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();				
		
		Query query = em.createQuery("SELECT entry FROM ActivityEntry entry" 			
				+ "             WHERE entry.parentId = :parent ORDER BY entry.published DESC");
	
		query.setParameter("parent", parentId);	
		query.setMaxResults(20);
		List<ActivityEntry> result = query.getResultList();
		em.close();
		
		return Collections.unmodifiableList(result);
	}
	

	
	private RepliesManager(){
		aclFactory = new PersistentAclFactory();
	}
}

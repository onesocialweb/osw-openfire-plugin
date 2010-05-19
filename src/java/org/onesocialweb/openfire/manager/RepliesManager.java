package org.onesocialweb.openfire.manager;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.OswPlugin;

public class RepliesManager {

	
	private static RepliesManager instance;

	public static RepliesManager getInstance() {
		if (instance == null) {
			// Carefull, we are in a threaded environment !
			synchronized (ActivityManager.class) {
				instance = new RepliesManager();
			}
		}
		return instance;
	}
	
	public List<ActivityEntry> getReplies(String parentId){
		final EntityManager em = OswPlugin.getEmFactory().createEntityManager();
		Query query = em.createQuery("SELECT entry FROM ActivityEntry entry" 			
				+ "             WHERE entry.parentId = :parent ORDER BY entry.published DESC");
	
		query.setParameter("parent", parentId);	
		query.setMaxResults(20);
		List<ActivityEntry> result = query.getResultList();
		em.close();
		
		return Collections.unmodifiableList(result);
	}
}

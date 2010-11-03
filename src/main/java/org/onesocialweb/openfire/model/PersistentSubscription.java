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
package org.onesocialweb.openfire.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="Subscriptions")
public class PersistentSubscription implements Subscription {
	
	@Basic
	private String subscriber;
	
	@Basic
	private String target;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#getCreated()
	 */
	public Date getCreated() {
		return created;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#setCreated(java.util.Date)
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#getSubscriber()
	 */
	public String getSubscriber() {
		return subscriber;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#setSubscriber(java.lang.String)
	 */
	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#getTarget()
	 */
	public String getTarget() {
		return target;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.database.model.Subscription#setTarget(java.lang.String)
	 */
	public void setTarget(String target) {
		this.target = target;
	}

}

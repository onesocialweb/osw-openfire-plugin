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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.openfire.model.activity.PersistentActivityEntry;

@Entity(name = "Messages")
public class PersistentActivityMessage implements ActivityMessage {

	@Basic
	private String sender;

	@Basic
	private String recipient;

	@Temporal(TemporalType.TIMESTAMP)
	private Date received;

	@OneToOne(cascade = CascadeType.ALL, targetEntity = PersistentActivityEntry.class, fetch = FetchType.EAGER)
	private ActivityEntry activity;

	@Override
	public String getSender() {
		return sender;
	}

	@Override
	public void setSender(String fromJID) {
		this.sender = fromJID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.onesocialweb.openfire.model.ActivityMessage#getRecipient()
	 */
	@Override
	public String getRecipient() {
		return recipient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.onesocialweb.openfire.model.ActivityMessage#setRecipient(java.lang
	 * .String)
	 */
	@Override
	public void setRecipient(String userJID) {
		this.recipient = userJID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.onesocialweb.openfire.model.ActivityMessage#getReceived()
	 */
	public Date getReceived() {
		return received;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.onesocialweb.openfire.model.ActivityMessage#setReceived(java.util
	 * .Date)
	 */
	public void setReceived(Date received) {
		this.received = received;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.onesocialweb.openfire.model.ActivityMessage#getActivity()
	 */
	public ActivityEntry getActivity() {
		return activity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.onesocialweb.openfire.model.ActivityMessage#setActivity(org.onesocialweb
	 * .model.activity.ActivityEntry)
	 */
	public void setActivity(ActivityEntry activity) {
		this.activity = activity;
	}

}

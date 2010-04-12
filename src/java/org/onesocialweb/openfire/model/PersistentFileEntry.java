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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PersistentFileEntry implements FileEntry {

	@Id
	private String id;
	
	@Basic
	private String name;
	
	@Basic
	private String type;
	
	@Basic
	private String owner;
	
	@Basic
	private long size;
		
	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#setType(java.lang.String)
	 */
	public void setType(String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#getOwner()
	 */
	public String getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#setOwner(java.lang.String)
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#getSize()
	 */
	public long getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see org.onesocialweb.openfire.model.FileEntry#setSize(long)
	 */
	public void setSize(long size) {
		this.size = size;
	}
}

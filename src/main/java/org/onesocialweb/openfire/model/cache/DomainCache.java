package org.onesocialweb.openfire.model.cache;

import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity(name="DomainCache")
public class DomainCache {

	@Basic
	private String domain;
	
	@Basic
	private String protocols;
	
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getProtocols() {
		return protocols;
	}

	public void setProtocols(String protocols) {
		this.protocols = protocols;
	}
	
	
}

package org.onesocialweb.openfire.model.cache;

import javax.persistence.Basic;
import javax.persistence.Entity;

import org.onesocialweb.model.cache.DomainCache;

@Entity(name="DomainCache")
public class PersistentDomainCache implements DomainCache {

	@Basic
	private String domain;
	
	@Basic
	private String protocols;
	
	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public String getProtocols() {
		return protocols;
	}

	@Override
	public void setProtocols(String protocols) {
		this.protocols = protocols;
	}
	
	
}

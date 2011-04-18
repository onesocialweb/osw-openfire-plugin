package org.onesocialweb.openfire.model.atom;

import javax.persistence.Basic;
import javax.persistence.Entity;
import org.onesocialweb.model.atom.AtomTo;

@Entity(name="AtomTo")
public class PersistentAtomTo implements AtomTo{

	@Basic
	private String uri;
	
	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}
}

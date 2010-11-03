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

package org.onesocialweb.openfire.model.vcard4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.onesocialweb.model.vcard4.BirthdayField;
import org.onesocialweb.model.vcard4.EmailField;
import org.onesocialweb.model.vcard4.Field;
import org.onesocialweb.model.vcard4.FullNameField;
import org.onesocialweb.model.vcard4.GenderField;
import org.onesocialweb.model.vcard4.NameField;
import org.onesocialweb.model.vcard4.NoteField;
import org.onesocialweb.model.vcard4.PhotoField;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.model.vcard4.TelField;
import org.onesocialweb.model.vcard4.TimeZoneField;
import org.onesocialweb.model.vcard4.URLField;
import org.onesocialweb.model.vcard4.exception.CardinalityException;
import org.onesocialweb.model.vcard4.exception.UnsupportedFieldException;

@Entity(name="Profile")
public class PersistentProfile extends Profile {

	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentFullNameField.class, fetch=FetchType.EAGER)
	private FullNameField fullNameField;

	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentNoteField.class, fetch=FetchType.EAGER)
	private NoteField noteField;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentBirthdayField.class, fetch=FetchType.EAGER)
	private BirthdayField birthdayField;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentGenderField.class, fetch=FetchType.EAGER)
	private GenderField genderField;
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentPhotoField.class, fetch=FetchType.EAGER)
	private List<PhotoField> photoFields = new ArrayList<PhotoField>();
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentEmailField.class, fetch=FetchType.EAGER)
	private List<EmailField> emailFields = new ArrayList<EmailField>();
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentNameField.class, fetch=FetchType.EAGER)
	private NameField nameField;
	
	@OneToMany(cascade=CascadeType.ALL, targetEntity=PersistentTelField.class, fetch=FetchType.EAGER)
	private List<TelField> telFields = new ArrayList<TelField>();
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentTimeZoneField.class, fetch=FetchType.EAGER)
	private TimeZoneField timeZoneField;
	
	@OneToOne(cascade=CascadeType.ALL, targetEntity=PersistentUrlField.class, fetch=FetchType.EAGER)
	private URLField urlField;
	
	@Id
	private String userId;
	
	@Override
	public void addField(Field field) throws CardinalityException, UnsupportedFieldException {
		if (field instanceof FullNameField) {
			fullNameField = (FullNameField) field;
		} else if (field instanceof BirthdayField) {
			birthdayField = (BirthdayField) field;
		} else if (field instanceof GenderField) {
			genderField = (GenderField) field;
		} else if (field instanceof PhotoField) {
			photoFields.add((PhotoField) field);
		} else if (field instanceof NoteField) {
			noteField = (NoteField) field;
		} else if (field instanceof EmailField) {
			emailFields.add((EmailField) field);
		} else if (field instanceof TelField) {
			telFields.add((TelField) field);
		} 
		else if (field instanceof NameField) {
			nameField = (NameField) field;
		} else if (field instanceof TimeZoneField) {
			timeZoneField = (TimeZoneField) field;
		} else if (field instanceof URLField) {
			urlField = (URLField) field;
		}
		else {
			throw new UnsupportedFieldException();
		}
	}

	@Override
	public Field getField(String name) {
		if (name.equals(FullNameField.NAME)) {
			return fullNameField;
		} else if (name.equals(BirthdayField.NAME)) {
			return birthdayField;
		} else if (name.equals(GenderField.NAME)) {
			return genderField;
		} else if (name.equals(NoteField.NAME)) {
			return noteField;
		} else if (name.equals(NameField.NAME)) {
			return nameField;
		} else if (name.equals(TimeZoneField.NAME)) {
			return timeZoneField;
		} else if (name.equals(URLField.NAME)) {
			return urlField;
		} else if (name.equals(PhotoField.NAME)) {
			if (photoFields != null && photoFields.size() > 0) {
				return photoFields.get(0);
			} else {
				return null;
			}
		} else if (name.equals(TelField.NAME)) {
			if (telFields != null && telFields.size() > 0) {
				return telFields.get(0);
			} else {
				return null;
			}
		} else if (name.equals(EmailField.NAME)) {
			if (emailFields != null && emailFields.size() > 0) {
				return emailFields.get(0);
			} else {
				return null;
			}
		}
		
		return null;
	}

	@Override
	public List<Field> getFields() {
		List<Field> fields = new ArrayList<Field>();
		if (birthdayField != null) fields.add(birthdayField);
		if (noteField != null) fields.add(noteField);
		if (genderField != null) fields.add(genderField);
		if (fullNameField != null) fields.add(fullNameField);
		if (nameField != null) fields.add(nameField);
		if (timeZoneField != null) fields.add(timeZoneField);
		if (urlField != null) fields.add(urlField);		
		if (photoFields != null && photoFields.size() > 0) {
			for (PhotoField photoField : photoFields) {
				fields.add(photoField);
			}
		}
		if (emailFields != null && emailFields.size() > 0) {
			for (EmailField emailField : emailFields) {
				fields.add(emailField);
			}
		}if (telFields != null && telFields.size() > 0) {
			for (TelField telField : telFields) {
				fields.add(telField);
			}
		}
		return Collections.unmodifiableList(fields);
	}

	@Override
	public List<Field> getFields(String name) {
		List<Field> result = new ArrayList<Field>();
		if (name.equals(FullNameField.NAME)) {
			if (fullNameField != null) {
				result.add(fullNameField);
			}
		} else if (name.equals(BirthdayField.NAME)) {
			if (birthdayField != null) {
				result.add(birthdayField);
			}
		} else if (name.equals(GenderField.NAME)) {
			if (genderField != null) {
				result.add(genderField);
			}
		} else if (name.equals(NoteField.NAME)) {
			if (noteField != null) {
				result.add(noteField);
			}
		} else if (name.equals(NameField.NAME)) {
			if (nameField != null) {
				result.add(nameField);
			}
		} else if (name.equals(TimeZoneField.NAME)) {
			if (timeZoneField != null) {
				result.add(timeZoneField);
			}
		} else if (name.equals(URLField.NAME)) {
			if (urlField != null) {
				result.add(urlField);
			}
		} else if (name.equals(PhotoField.NAME)) {
			if (photoFields != null && photoFields.size() > 0) {
				for (PhotoField photoField : photoFields) {
					result.add(photoField);
				}
			}
		} else if (name.equals(EmailField.NAME)) {
			if (emailFields != null && emailFields.size() > 0) {
				for (EmailField emailField : emailFields) {
					result.add(emailField);
				}
			}
		} else if (name.equals(TelField.NAME)) {
			if (telFields != null && telFields.size() > 0) {
				for (TelField telField : telFields) {
					result.add(telField);
				}
			}
		}
		
		return Collections.unmodifiableList(result);
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public boolean hasField(String name) {
		if (name.equals(FullNameField.NAME)) {
			return (fullNameField != null);
		} else if (name.equals(BirthdayField.NAME)) {
			return (birthdayField != null);
		} else if (name.equals(GenderField.NAME)) {
			return (genderField != null);
		} else if (name.equals(NoteField.NAME)) {
			return (noteField != null);
		} else if (name.equals(NameField.NAME)) {
			return (nameField != null);
		} else if (name.equals(URLField.NAME)) {
			return (urlField != null);
		} else if (name.equals(TimeZoneField.NAME)) {
			return (timeZoneField != null);
		}
		else if (name.equals(PhotoField.NAME)) {
			return (photoFields != null && photoFields.size() > 0);
		}
		else if (name.equals(TelField.NAME)) {
			return (telFields != null && telFields.size() > 0);
		}
		else if (name.equals(EmailField.NAME)) {
			return (emailFields != null && emailFields.size() > 0);
		}
		
		return false;
	}

	@Override
	public void removeAll() {
		fullNameField = null;
		birthdayField = null;
		genderField   = null;
		noteField     = null;
		nameField=null;
		urlField=null;
		timeZoneField=null;
		photoFields.clear();
		emailFields.clear();
		telFields.clear();
	}

	@Override
	public void removeField(Field field) {
		if (field instanceof FullNameField) {
			if (fullNameField != null && fullNameField.equals(field)) {
				fullNameField = null;
			}
		} else if (field instanceof BirthdayField) {
			if (birthdayField != null && birthdayField.equals(field)) {
				birthdayField = null;
			}
		} else if (field instanceof GenderField) {
			if (genderField != null && genderField.equals(field)) {
				genderField = null;
			}
		} else if (field instanceof PhotoField) {
			if (photoFields != null && photoFields.size() > 0) {
				photoFields.remove(field);
			}
		} else if (field instanceof EmailField) {
			if (emailFields != null && emailFields.size() > 0) {
				emailFields.remove(field);
			}
		} else if (field instanceof TelField) {
			if (telFields != null && telFields.size() > 0) {
				telFields.remove(field);
			}
		}else if (field instanceof NoteField) {
			if (noteField != null && noteField.equals(field)) {
				noteField = null;
			}
		} else if (field instanceof NameField) {
			if (nameField != null && nameField.equals(field)) {
				nameField = null;
			}
		} else if (field instanceof URLField) {
			if (urlField != null && urlField.equals(field)) {
				urlField = null;
			}
		} else if (field instanceof TimeZoneField) {
			if (timeZoneField != null && timeZoneField.equals(field)) {
				timeZoneField = null;
			}
		}
	}

	@Override
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}

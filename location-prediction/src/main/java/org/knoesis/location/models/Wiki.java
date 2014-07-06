package org.knoesis.location.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Wiki implements Serializable {
	private static final long serialVersionUID = 1L;
	private String title;
	private boolean isDisambiguation;
	private boolean isStub;
	private boolean isRedirect;
	private String redirectText;
	private List<String> internalLinks;
	

	public Wiki(String title, boolean isDisambiguation, boolean isStub, boolean isRedirect,
				String redirectText, List<String> internalLinks) {
			this.title = title;
			this.isDisambiguation = isDisambiguation;
			this.isStub = isStub;
			this.isRedirect= isRedirect;
			this.redirectText = redirectText;
			this.internalLinks = new ArrayList<String>(internalLinks);
	}
	
	public String getTitle() {
			return this.title;
	}
	
	public boolean getIsDisambiguation() {
			return this.isDisambiguation;				
	}
	
	public boolean getIsStub() {
			return this.isStub;
	}
	
	public boolean getIsRedirect() {
			return this.isRedirect;
	}
	
	public String getRedirectText() {
			return this.redirectText;
	}
	
	public List<String> getInternalLinks() {
			return this.internalLinks;
	}

	
}

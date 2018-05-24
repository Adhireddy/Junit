package com.kibo.ng.bis.model;

public class SitesModel {

	private String siteName;
	private boolean allowIntegration;
	private String siteControlFolderPath;
	private String siteCommandFolderPath;
	private String secring;
	
	public String getSecring() {
		return secring;
	}
	public void setSecring(String secring) {
		this.secring = secring;
	}
	public String getPassphrase() {
		return passphrase;
	}
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	private String passphrase;
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public boolean isAllowIntegration() {
		return allowIntegration;
	}
	public void setAllowIntegration(boolean allowIntegration) {
		this.allowIntegration = allowIntegration;
	}
	public String getSiteControlFolderPath() {
		return siteControlFolderPath;
	}
	public void setSiteControlFolderPath(String siteControlFolderPath) {
		this.siteControlFolderPath = siteControlFolderPath;
	}
	public String getSiteCommandFolderPath() {
		return siteCommandFolderPath;
	}
	public void setSiteCommandFolderPath(String siteCommandFolderPath) {
		this.siteCommandFolderPath = siteCommandFolderPath;
	}

}

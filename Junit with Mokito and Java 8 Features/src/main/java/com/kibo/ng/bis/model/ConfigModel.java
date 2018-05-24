package com.kibo.ng.bis.model;

public class ConfigModel {

	private String environment;
	
	private Integer tenantId;
	private Integer siteId;
	private String appId;
	private String sharedSecret;
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
	
	
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public Integer getTenantId() {
		return tenantId;
	}
	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}
	public Integer getSiteId() {
		return siteId;
	}
	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getSharedSecret() {
		return sharedSecret;
	}
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	
	
}

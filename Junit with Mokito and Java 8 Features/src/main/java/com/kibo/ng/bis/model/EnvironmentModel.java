package com.kibo.ng.bis.model;

public class EnvironmentModel {

	private String environment;
	private String tenantId;
	private String siteId;
	private String appId;
	private String sharedSecret;
	
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
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

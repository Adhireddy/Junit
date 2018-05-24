package com.kibo.ng.bis.model;

public class CatalogModel {

	private String siteName;
	private Integer masterCatalogId;
	private Integer catalogId;
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public Integer getMasterCatalogId() {
		return masterCatalogId;
	}
	public void setMasterCatalogId(Integer masterCatalogId) {
		this.masterCatalogId = masterCatalogId;
	}
	public Integer getCatalogId() {
		return catalogId;
	}
	public void setCatalogId(Integer catalogId) {
		this.catalogId = catalogId;
	}
	
}

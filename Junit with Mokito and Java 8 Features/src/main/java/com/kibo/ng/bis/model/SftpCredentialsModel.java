package com.kibo.ng.bis.model;



public class SftpCredentialsModel {

    private String ipAddress;
    private String userName;
    private String password;
    private int port;
    private String sftpExportPath;
    private String sftpImportPath;
    private boolean isDefault;
    
	public SftpCredentialsModel() {

    }

    public SftpCredentialsModel(String ipAddress, String userName, String password, int port) {
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

	public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSftpExportPath() {
		return sftpExportPath;
	}

	public void setSftpExportPath(String sftpExportPath) {
		this.sftpExportPath = sftpExportPath;
	}

	public String getSftpImportPath() {
		return sftpImportPath;
	}

	public void setSftpImportPath(String sftpImportPath) {
		this.sftpImportPath = sftpImportPath;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

}
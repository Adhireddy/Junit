package com.kibo.ng.bis.model;

public class EmailInfo {
	private String from;
	private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String msg;
    private Boolean enabled;

    public EmailInfo(String from, String to, String cc, String bcc, String subject, String msg, Boolean enabled) {
    	this.from = from;
    	this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.msg = msg;
        this.enabled = enabled;
    }
    
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
    public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	 
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}

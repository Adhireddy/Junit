package com.kibo.ng.bis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.kibo.ng.bis.jaxb.T4EmailNotification;
import com.kibo.ng.bis.jaxb.TBatchIntegrationRequest;
import com.kibo.ng.bis.model.EmailInfo;


@Service
public class EmailService {
	
	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	
	@Autowired
	private MailSender mailSender;

	@Value("${mail.enabled}")
	private Boolean emailEnabled;
	
	@Value("${mail.from}")
	private String from;

	@Value("${mail.to}")
	private String to;
	
	@Value("${mail.cc}")
	private String cc;
	
	@Value("${mail.bcc}")
	private String bcc;
	
	@Value("${mail.subject.fatal}")
	private String subjectFatal;
	
	@Value("${mail.commandfile.subject.succes}")
	private String subjectSucess;
	
	@Value("${mail.commandfile.subject.failure}")
	private String subjectFailure;
	
	
	
	public void sendFatalEmail(String msg) {
		if(emailEnabled) {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setCc(cc);
			message.setBcc(bcc);
			message.setSubject(subjectFatal);
			message.setText(msg);
			mailSender.send(message);
		}
		logger.error("sending fatal mail with message {}", msg);
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendNotification(String msg, T4EmailNotification emailNotification) {
		
		if(emailNotification == null) {
			sendFatalEmail(msg);
		} else {
			if(emailEnabled) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(from);
				message.setTo(emailNotification.getTo());
				message.setCc(emailNotification.getCc().getValue());
				message.setBcc(bcc);
				message.setSubject(subjectFailure);
				message.setText(msg);
				mailSender.send(message);
			}
			logger.info("sending Notification mail with message {}", msg);
		}
	}

	public void sendNotification(String msg, TBatchIntegrationRequest control) {
		try {
			T4EmailNotification emailNotification = control.getMerchant().getSite().getEmailNotification();
			sendNotification(msg, emailNotification);
			logger.info("sending Notification mail with message");
		} catch(Exception e) {
			sendFatalEmail(msg);
		}
	}
}
